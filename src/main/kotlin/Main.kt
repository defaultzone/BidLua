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

import Data.flags
import java.io.File
import java.nio.charset.Charset

/**
 * Program arguments, < ** > is optional:
 *      [0]     = < input file >
 *      [1]     = < output file >
 *      [2 - *] = < flags > ( ** )
 * Acceptable flags: ( ** )
 *      That accepts value (--flag=value):
 *          --charset            < charset >
 *          --map                < file with map >
 *      That not accepts value (--flag):
 *          --stop-on-error
 *          --ignore-default-map
 * @param args ( arguments ) Array of string, where stores program arguments.
 */

fun main(args : Array<String>) {
    for (i in 2 until args.size) run {
        for ((key, _) in flags) {
            val splitFlag : List<String> = args[i].split("=")
            if (key == splitFlag[0]) {
                flags[key] = when (splitFlag.size) {
                    2       -> splitFlag[1]
                    else    -> "true"
                }
            }
        }
    }

    val helpContent : String = """
        ${Colors.BOLD}> BidLua ${Colors.YELLOW}( ${Data.VERSION} )${Colors.RESET + Colors.BOLD} compiler to Lua usage
            ${Colors.RESET}java -jar < path to .jar file > < input || -h || --help > < output > [ flags ]
        ${Colors.BOLD}> Flags
            ${Colors.RESET}--charset=value         ${Colors.BOLD} < value > is charset to input, output and map files.
            ${Colors.RESET}--map=value             ${Colors.BOLD} < value > is path to your file.map.blya.
            ${Colors.RESET}--stop-on-error         ${Colors.BOLD} Stop compiling on get error.
            ${Colors.RESET}--ignore-default-map    ${Colors.BOLD} Ignore BidLua default map.
        > BidLua Copyright (C) 2023 defaultzon3 (also known as DZONE)${Colors.RESET}
            This program comes with ${Colors.BOLD}ABSOLUTELY NO WARRANTY;${Colors.RESET}
            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.
        ${Colors.BOLD}> Source code: ${Colors.UNDERLINE}https://github.com/defaultzon3/BidLua/
    """.trimIndent()

    if (args.isEmpty()) {
        println(helpContent)
    } else {
        val inputFileError : String = """
            ${Colors.BOLD + Colors.RED}> ERROR
              ${Colors.RESET + Colors.BOLD}File in < input > doesn't exist. Input and output extension must be .blya or .lua.
              Check again the path to the file, and if it still turned out to be correct:
              Create issue on ${Data.REPOSITORY_URL + Colors.UNDERLINE + Colors.CYAN}/issues.
            ${Colors.RESET + Colors.BOLD}> INPUT PATH
              ${args[0]}
            > OUTPUT PATH 
              ${args[1]}
        """.trimIndent()

        if (args[0] == "-h" || args[0] == "--help") {
            println(helpContent)
        } else {
            println("${Colors.BOLD}> BidLua ${Colors.YELLOW}( ${Data.VERSION} )\n")
            if (Charset.availableCharsets()[flags["--charset"]] != null) {
                val inputExtension : String = File(args[0]).extension
                val outputExtension : String = File(args[1]).extension
                val acceptableExtensions : List<String> = listOf("blya", "lua")
                if (File(args[0]).exists() && inputExtension in acceptableExtensions && outputExtension in acceptableExtensions) {
                    Data.compiling = true
                    compile (
                        File(args[0]),
                        File(args[1]),
                        Charset.availableCharsets()[flags["--charset"]]!!,
                        flags["--map"]!!
                    )
                } else {
                    println(inputFileError)
                }
            } else {
                println("""
                    ${Colors.BOLD + Colors.RED}> ERROR
                      ${Colors.RESET + Colors.BOLD}Unknown charset "${Colors.CYAN + flags["--charset"]}${Colors.RESET + Colors.BOLD}"
                """.trimIndent())
            }
        }
    }
}