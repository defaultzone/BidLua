### BidLua - компилируемый в Lua язык программирования c сотнью именами и функций для "четких" парней.
***
**Пример:** Этот `.blya` файл, со следующим содержимым ...
```
вася
    петух kto будет "курьером"
    длялоха i жыесть 1, 5 паши
        print(kto)
    буээ 
чмо
```
... скомпилируется в `.lua` файл:
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
Компилировать можно как и `.lua` файл в `.blya` файл, так и наоборот.
***
### BidLua поддерживает пользовательские имена
Это значит, что Вы можете использовать свои имена, вместо имен которые встроены в BidLua по умолчанию. 
Для начала, создайте где-нибудь `file.map.blya` со следующим содержимым( ниже пример, вы можете писать в Вашем файле все что угодно. ):
```
file.map.blya
Все, что Вы пишите здесь является комментарием, за исключением SET функций.

Использование:
SET привет_мир::print("Hello, world!"); Это позволит Вам использовать ...
    ... привет_мир как имя, и при компиляции вернется print("Hello, world")
SET пока_мир::print("Bye, world!");
```
Чтобы скомпилировать Ваш `file.map.blya`, добавьте флаг `--map=/path/to/your/file.map.blya` при компиляции.
***
Проект залицензирован под **GNU General Public License** третьей версии. [Узнайте больше.](https://github.com/defaultzon3/BidLua/blob/main/LICENSE)
**BidLua Copyright (C) 2023 defaultzon3**
