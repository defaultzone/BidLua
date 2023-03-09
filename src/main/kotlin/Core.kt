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

import names.* // For names, which represents as Map<String, String>

/**
 * Replacing key to value (defined in `Names.kt`) for each match.
 * Does not react to < single || double > quotes and < "[[ ... ]]" || "[=[ ... ]=]" > brackets.
 * @param content String, which need to replace by found `key` to `value`.
 * @param map Map, where replacing `key` to `value`. By default, it's `names.getNames <String, String>`. Accepts string as key and string as value.
 * @return String with replaced keys.
 */

private fun replaceKeys(content: String, map : Map<String, String> = names): String {
    var result : String = ""
    var i : Int = 0

    while (i < content.length) {
        when {
            // Checking for quotes in string, there may be as " ... " or ' ... ' (for brackets see below).
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
                for ((key, value) in names) {
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
 * In progress, soon this function will be writing lua-file.
 * Now it's for debugging.
 * @param fileContent will be changed for java.io.File.
 */

fun compile(fileContent : String) : Unit {
    println(replaceKeys(fileContent))
}
