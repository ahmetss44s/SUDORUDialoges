<div align="center">

# ⚔️ SUDORUDialoges
### Диалоговая Торговая Система для Minecraft 1.21.x (Paper)

[![Version](https://img.shields.io/badge/Версия-1.0.4-gold?style=for-the-badge)](https://github.com/ahmetss44s/SUDORUDialoges/releases/tag/v1.0.4)
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
| 💬 Нативный Dialog API | Меню — тёмный оверлей с кнопками, как в Screenshot выше |
| 🚫 Без NPC | Меню открывается командой `/trader <имя>` |
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

1. Скачай последний релиз [`SUDORUDialoges-1.0.4.jar`](https://github.com/ahmetss44s/SUDORUDialoges/releases/tag/v1.0.4) из [Releases](../../releases)
2. Скопируй в папку `plugins/` сервера
3. Запусти / перезапусти сервер
4. Конфиг создастся автоматически: `plugins/SUDORUDialoges/config.yml`

> **Требования:** Paper **1.21.5+** (Dialog API), Java 21+
> **Опционально:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) для плейсхолдеров

---

## 🎮 Команды

### 👤 Для игроков

| Команда | Алиасы | Описание | Право |
|---|---|---|---|
| `/trader` | `/trade`, `/shop` | Показать список всех торговцев | `sudoru.trader` |
| `/trader <имя>` | `/trade <имя>`, `/shop <имя>` | Открыть диалоговое меню торговца | `sudoru.trader` |

---

### 🛠️ Для администраторов

| Команда | Алиасы | Описание | Право |
|---|---|---|---|
| `/tradermenu` | `/trmenu`, `/shopeditor` | GUI-панель управления торговцами | `sudoru.trader.admin` |
| `/tradermenu <id>` | — | Открыть редактор конкретного торговца | `sudoru.trader.admin` |
| `/traderconfig` | `/tconfig`, `/shopconfig` | Полное GUI-меню всех настроек `config.yml` | `sudoru.trader.admin` |
| `/traderconfig currency` | — | Настройки валюты | `sudoru.trader.admin` |
| `/traderconfig trader <id>` | — | Настройки конкретного торговца | `sudoru.trader.admin` |
| `/traderconfig items <id>` | — | Список предметов торговца | `sudoru.trader.admin` |
| `/traderreload` | `/tradereload` | Перезагрузить `config.yml` без рестарта | `sudoru.trader.reload` |

---

## 🛠️ GUI-меню настроек (`/traderconfig`)

Полное GUI для редактирования всего `config.yml` прямо в игре:

```
/traderconfig
     │
     ├─ 💎  Валюта ──────── currency.type / item-name / item-material
     │
     ├─ ⚒  Торговец <id>
     │    ├─ display-name     ← ввод в чат (поддерживает &-коды и &#HEX)
     │    ├─ description      ← ввод в чат (\n для новой строки)
     │    ├─ icon-material    ← ввод в чат (пример: ANVIL)
     │    ├─ refresh-seconds  ← ЛКМ −60с | ПКМ +60с | Shift ±300с
     │    ├─ min-items        ← ЛКМ −1 | ПКМ +1
     │    ├─ max-items        ← ЛКМ −1 | ПКМ +1
     │    ├─ 📦 Предметы      ← открыть список предметов
     │    ├─ ⟳  Обновить ассортимент
     │    └─ ✗  Удалить торговца (Shift+ЛКМ)
     │
     └─ 📦  Предметы <id>  (пагинация)
            ├─ ЛКМ → редактор предмета
            ├─ ПКМ → удалить предмет
            ├─ ✚  Добавить предмет
            └─ ✎  Редактор предмета #N
                   ├─ material     ← ввод в чат
                   ├─ name         ← ввод в чат (&-коды и &#HEX)
                   ├─ price        ← ЛКМ −1 | ПКМ +1 | Shift ±10
                   ├─ price-range  ← ЛКМ −1 | ПКМ +1
                   ├─ chance       ← ЛКМ −5% | ПКМ +5% | Shift ±1%
                   ├─ amount       ← ЛКМ −1 | ПКМ +1
                   ├─ lore         ← ввод через | (строка1|строка2)
                   └─ potion-type  ← ввод в чат (только для зелий)
```

---

## 🔑 Права доступа

| Право | По умолчанию | Описание |
|---|---|---|
| `sudoru.trader` | Все игроки | Использовать `/trader` |
| `sudoru.trader.admin` | OP | `/tradermenu`, `/traderconfig` |
| `sudoru.trader.reload` | OP | `/traderreload` |
| `sudoru.trader.bypass` | OP | Покупать бесплатно (режим отладки) |

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
