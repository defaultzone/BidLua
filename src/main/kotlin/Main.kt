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
import Data.inputFileExtension
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
                when (splitFlag.size) {
                    1 -> { flags[key] = "true" }
                    2 -> { flags[key] = splitFlag[1] }
                }
            }
        }
    }

    val helpContent : String = """
        BidLua ( ${Data.VERSION} ) compiler to Lua usage:
            java -jar < path to .jar file > < input || -h || --help > < output > [ flags ]
        Flags:
            --charset=value         :: < value > is charset to input, output and map files.
            --map=value             :: < value > is path to your file.map.blya.
            --stop-on-error         :: Stop compiling on get error.
            --ignore-default-map    :: Ignore BidLua default map.
        BidLua Copyright (C) 2023 defaultzon3 (also known as DZONE)
            This program comes with ABSOLUTELY NO WARRANTY;
            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.
        Source code: https://github.com/defaultzon3/BidLua/
    """.trimIndent()

    if (args.isEmpty()) {
        println(helpContent)
    } else {
        if (args[0] == "-h" || args[0] == "--help") {
            println(helpContent)
        } else {
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
                    println("File in < input > doesn't exist.")
                    println("Input and output extension must be \".blya\" or \".lua\".")
                    println("Check again the path to the file, and if it still turned out to be correct - create issue ...")
                    println("... on ${Data.REPOSITORY_URL}/issues.")
                    println("< input > path: ${args[0]}; < output > path: ${args[1]}")
                }
            } else {
                println("Unknown charset: ${flags["--charset"]}")
            }
        }
    }
}