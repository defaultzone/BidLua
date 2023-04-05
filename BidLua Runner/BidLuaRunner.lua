script_name         "BidLua Runner"
script_version      "1.0"
script_author       "DZONE"
script_description  "Execute .blya files directly in the game using BidLua compiler."

-- This script is part of the BidLua project, which is compiled into the Lua language.
-- BidLua repository URL: https://github.com/defaultzon3/BidLua
-- BidLua licensed under GNU General Public License v3.
-- For information about the License, see < https://www.gnu.org/licenses/ >

require("moonloader")

function main()
    while not isSampAvailable() do wait(0) end

    while true do
        wait(0)
        if doesFileExist(getWorkingDirectory() .. "\\BidLuaRunner.suka.blya") and doesFileExist(getWorkingDirectory() .. "\\BidLua-1.1.1.jar") then
            local handle = assert(io.open(getWorkingDirectory() .. "\\BidLuaRunner.suka.blya", "r"))
            local runnerConfig = handle:read("*all")

            for input, output, flags in string.gmatch(runnerConfig, "send.*::.*\n.*input.*=(.*)\n.*output.*=(.*)\n.*flags.*=%[(.*)%].*\n::send") do
                local replacePathVariables = function(string)
                    local finalString = string
                    local pathVariables = {
                        ["$WORKING_DIR"]         = getWorkingDirectory(),
                        ["$GAME_DIR"]            = getGameDirectory(),
                        ["$APPDATA_DIR"]         = getFolderPath(0x23),
                        ["$DESKTOP_DIR"]         = getFolderPath(0x19),
                        ["$MYDOCUMENTS_DIR"]     = getFolderPath(0x05)
                    }

                    for key, value in pairs(pathVariables) do 
                        finalString = string.gsub(finalString, "%" .. key, value)
                    end

                    return finalString
                end

                if doesFileExist(replacePathVariables(output)) or not doesFileExist(replacePathVariables(input)) then
                    break
                end

                local finalCommand = string.format(
                    "java -jar %s %s %s ",
                    getWorkingDirectory() .. "\\BidLua-1.1.1.jar",
                    replacePathVariables(input),
                    replacePathVariables(output)
                )

                for flag in string.gmatch(flags, "%-%-(.*);") do
                    if string.find(flag, "charset=.*;") then
                        finalCommand = finalCommand .. "--charset=" .. string.match(flag, ".*=(.*)")
                    elseif string.find(flag, "map=.*;") then
                        finalCommand = finalCommand .. "--map=" .. replacePathVariables(string.match(flag, ".*=(.*)"))
                    elseif flag == "stop-on-error" then
                        finalCommand = finalCommand .. flag
                    elseif flag == "ignore-default-map" then
                        finalCommand = finalCommand .. flag
                    end
                end

                os.execute(finalCommand)
                lua_thread.create(function()
                    wait(5000)
                    if doesFileExist(replacePathVariables(input)) then
                        script.load(replacePathVariables(input))
                    end
                end)
            end

            handle:close()
            break
        end
    end
end
