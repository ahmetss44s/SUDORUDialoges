<div align="center">

# ⚔️ SUDORUDialoges
### Диалоговая Торговая Система для Minecraft 1.21.x (Paper)

[![Version](https://img.shields.io/badge/Версия-1.1.7-gold?style=for-the-badge)](https://github.com/ahmetss44s/SUDORUDialoges/releases/tag/v1.1.7)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.5+-brightgreen?style=for-the-badge&logo=minecraft)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/API-Paper%201.21.8-blue?style=for-the-badge)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

> Торговая система с **нативным диалоговым меню** Minecraft, случайным ассортиментом и полной GUI-настройкой — без NPC!

</div>

---

## 🖼️ Вид меню торговца

Меню открывается прямо в игровом мире — тёмный оверлей с кнопками по центру экрана (нативный Dialog API Paper 1.21.5+):

```
        ┌─────────────────────────────┐
        │   ⚒ Таинственный Кузнец     │  ← display-name торговца
        │                             │
        │  «Добро пожаловать.         │  ← description из config.yml
        │   Выбирай товар...»         │
        │                             │
        │  [ #1 Алмазный меч — 10 💎 ] │  ← доступный товар
        │  [ #2 Алмазная кирка — 12💎] │
        │  [ #3 Железный меч — 4 💎  ] │
        │  [ ✗ #4 Щит — ПРОДАНО      ] │  ← уже куплен
        │  [ #5 Незеритовый слиток 30] │
        │                             │
        │         [ Выход ]           │  ← закрыть
        └─────────────────────────────┘
```

- **Наведи курсор** на кнопку — появится тултип с лором, ценой и количеством
- **Клик** — покупка; если недостаточно валюты — сообщение в чат
- После покупки диалог **автоматически обновляется** (купленный слот становится серым «ПРОДАНО»)

---

## ✨ Особенности

| Функция | Описание |
|---|---|
| 💬 Нативный Dialog API | Меню — тёмный оверлей с кнопками |
| 🤝 Datapack + Plugin | Игровое меню ведет datapack, плагин дает bridge-команды и admin-GUI |
| 🎲 Случайный ассортимент | Каждый торговец имеет **5–8 предметов** с настраиваемыми шансами |
| 🔁 Дубликаты | Один и тот же предмет может выпасть **2–3 раза** одновременно |
| 🛑 Блокировка покупки | Купленный товар → серый «ПРОДАНО» (нельзя купить повторно) |
| ⏱️ Автообновление | Ассортимент обновляется автоматически через заданное время |
| 💎 Предметная валюта | По умолчанию — **изумруды**, легко меняется в конфиге |
| 🎨 HEX цвета | Поддержка `&#RRGGBB` и `#RRGGBB` во всём интерфейсе |
| 🛠️ GUI-настройки | Все параметры `config.yml` редактируются прямо в игре |
| 📊 PlaceholderAPI | Плейсхолдеры `%sudoru_*%` для других плагинов |
| ➕ Несколько торговцев | Создавай **сколько угодно** торговцев с разными товарами |

---

## 📦 Установка

1. Скачай последний релиз [`SUDORUDialoges-1.0.5.jar`](https://github.com/ahmetss44s/SUDORUDialoges/releases/tag/v1.0.5) из [Releases](../../releases)
2. Скопируй в папку `plugins/` сервера
3. Запусти / перезапусти сервер
4. Конфиг создастся автоматически: `plugins/SUDORUDialoges/config.yml`

> **Требования:** Paper **1.21.5+** (Dialog API), Java 21+
> **Опционально:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) для плейсхолдеров

---

## 📋 Команды

### 👤 Игровые команды (доступны всем)

| Команда | Псевдонимы | Разрешение | Описание |
|---|---|---|---|
| `/trader` | `/trade` | `sudoru.trader` | Показать список доступных торговцев |
| `/trader <id>` | `/trade <id>` | `sudoru.trader` | Открыть диалоговое меню торговца по его ID |
| `/sellshop` | `/sell`, `/selltrade` | `sudoru.sellshop` | Открыть меню продажи предметов за валюту |

---

### 🔧 Администраторские команды (только OP)

| Команда | Псевдонимы | Разрешение | Описание |
|---|---|---|---|
| `/tradermenu` | `/trmenu`, `/shopeditor` | `sudoru.trader.admin` | Открыть GUI-редактор всех торговцев |
| `/tradermenu <id>` | | `sudoru.trader.admin` | Открыть редактор конкретного торговца |
| `/traderconfig` | `/tconfig`, `/shopconfig` | `sudoru.trader.admin` | Открыть GUI-меню настроек (`config.yml`) |
| `/traderconfig currency` | | `sudoru.trader.admin` | Перейти прямо на страницу настройки валюты |
| `/traderconfig trader <id>` | | `sudoru.trader.admin` | Открыть настройки конкретного торговца |
| `/traderconfig items <id>` | | `sudoru.trader.admin` | Открыть редактор предметов торговца |
| `/traderreload` | `/tradereload` | `sudoru.trader.reload` | Перезагрузить `config.yml` и всех торговцев |
| `/villageres create <id> [имя]` | `/svillager`, `/sudorunpc` | `sudoru.villageres.admin` | Заспавнить NPC-жителя, привязанного к торговцу |

---

### ⚙️ Bridge-команды (внутренние, вызываются датапаком)

> ⚠️ Эти команды предназначены для связки **датапак ↔ плагин**. Игроки не вызывают их вручную — они запускаются из диалога или датапака.

| Команда | Разрешение | Описание |
|---|---|---|
| `/shopbridge open <traderId>` | `sudoru.trader.bridge` | Синкать ассортимент и открыть диалог торговца |
| `/shopbridge buy <число>` | `sudoru.trader.bridge` | Обработать покупку (число = ShopID×100 + slotIndex) |
| `/shopbridge sell <число>` | `sudoru.trader.bridge` | Обработать продажу через датапак-триггер |

---

### 🔑 Таблица разрешений

| Разрешение | По умолчанию | Описание |
|---|---|---|
| `sudoru.trader` | ✅ Все | Открывать меню торговцев |
| `sudoru.sellshop` | ✅ Все | Открывать меню продажи |
| `sudoru.trader.bridge` | ✅ Все | Выполнять bridge-команды (нужно для кнопок диалога) |
| `sudoru.trader.admin` | 🔑 OP | Редактировать торговцев через GUI |
| `sudoru.trader.reload` | 🔑 OP | Перезагружать конфиг |
| `sudoru.villageres.admin` | 🔑 OP | Создавать NPC-торговцев |
| `sudoru.trader.bypass` | ❌ Никто | Бесплатная покупка (обход проверки валюты) |

---

## 🖼️ Вид меню торговца

Меню открывается прямо в игровом мире — тёмный оверлей с кнопками по центру экрана (нативный Dialog API Paper 1.21.5+):

```
        ┌─────────────────────────────┐
        │   ⚒ Таинственный Кузнец     │  ← display-name торговца
        │                             │
        │  «Добро пожаловать.         │  ← description из config.yml
        │   Выбирай товар...»         │
        │                             │
        │  [ #1 Алмазный меч — 10 💎 ] │  ← доступный товар
        │  [ #2 Алмазная кирка — 12💎] │
        │  [ #3 Железный меч — 4 💎  ] │
        │  [ ✗ #4 Щит — ПРОДАНО      ] │  ← уже куплен
        │  [ #5 Незеритовый слиток 30] │
        │                             │
        │         [ Выход ]           │  ← закрыть
        └─────────────────────────────┘
```

- **Наведи курсор** на кнопку — появится тултип с лором, ценой и количеством
- **Клик** — покупка; если недостаточно валюты — сообщение в чат
- После покупки диалог **автоматически обновляется** (купленный слот становится серым «ПРОДАНО»)

---

## ✨ Особенности

| Функция | Описание |
|---|---|
| 💬 Нативный Dialog API | Меню — тёмный оверлей с кнопками |
| 🤝 Datapack + Plugin | Игровое меню ведет datapack, плагин дает bridge-команды и admin-GUI |
| 🎲 Случайный ассортимент | Каждый торговец имеет **5–8 предметов** с настраиваемыми шансами |
| 🔁 Дубликаты | Один и тот же предмет может выпасть **2–3 раза** одновременно |
| 🛑 Блокировка покупки | Купленный товар → серый «ПРОДАНО» (нельзя купить повторно) |
| ⏱️ Автообновление | Ассортимент обновляется автоматически через заданное время |
| 💎 Предметная валюта | По умолчанию — **изумруды**, легко меняется в конфиге |
| 🎨 HEX цвета | Поддержка `&#RRGGBB` и `#RRGGBB` во всём интерфейсе |
| 🛠️ GUI-настройки | Все параметры `config.yml` редактируются прямо в игре |
| 📊 PlaceholderAPI | Плейсхолдеры `%sudoru_*%` для других плагинов |
| ➕ Несколько торговцев | Создавай **сколько угодно** торговцев с разными товарами |

---

## 📦 Установка

1. Скачай последний релиз [`SUDORUDialoges-1.0.5.jar`](https://github.com/ahmetss44s/SUDORUDialoges/releases/tag/v1.0.5) из [Releases](../../releases)
2. Скопируй в папку `plugins/` сервера
3. Запусти / перезапусти сервер
4. Конфиг создастся автоматически: `plugins/SUDORUDialoges/config.yml`

> **Требования:** Paper **1.21.5+** (Dialog API), Java 21+
> **Опционально:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) для плейсхолдеров

---

## 📋 Команды

### 👤 Игровые команды (доступны всем)

| Команда | Псевдонимы | Разрешение | Описание |
|---|---|---|---|
| `/trader` | `/trade` | `sudoru.trader` | Показать список доступных торговцев |
| `/trader <id>` | `/trade <id>` | `sudoru.trader` | Открыть диалоговое меню торговца по его ID |
| `/sellshop` | `/sell`, `/selltrade` | `sudoru.sellshop` | Открыть меню продажи предметов за валюту |

---

### 🔧 Администраторские команды (только OP)

| Команда | Псевдонимы | Разрешение | Описание |
|---|---|---|---|
| `/tradermenu` | `/trmenu`, `/shopeditor` | `sudoru.trader.admin` | Открыть GUI-редактор всех торговцев |
| `/tradermenu <id>` | | `sudoru.trader.admin` | Открыть редактор конкретного торговца |
| `/traderconfig` | `/tconfig`, `/shopconfig` | `sudoru.trader.admin` | Открыть GUI-меню настроек (`config.yml`) |
| `/traderconfig currency` | | `sudoru.trader.admin` | Перейти прямо на страницу настройки валюты |
| `/traderconfig trader <id>` | | `sudoru.trader.admin` | Открыть настройки конкретного торговца |
| `/traderconfig items <id>` | | `sudoru.trader.admin` | Открыть редактор предметов торговца |
| `/traderreload` | `/tradereload` | `sudoru.trader.reload` | Перезагрузить `config.yml` и всех торговцев |
| `/villageres create <id> [имя]` | `/svillager`, `/sudorunpc` | `sudoru.villageres.admin` | Заспавнить NPC-жителя, привязанного к торговцу |

---

### ⚙️ Bridge-команды (внутренние, вызываются датапаком)

> ⚠️ Эти команды предназначены для связки **датапак ↔ плагин**. Игроки не вызывают их вручную — они запускаются из диалога или датапака.

| Команда | Разрешение | Описание |
|---|---|---|
| `/shopbridge open <traderId>` | `sudoru.trader.bridge` | Синкать ассортимент и открыть диалог торговца |
| `/shopbridge buy <число>` | `sudoru.trader.bridge` | Обработать покупку (число = ShopID×100 + slotIndex) |
| `/shopbridge sell <число>` | `sudoru.trader.bridge` | Обработать продажу через датапак-триггер |

---

### 🔑 Таблица разрешений

| Разрешение | По умолчанию | Описание |
|---|---|---|
| `sudoru.trader` | ✅ Все | Открывать меню торговцев |
| `sudoru.sellshop` | ✅ Все | Открывать меню продажи |
| `sudoru.trader.bridge` | ✅ Все | Выполнять bridge-команды (нужно для кнопок диалога) |
| `sudoru.trader.admin` | 🔑 OP | Редактировать торговцев через GUI |
| `sudoru.trader.reload` | 🔑 OP | Перезагружать конфиг |
| `sudoru.villageres.admin` | 🔑 OP | Создавать NPC-торговцев |
| `sudoru.trader.bypass` | ❌ Никто | Бесплатная покупка (обход проверки валюты) |

---

## 📊 PlaceholderAPI

Доступны после установки [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/):

| Плейсхолдер | Возвращает |
|---|---|
| `%sudoru_currency%` | Кол-во валюты у игрока |
| `%sudoru_currency_name%` | Название валюты |
| `%sudoru_traders_count%` | Количество загруженных торговцев |
| `%sudoru_trader_<id>_name%` | Имя торговца |
| `%sudoru_trader_<id>_items%` | Предметов в пуле |
| `%sudoru_trader_<id>_refresh%` | Время обновления |

Пример: `%sudoru_trader_blacksmith_name%` → `⚒ Таинственный Кузнец`

---

## ⚙️ Конфигурация

### Валюта
```yaml
currency:
  type: ITEM            # ITEM — предмет из инвентаря
  item-material: EMERALD
  item-name: "Изумруд"
```

### Структура торговца
```yaml
traders:
  my_trader:                              # ID — используется в /trader <ID>
    display-name: "&6✦ Мой торговец"     # Поддержка &-кодов и &#HEX
    description: "&7«Описание торговца.\n&7Здесь можно купить товары.»"
    icon-material: CHEST                  # Иконка в GUI-редакторе
    refresh-seconds: 300                  # Обновление ассортимента (0 = выключено)
    min-items: 5                          # Минимум товаров в ассортименте
    max-items: 8                          # Максимум товаров в ассортименте
    items:
      - material: DIAMOND_SWORD
        name: "&#FFD700Алмазный меч"     # HEX цвет
        lore:
          - "&7Острый как правда."
        price: 10           # Базовая цена
        price-range: 3      # Разброс ±3 → итог: 7–13 изумрудов
        chance: 40.0        # Вес при случайной выборке (чем больше — чаще)
        amount: 1           # Количество в стаке
        enchantments: []
```

### Зелья
```yaml
      - material: POTION
        name: "&aЗелье лечения II"
        potion-type: STRONG_HEALING
        price: 5
        price-range: 1
        chance: 50.0
        amount: 1
        lore:
          - "&7Исцеляет раны."
        enchantments: []
```

<details>
<summary>📋 Список доступных potion-type</summary>

`HEALING`, `STRONG_HEALING`, `SWIFTNESS`, `STRONG_SWIFTNESS`, `STRENGTH`,
`STRONG_STRENGTH`, `INVISIBILITY`, `NIGHT_VISION`, `WATER_BREATHING`,
`REGENERATION`, `STRONG_REGENERATION`, `FIRE_RESISTANCE`, `LEAPING`,
`STRONG_LEAPING`, `POISON`, `WEAKNESS`, `SLOWNESS`

</details>

---

## 🗃️ Встроенные торговцы

| ID | Название | Предметов в пуле | Обновление |
|---|---|---|---|
| `blacksmith` | ⚒ Таинственный Кузнец | 8 | 5 минут |
| `alchemist` | ⚗ Таинственный Зельевар | 7 | 3 минуты |
| `relic` | ✦ Торговец Реликвиями | 6 | 10 минут |

---

## 🏗️ Структура проекта

```
src/main/java/.../
├── SUDORUDialoges.java                — Главный класс, логика валюты
├── command/
│   ├── ShopCommand.java               — /trader + tab-complete
│   ├── ReloadCommand.java             — /traderreload
│   ├── TraderMenuCommand.java         — /tradermenu [id]
│   └── ConfigMenuCommand.java         — /traderconfig [currency|trader|items]
├── dialog/
│   └── TraderDialogMenu.java          — Нативное диалоговое меню (Paper Dialog API)
├── listener/
│   ├── ShopMenuListener.java          — Клики в инвентарном меню (admin-GUI)
│   ├── MenuEditorListener.java        — Клики в /tradermenu
│   └── ConfigMenuListener.java        — Клики в /traderconfig + ввод в чат
├── menu/
│   ├── TraderMenuGUI.java             — GUI панели управления торговцами
│   └── ConfigMenuGUI.java             — GUI полных настроек config.yml
├── placeholder/
│   └── TraderPlaceholder.java         — PlaceholderAPI %sudoru_*%
├── shop/
│   ├── ShopItem.java                  — Модель предмета (цена, шанс, lore)
│   ├── TraderConfig.java              — Конфиг одного торговца
│   ├── TraderManager.java             — Загрузка config.yml
│   └── TraderShop.java                — Ассортимент, покупка, таймер обновления
└── util/
    └── ColorUtil.java                 — HEX и &-коды цветов
```

---

## 🔨 Сборка из исходников

Требуется: **Java 21**, **Maven 3.8+**, **Paper 1.21.5+**

```bash
git clone https://github.com/ahmetss44s/SUDORUDialoges.git
cd SUDORUDialoges
mvn clean package
```

JAR будет в `target/SUDORUDialoges-1.0.2.jar`

---

## 📄 Лицензия

Распространяется под лицензией **MIT** — используй, модифицируй, делись свободно.

---

<div align="center">
Сделано с ❤️ командой <b>SUDORU</b>
</div>
