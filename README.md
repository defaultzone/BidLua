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
### Сборка ( .jar архива ) и компилиция ( .blya файла )
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
--dont-use-default-map  :: Ignore BidLua default map.
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
Синтаксис строгий и неизменяемый: SET key::value; ( это, однако, выполнится ...
... выводы делайте сами )
```
Чтобы скомпилировать Ваш **file.blya** c Вашими именами, выполните компиляцию с флагом `--map=/path/to/your/file.map.blya`
.
***
### BidLua - свободная программа под GNU General Public License version 3
Это означает, что вы имеете четыре свободы ( которые должны быть у каждого пользователя ):

- свобода применять программу в любых целях,
- свобода дорабатывать программу под свои нужды,
- свобода обмениваться программой со своими друзьями и соседями,
- свобода обмениваться изменениями, которые вы внесли.

Никто не должен быть ограничен программами, которыми пользуется. При "дистрибьютции" Вы должны использовать **GNU General Public License** версии 3 ( или, ниже - на Ваш выбор. ). Текст лицензии присутствует в этом репозитории: **[Открыть](https://github.com/defaultzon3/BidLua/blob/main/LICENSE)**, а также на официальном сайте **GNU: https://www.gnu.org/licenses/**.
***
**BidLua Copyright (C) 2023 defaultzon3**