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

import java.io.File
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import javax.crypto.IllegalBlockSizeException

private var outputPath = ""
private var mapPath = ""
private var ignoreDefaultCompilerMap = false
private var charset = "UTF-8"

fun main(args : Array<String>) {
    val helpContent : String = """
        usage: java -jar {path to BidLua.jar} [key] [file] [options...]
        
        key:
            -h, --help      display this
            --info          display info about BidLua
            -i FILE         compile just the file
            -I FILE         compile file that contains [options...]
        options:
            -m, --map FILE              allows you to use your map in the main file
            -n, --ignore-default-map    ignore default map
            -o, --output FILE           write write compiled code to specific file, by default it is output 
                                        file path without extension + .lua extension
            -c, --charset CHARSET       set charset to input, output and map files, by default it is UTF-8
    """.trimIndent()

    val parseArguments : (arguments : Array<String>) -> Unit = {
        val dotPosition : Int = it[1].lastIndexOf('.')
        outputPath = it[1].substring(0, dotPosition) + ".lua"

        for (i in it.indices) {
            if (it[i] == "-m" || it[i] == "--map") {
                if (i + 1 < it.size)
                    mapPath = it[i + 1]
                else throw IllegalArgumentException("Expected 'FILE' in setting map path. For more information, type '-h' or '--help'.")
            } else if (it[i] == "-o" || it[i] == "--output") {
                if (i + 1 < it.size)
                    outputPath = it[i + 1]
                else throw IllegalArgumentException("Expected 'FILE' in setting output path. For more information, type '-h' or '--help'.")
            } else if (it[i] == "-c" || it[i] == "--charset") {
                if (i + 1 < it.size)
                    charset = if (Charset.availableCharsets()[it[i + 1]] != null) it[i + 1]
                    else throw IllegalArgumentException("Unacceptable charset: ${it[i + 1]}")
                else
                    throw IllegalArgumentException("Expected 'CHARSET' in setting charset. For more information, type '-h' or '--help'.")
            }

            ignoreDefaultCompilerMap = it[i] == "-n" || it[i] == "--ignore-default-map"
        }
    }

    if (args.isNotEmpty()) {
        if (args[0] == "--info") {
            println("""
                BidLua Copyright (C) 2023 defaultzon3 (or just DZONE)
                    This program comes with ABSOLUTELY NO WARRANTY;
                    This program is free software: you can redistribute it and/or modify it under the
                    terms of the GNU General Public License as published by the Free Software Foundation,
                    either version 3 of the License, or (at your option) any later version.
                version:        ${Info.VERSION}
                repository url: ${Info.REPOSITORY_URL}
            """.trimIndent())
        } else if (args[0] == "-h" || args[0] == "--help") {
            println(helpContent)
        } else if (args[0] == "-i" /* Meaning compile just the file. */) {
            parseArguments(args)
        } else if (args[0] == "-I" /* Meaning compile just the file that contains program [options...] */) {
            if (args.size != 1) {
                val options : String =
                    File(if (File(args[1]).exists()) args[1] else throw IllegalArgumentException("File in input doesn't exist.")).readLines()[0]
                val optionsMatchResult : MatchResult? = Regex("^---@args:\\s+(.*)$").find(options)
                if (optionsMatchResult != null) {
                    val fileOptions : List<String> = optionsMatchResult.groupValues[1].split(' ')
                    if (options.isEmpty()) throw IllegalBlockSizeException("In fileOptions(content: '$fileOptions') no options. You can pass it by --@args ...")
                    parseArguments((listOf("-I", args[1]) + fileOptions).toTypedArray())
                } else throw IllegalArgumentException("First line in ${args[0]} doesn't have program options that passed by --@args ...")
            } else throw IllegalArgumentException("Expected 'FILE' after [key], but got nothing.")
        } else throw IllegalArgumentException("Illegal key: ${args[0]}")

        if (args.size != 1) {
            compile(
                File(if (File(args[1]).exists()) args[1] else throw IllegalArgumentException("File in input doesn't exist.")),
                File(outputPath),
                Charset.availableCharsets()[charset]!!,
                mapPath.takeIf { it != "" } ?: "null",
                ignoreDefaultCompilerMap
            )
        }
    } else {
        println(helpContent)
    }
}