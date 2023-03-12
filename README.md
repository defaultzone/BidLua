### BidLua - компилируемый в Lua язык программирования c больше сотнью именами для "четких" парней.
***
**Пример:** Этот **.blya** файл, со следующим содержимым ...
```
вася
    петух kto будет "курьером"
    длялоха i жыесть 1, 5 паши
        print(kto)
    буээ 
чмо
```
... скомпилируется в **.lua** файл:
```lua
do
    local kto = "курьером"
    for i = 1, 5 do
        print(kto)
    end
end 
```

***
### Сборка ( .jar архива ) и компиляция ( .blya файла )
```shell
cd /src/main/kotlin/
kotlinc * -include-runtime -d Output.jar
java -jar Output.jar input.blya output.lua [ flags ]
```
**Доступные флаги:**
```shell
--charset=value         :: < value > is charset to input, output and map files.
--map=value             :: < value > is path to your file.map.blya.
--stop-on-error         :: Stop compiling on get error.
--ignore-default-map    :: Ignore BidLua default map.
```
***
### BidLua поддерживает пользовательские имена
```
file.map.blya
Все, что Вы пишите здесь является комментарием, за исключением SET функций.

Использование:
SET привет_мир::print("Hello, world!"); Это позволит Вам использовать ...
    ... привет_мир как имя, и при компиляции вернется print("Hello, world")
SET пока_мир::print("Bye, world!");
Синтаксис строгий и неизменяемый: SET key::value; ( это однако выполнится, ...
... выводы делайте сами )
```
Чтобы скомпилировать Ваш **file.blya** c Вашими именами, выполните компиляцию с флагом `--map=/path/to/your/file.map.blya`
.
***
Проект залицензирован под **GNU General Public License** третьей версии. [Узнайте больше.](https://github.com/defaultzon3/BidLua/blob/main/LICENSE)
**BidLua Copyright (C) 2023 defaultzon3**
