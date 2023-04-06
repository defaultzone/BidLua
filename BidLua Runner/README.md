## BidLua Runner

Компилирует и выполняет Lua скрипты в игре **GTA: San Andreas Multiplayer**. Построено с изпользованием **Moonloader API**.
Чтобы начать, положите этот скрипт в папку с moonloader-ом, создайте файл BidLuaRunner.suka.blya со следующим устройством:
```bash
send::
  input=/path/to/file.blya
  output=/path/to/file.lua
  flags=[--some-flag=value;--some-bool-flag] # Флаги разделяются через символ ";".
::send
```
Чтобы вводить путь, можно использовать следующие переменные:
```bash
$WORKING_DIR        # getWorkingDirectory()
$GAME_DIR           # getGameDirectory()
$APPDATA_DIR        # getFolderPath(0x23)
$DESKTOP_DIR        # getFolderPath(0x19)
$MYDOCUMENTS_DIR    # getFolderPath(0x05)
```
BidLua Runner использует последнию версию BidLua - 1.1.1. Чтобы использовать скрипт также необходимо положить `.jar` архив
в папку со скриптом. Требует наличие Java.
BidLua Runner работает только на Windows.
