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
import java.util.*
import javax.print.DocFlavor.STRING

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
    // Compiler function: базар(content). Replaces cyrillic symbols to latin.
    // On usage should return pure latin string.
    var safeInput : String = input.replace(Regex("базар\\(([^)]*)\\)")) {
        val symbols : List<String> = it.groupValues[1].lowercase().split("")
        var result = ""
        for (symbol in symbols) {
            result += when (symbol) {
                "а" -> "a"
                "б" -> "b"
                "в" -> "v"
                "г" -> "g"
                "д" -> "d"
                "е" -> "e"
                "ё" -> "e"
                "ж" -> "zh"
                "з" -> "z"
                "и" -> "i"
                "й" -> "y"
                "к" -> "k"
                "л" -> "l"
                "м" -> "m"
                "н" -> "n"
                "о" -> "o"
                "п" -> "p"
                "р" -> "r"
                "с" -> "s"
                "т" -> "t"
                "у" -> "u"
                "ф" -> "f"
                "х" -> "h"
                "ц" -> "tc"
                "ч" -> "ch"
                "ш" -> "sh"
                "щ" -> "shch"
                "ъ" -> "'"
                "ы" -> "i"
                "ь" -> "'"
                "э" -> "e"
                "ю" -> "yu"
                "я" -> "ya"
                else -> symbol
            }
        }
        result
    }

    val output : StringBuilder = StringBuilder()
    val n = safeInput.length
    var i = 0

    while (i < n) {
        when {
            safeInput.startsWith("[[", i) -> { // Skip until closing brackets.
                val j = safeInput.indexOf("]]", i + 2).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 2)
                i = j + 2
            }
            safeInput.startsWith("[=[", i) -> { // Skip until closing brackets.
                val j = safeInput.indexOf("]=]", i + 2).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 3)
                i = j + 3
            }
            safeInput.startsWith("—[[", i) -> { // Skip until closing brackets.
                val j = safeInput.indexOf("]]", i + 2).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 2)
                i = j + 2
            }
            safeInput.startsWith("—[=[", i) -> { // Skip until closing brackets.
                val j = safeInput.indexOf("]=]", i + 2).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 3)
                i = j + 3
            }
            safeInput.startsWith("--", i) -> { // Skip until end of line.
                val j = safeInput.indexOf('\n', i + 2).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j)
                i = j
            }
            safeInput.startsWith("\"", i) -> { // Skip until closing quote.
                val j = safeInput.indexOf('"', i + 1).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 1)
                i = j + 1
            }
            safeInput.startsWith("'", i) -> { // Skip until closing quote.
                val j = safeInput.indexOf('\'', i + 1).takeIf { it >= 0 } ?: n
                output.append(safeInput, i, j + 1)
                i = j + 1
            }
            else -> { // Try to match a key from the map
                var found = false
                for ((key, value) in map) {
                    val (safeKey, safeValue) = when (fileExtension) {
                        "blya"  -> Pair(key, value)
                        else    -> Pair(value, key)
                    }

                    if (safeInput.startsWith(safeKey, i)) {
                        output.append(safeValue)
                        i += safeKey.length
                        found = true
                        break
                    }
                }
                if (!found) {
                    output.append(safeInput[i])
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
 *      ADD key::value;
 * This will be handled as kotlin default map:
 *      "key" to "value"
 * For more information, see /tests/file.map.blya.
 *
 * v1.1.1: Allows to create map from main file.
 *
 * @param filePath Path to user-defined map-file. File extension - .map.blya.
 * @param fileContent File content, where need create map.
 * @param charset  Charset of user-defined map-file.
 * @return MutableMap<String, String>
 */

private fun createProjectMap(filePath : String, fileContent : String, charset: Charset) : Pair<MutableMap<String, String>, String> {
    val mapToReturn : MutableMap<String, String> = mutableMapOf()
    val file = File(filePath)
    var safeFileContent = ""

    // Creating map from `.map.blya` file.
    if (filePath != "null") {
        if (file.exists()) {
            val mapFileContent: String = file.readText(charset)
            Regex("ADD (.*?)::(.*?);").findAll(mapFileContent).forEach {
                mapToReturn += mutableMapOf(it.groupValues[1] to it.groupValues[2])
            }
        } else {
            println("File $file doesn't exist.")
            println("Check again the path to the file (file extension should be \".map.blya\"), and if it still turned out to be correct - create issue ...")
            println("... on ${Data.REPOSITORY_URL}/issues.")
            mapToReturn["NO_MAP"] = "NO_MAP"
            compilingStatusChange()
        }
    }

    // Creating map from `.blya` file.
    Regex("#add (.*?)::(.*?);").findAll(fileContent).forEach {
        mapToReturn += mutableMapOf(it.groupValues[1] to it.groupValues[2])
        safeFileContent = fileContent.replace(it.value, "")
    }

    return Pair(mapToReturn, safeFileContent)
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
        var (userMapInitialize, fileContent) = createProjectMap(userMapPath, input.readText(charset), charset)
        var userMap : MutableMap<String, String> = mutableMapOf()
        if (userMapInitialize["NO_MAP"] == null) userMap += userMapInitialize

        // Sorting maps by key descending ( key.length ).
        if (userMap::class.simpleName != "LinkedHashMap")
            userMap = userMap.toList().sortedByDescending { it.first.length }.toMap() as MutableMap<String, String>
        names = names.toList().sortedByDescending { it.first.length }.toMap()

        if (input.exists()) {
            val outputFileContent : String = when (Data.flags["--ignore-default-map"]) {
                "true"  -> replaceKeys(fileContent, userMap, input.extension)
                else    -> {
                    fileContent = replaceKeys(fileContent, userMap, input.extension)
                    replaceKeys(fileContent, names, input.extension)
                }
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
                    println("Compiler can't create new lua-file. Try again. || ${input.path} || ${output.path}")
            }
        }
    }
}