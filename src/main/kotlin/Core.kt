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

fun replaceKeys(input : String, map : Map<String, String>) : String {
    // Compiler function: базар(content). Replaces cyrillic symbols to latin.
    // On usage should return pure latin string.
    val safeInput : String = input.replace(Regex("базар\\(([^)]*)\\)")) {
        val symbols : List<String> = it.groupValues[1].lowercase().split("")
        var result = ""
        for (symbol in symbols) {
            result += when (symbol) {
                "а" -> "a";     "б" -> "b";     "в" -> "v"
                "г" -> "g";     "д" -> "d";     "е" -> "e"
                "ё" -> "e";     "ж" -> "zh";    "з" -> "z"
                "и" -> "i";     "й" -> "y";     "к" -> "k"
                "л" -> "l";     "м" -> "m";     "н" -> "n"
                "о" -> "o";     "п" -> "p";     "р" -> "r"
                "с" -> "s";     "т" -> "t";     "у" -> "u"
                "ф" -> "f";     "х" -> "h";     "ц" -> "tc"
                "ч" -> "ch";    "ш" -> "sh";    "щ" -> "shch"
                "ъ" -> "'";     "ы" -> "i";     "ь" -> "'"
                "э" -> "e";     "ю" -> "yu";    "я" -> "ya"
                else -> symbol
            }
        }
        result // ^replace
    }


    var i = 0
    var output = ""
    var skipFound = false
    while (i < safeInput.length) {
        val skipUntilMap : Map<*, *> = mapOf(
            /* <String|Char, String|Char> */
            "--" to "\n",
            "--[[" to "]]",
            "--[=[" to "]=]",
            "[[" to "]]",
            "[=[" to "]=]",
            '"' to '"',
            '\'' to '\''
        )

        // Skip the expression if it is enclosed in certain character(s) from key to value.
        for ((key, value) in skipUntilMap) {
            if (key is Char && value is Char) {
                if (safeInput[i] == key) {
                    i++
                    output += key
                    while (i < safeInput.length && safeInput[i] != value) {
                        output += safeInput[i]
                        i++
                    }
                    skipFound = true
                    break
                }
            } else if (key is String && value is String) {
                if (safeInput.substring(i).startsWith(key)) {
                    i += key.length
                    output += key
                    while (!safeInput.substring(i).startsWith(value)) {
                        output += safeInput[i]
                        i++
                        if (i == safeInput.length) break
                    }
                    skipFound = true
                    break
                }
            }
        }

        if (!skipFound) {
            var mapFound = false
            for ((key, value) in map) {
                if (safeInput.substring(i).startsWith(key)) {
                    i += key.length
                    output += value
                    mapFound = true
                    break
                }
            }

            if (!mapFound) {
                output += safeInput[i]
                i++
            }
        }
        skipFound = false
    }

    return output
}

/**
 * Creating map from user-defined map-file. File syntax:
 *      ADD key::value;
 * This will be handled as kotlin default map:
 *      "key" to "value"
 * For more information, see /tests/file.map.blya.
 *
 * @since v1.1.1: Allows to create map from main file.
 *
 * @param filePath Path to user-defined map-file. File extension - .map.blya.
 * @param fileContent File content, where need create map.
 * @param charset  Charset of user-defined map-file.
 * @return MutableMap<String, String>
 */

private fun createProjectMap(filePath : String, fileContent : String, charset : Charset) : Pair<MutableMap<String, String>, String> {
    val mapToReturn : MutableMap<String, String> = mutableMapOf()
    val file = File(filePath)
    var safeFileContent = fileContent

    // Creating map from `.map.blya` file.
    if (filePath != "null") {
        if (file.exists()) {
            val mapFileContent: String = file.readText(charset)
            Regex("ADD (.*?)::(.*?);").findAll(mapFileContent).forEach {
                mapToReturn += mutableMapOf(it.groupValues[1] to it.groupValues[2])
            }
        } else throw IllegalAccessError("Error opening .map.blya file in ($filePath).")
    }


    // Creating map from `.blya` file.
    Regex("#add (.*?)::(.*?);").findAll(fileContent).forEach {
        mapToReturn += mutableMapOf(it.groupValues[1] to it.groupValues[2])
        safeFileContent = fileContent.replace(it.value, "")
    }

    return Pair(mapToReturn, safeFileContent)
}

/**
 * Compile .blya file to .lua file.
 *
 * @param input         java.io.File that need to compile.
 * @param output        java.io.File that will be output after compiling.
 * @param charset       Charset of input, output and map file. For all charsets, see `Charset.availableCharsets()`.
 * @param userMapPath   Path to user-defined map. If you don't need to use BidLua map, enter "null" as string.
 */

fun compile(input : File, output : File, charset : Charset, userMapPath : String, ignoreDefaultCompilerMap : Boolean) {
    var (userMapInitialize, fileContent) = createProjectMap(userMapPath, input.readText(charset), charset)
    var userMap : MutableMap<String, String> = mutableMapOf()
    if (userMapInitialize["NO_MAP"] == null) userMap += userMapInitialize

    // Sorting maps by key descending ( key.length ).
    if (userMap !is LinkedHashMap)
        userMap = userMap.toList().sortedByDescending { it.first.length }.toMap() as MutableMap<String, String>
    names = names.toList().sortedByDescending { it.first.length }.toMap()

    output.writeText(when (ignoreDefaultCompilerMap) {
        true  -> replaceKeys(fileContent, userMap)
        false -> {
            fileContent = replaceKeys(fileContent, userMap)
            replaceKeys(fileContent, names)
        }
    }, charset)
}