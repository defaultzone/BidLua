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

object Data {
    const val VERSION : String = "0.1-PREVIEW"
    const val REPOSITORY_URL : String = "https://github.com/defaultzon3/BidLua"
    var compiling : Boolean = false
    var flags : MutableMap<String, String> = mutableMapOf(
        "--charset" to "UTF-8",
        "--map" to "null",
        "--stop-on-error" to "false",
        "--dont-use-default-map" to "false"
    )
}