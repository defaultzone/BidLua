/**
 * BidLua - compiled language for Lua.
 * Copyright (C) 2023 defaultzon3 (also known as DZONE)
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
 * Replacing key to value (defined in `Names.kt`) for each match.
 * Does not react to < single || double > quotes and < "[[ ... ]]" || "[=[ ... ]=]" > brackets.
 * @param content String, which need to replace by found `key` to `value`.
 * @param map Map, where replacing `key` to `value`. By default, it's `names.getNames <String, String>`. Accepts string as key and string as value.
 * @return String with replaced keys.
 */

private fun replaceKeys(content: String, map : Map<String, String>): String {
    var result : String = ""
    var i : Int = 0

    while (i < content.length) {
        when {
            // Checking for quotes in string, there may be as " ... " or ' ... ' (for brackets see before).
            content[i] == '\'' || content[i] == '"' -> {
                val quote : Char = content[i]
                result += quote
                i++
                while (i < content.length && content[i] != quote) {
                    result += content[i]
                    i++
                }
                if (i < content.length && content[i] == quote) {
                    result += quote
                    i++
                }
            }
            // Checking for brackets in string, there may be as [[ ... ]] or [=[ ... ]=]
            content.startsWith("[[", i) || content.startsWith("[=[", i) -> {
                val start : String = content.substring(i, i + 2)
                val end : String = if (start == "[[") "]]" else "]=]"
                result += start
                i += 2
                while (i < content.length && !content.startsWith(end, i)) {
                    result += content[i]
                    i++
                }
                if (i < content.length && content.startsWith(end, i)) {
                    result += end
                    i += end.length
                }
            }
            // If key is not inside of string, change < found key > to < lua name >.
            else -> {
                var found : Boolean = false
                for ((key, value) in map) {
                    if (content.startsWith(key, i)) {
                        result += value
                        i += key.length
                        found = true
                        break
                    }
                }
                if (!found) {
                    result += content[i]
                    i++
                }
            }
        }
    }

    return result
}

/**
 * Changing compiling status. If used flag `--stop-on-error`, compile will be stopped.
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
 * @param filePath Path to user-defined map-file. File extention - .map.blya.
 * @param charset  Charset of user-defined map-file.
 * @return MutableMap<String, String>
 */

private fun createMapFromFile(filePath : String, charset: Charset) : MutableMap<String, String> {
    var mapToReturn : MutableMap<String, String> = mutableMapOf()
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
 * Compile .blya file to .lua file.
 * @param input         java.io.File that need to compile.
 * @param output        java.io.File that will be output after compiling.
 * @param charset       Charset of input, output and map file. For all charsets, see `Charset.availableCharsets()`.
 * @param userMapPath   Path to user-defined map. If you don't need to use BidLua map, enter "null" as string.
 */

fun compile(input : File, output : File, charset : Charset, userMapPath : String) {
    if (Data.compiling) {
        val userMapInitialize: Map<String, String> = createMapFromFile(userMapPath, charset)
        val userMap: MutableMap<String, String> = mutableMapOf()
        if (userMapInitialize["NO_MAP"] == null) userMap += userMapInitialize

        if (input.exists()) {
            val fileContent: String = input.readText(charset)
            val outputFileContent : String = when (Data.flags["--dont-use-default-map"]) {
                "true"  -> replaceKeys(fileContent, userMap)
                else    -> replaceKeys(replaceKeys(fileContent, userMap), names)
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