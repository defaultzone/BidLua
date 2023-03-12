/**
 * BidLua - compiled language for Lua.
 * Copyright (C) 2023 defaultzon3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see < https://www.gnu.org/licenses/ >.
 */

import names.*
import java.io.File
import java.nio.charset.Charset

/**
 * Replacing key/value to value/key (defined in `Names.kt`) for each match.
 * Does not react to key/value inside:
 *      < single || double > quotes,
 *      < "[[" to "]]" and "[=[" to "]=]" > brackets,
 *      < -- > single line commentary,
 *      < "--[[" to "]]" and "--[-[" to "]=]" > multi line commentary.
 * @param input String, which need to replace by found `key/value` to `value/key`.
 * @param map Map, where replacing `key/value` to `value/key`.
 * @return String with replaced key(s)/value(s).
 */

fun replaceKeys(input : String, map : Map<String, String>, fileExtension : String) : String {
    val output : StringBuilder = StringBuilder()
    val n = input.length
    var i = 0

    while (i < n) {
        when {
            input.startsWith("[[", i) -> { // Skip until closing brackets.
                val j = input.indexOf("]]", i + 2).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 2)
                i = j + 2
            }
            input.startsWith("[=[", i) -> { // Skip until closing brackets.
                val j = input.indexOf("]=]", i + 2).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 3)
                i = j + 3
            }
            input.startsWith("—[[", i) -> { // Skip until closing brackets.
                val j = input.indexOf("]]", i + 2).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 2)
                i = j + 2
            }
            input.startsWith("—[=[", i) -> { // Skip until closing brackets.
                val j = input.indexOf("]=]", i + 2).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 3)
                i = j + 3
            }
            input.startsWith("--", i) -> { // Skip until end of line.
                val j = input.indexOf('\n', i + 2).takeIf { it >= 0 } ?: n
                output.append(input, i, j)
                i = j
            }
            input.startsWith("\"", i) -> { // Skip until closing quote.
                val j = input.indexOf('"', i + 1).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 1)
                i = j + 1
            }
            input.startsWith("'", i) -> { // Skip until closing quote.
                val j = input.indexOf('\'', i + 1).takeIf { it >= 0 } ?: n
                output.append(input, i, j + 1)
                i = j + 1
            }
            else -> { // Try to match a key from the map
                var found = false
                for ((key, value) in map) {
                    val (safeKey, safeValue) = when (fileExtension) {
                        "blya"  -> Pair(key, value)
                        else    -> Pair(value, key)
                    }

                    if (input.startsWith(safeKey, i)) {
                        output.append(safeValue)
                        i += safeKey.length
                        found = true
                        break
                    }
                }
                if (!found) {
                    output.append(input[i])
                    i++
                }
            }
        }
    }

    return output.toString()
}

/**
 * Changing compiling status. If used flag `--stop-on-error`, compiling will be stopped.
 * Otherwise, compiling will be continued.
 */

private fun compilingStatusChange() {
    if (Data.flags["--stop-on-error"] == "true") {
        Data.compiling = false
        println("Compiling was stopped because \"--stop-on-error\" value is true. To compile anyway, remove this flag.")
    } else {
        println("Continue compiling.")
    }
}

/**
 * Creating map from user-defined map-file. File syntax:
 *      SET key::value;
 * This will be handled as kotlin default map:
 *      "key" to "value"
 * For more information, see /tests/file.map.blya.
 *
 * @param filePath Path to user-defined map-file. File extension - .map.blya.
 * @param charset  Charset of user-defined map-file.
 * @return MutableMap<String, String>
 */

private fun createMapFromFile(filePath : String, charset: Charset) : MutableMap<String, String> {
    val mapToReturn : MutableMap<String, String> = mutableMapOf()
    val file = File(filePath)

    if (filePath != "null") {
        if (file.exists()) {
            val fileContent: String = file.readText(charset)
            val matches = Regex("SET (.*?)::(.*?);").findAll(fileContent)
            for (match in matches) {
                mapToReturn += mutableMapOf(match.groupValues[1] to match.groupValues[2])
            }
        } else {
            println("File $file doesn't exist.")
            println("Check again the path to the file (file extension should be \".map.blya\"), and if it still turned out to be correct - create issue ...")
            println("... on ${Data.REPOSITORY_URL}/issues.")
            mapToReturn["NO_MAP"] = "NO_MAP"
            compilingStatusChange()
        }
    }

    return mapToReturn
}

/**
 * Compile .blya file to .lua file and vice versa.
 * @param input         java.io.File that need to compile.
 * @param output        java.io.File that will be output after compiling.
 * @param charset       Charset of input, output and map file. For all charsets, see `Charset.availableCharsets()`.
 * @param userMapPath   Path to user-defined map. If you don't need to use BidLua map, enter "null" as string.
 */

fun compile(input : File, output : File, charset : Charset, userMapPath : String) {
    if (Data.compiling) {
        val userMapInitialize : Map<String, String> = createMapFromFile(userMapPath, charset)
        var userMap : MutableMap<String, String> = mutableMapOf()
        if (userMapInitialize["NO_MAP"] == null) userMap += userMapInitialize

        // Sorting maps by key descending ( key.length ).
        userMap = userMap.toList().sortedByDescending { it.first.length }.toMap() as MutableMap<String, String>
        names   = names.toList().sortedByDescending { it.first.length }.toMap()

        if (input.exists()) {
            var fileContent : String = input.readText(charset)
            val outputFileContent : String = when (Data.flags["--ignore-default-map"]) {
                "true"  -> replaceKeys(fileContent, userMap, input.extension)
                else    -> replaceKeys(replaceKeys(fileContent, userMap, input.extension), names, input.extension)
            }
            if (output.exists()) {
                output.writeText(outputFileContent, charset)
                println("Compiled successfully.")
            } else {
                output.createNewFile()
                if (output.exists()) {
                    output.writeText(outputFileContent, charset)
                    println("Compiled successfully.")
                } else
                    println("Compiler can't create new lua-file. Try again. java.io.File: $input; $output;")
            }
        }
    }
}