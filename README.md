<div align="center">

# ⚔️ SUDORUDialoges
### Диалоговая Торговая Система для Minecraft 1.21.x (Paper)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-brightgreen?style=for-the-badge&logo=minecraft)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/API-Paper-blue?style=for-the-badge)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

> Красивая торговая система с диалоговым стилем меню, случайным ассортиментом и гибкой настройкой — без NPC!

</div>

---

## ✨ Особенности

| Функция | Описание |
|---|---|
| 🚫 Без NPC | Меню открывается командой `/trader <имя>` |
| 🎲 Случайный ассортимент | Каждый торговец имеет **5–8 предметов** с настраиваемыми шансами |
| 🔁 Дубликаты | Один и тот же предмет может выпасть **2–3 раза** одновременно |
| 🛑 Блокировка слота | Купленный товар → **барьер** (нельзя купить повторно) |
| ⏱️ Автообновление | Ассортимент обновляется автоматически через заданное время |
| 💎 Предметная валюта | По умолчанию — **изумруды**, легко меняется в конфиге |
| 📝 Полная настройка | Все параметры в `config.yml` без изменения кода |
| ➕ Несколько торговцев | Создавай **сколько угодно** торговцев с разными товарами |

---

## 🖼️ Скриншот меню

```
╔═══════════════════════════════════════════════╗
║  [▒] [▒] [▒] [ ⚒ Кузнец ] [▒] [▒] [▒] [▒]  ║  ← Шапка с именем торговца
║  [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒]   ║  ← Разделитель
╠═══════════════════════════════════════════════╣
║  [▒] [⚔️] [▒] [⛏️] [▒] [🛡️] [▒] [💎] [▒]   ║  ← Товары
║  [✔] [▒] [✔] [▒] [✗] [▒] [✔] [▒] [⚔️]      ║  ← Кнопки покупки + 5й товар
║  [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒] [▒]   ║  ← Пустая строка
╠═══════════════════════════════════════════════╣
║  [▒] [▒] [▒] [▒] [⏱️] [▒] [▒] [▒] [✖]      ║  ← Таймер + Кнопка закрыть
╚═══════════════════════════════════════════════╝
```
- `✔` — кнопка «КУПИТЬ»
- `✗` — барьер (товар куплен)
- `⏱️` — время до следующего обновления

---

## 📦 Установка

1. Скачай последний релиз `SUDORUDialoges-1.0-SNAPSHOT.jar` из [Releases](../../releases)
2. Скопируй в папку `plugins/` сервера
3. Запусти / перезапусти сервер
4. Конфиг создастся автоматически: `plugins/SUDORUDialoges/config.yml`

> **Требования:** Paper 1.21.x, Java 21+

---

## 🎮 Команды

| Команда | Описание | Право |
|---|---|---|
| `/trader` | Список всех торговцев | `sudoru.trader` |
| `/trader <имя>` | Открыть меню торговца | `sudoru.trader` |
| `/traderreload` | Перезагрузить конфиг | `sudoru.trader.reload` |

**Алиасы:** `/trade`, `/shop`

---

## 🔑 Права доступа

| Право | Кому выдано | Описание |
|---|---|---|
| `sudoru.trader` | Все игроки | Открывать меню торговцев |
| `sudoru.trader.reload` | OP | Перезагружать конфигурацию |
| `sudoru.trader.bypass` | OP | Покупать бесплатно (режим отладки) |

---

## ⚙️ Конфигурация

### Валюта
```yaml
currency:
  type: ITEM            # Тип: ITEM (предмет из инвентаря)
  item-material: EMERALD
  item-name: "Изумруд"
```

### Структура торговца
```yaml
traders:
  my_trader:                              # ID — используется в /trader <ID>
    display-name: "&6✦ Мой торговец"     # Поддержка &-кодов цветов
    description: "&7«Описание торговца»"
    icon-material: CHEST                  # Material иконки в шапке меню
    refresh-seconds: 300                  # Обновление ассортимента (0 = выключено)
    min-items: 5                          # Минимум товаров в ассортименте
    max-items: 8                          # Максимум товаров в ассортименте
    items:
      - material: DIAMOND_SWORD
        name: "&bАлмазный меч"
        lore:
          - "&7Острый как правда."
        price: 10           # Базовая цена
        price-range: 3      # Разброс ±3 → итог: 7–13 изумрудов
        chance: 40.0        # Вес при случайной выборке
        amount: 1           # Количество в стаке
        enchantments: []    # Зарезервировано
```

### Зелья
Для предметов типа `POTION` используй дополнительное поле:
```yaml
      - material: POTION
        name: "&aЗелье лечения II"
        potion-type: STRONG_HEALING    # Тип зелья из PotionType
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
`STRONG_LEAPING`, `POISON`, `WEAKNESS`, `SLOWNESS`, `SLOWNESS`

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
├── SUDORUDialoges.java           — Главный класс, логика валюты
├── command/
│   ├── ShopCommand.java          — /trader + tab-complete
│   └── ReloadCommand.java        — /traderreload
├── listener/
│   └── ShopMenuListener.java     — Обработка кликов GUI
└── shop/
    ├── ShopItem.java             — Модель предмета (цена, шанс, lore)
    ├── TraderConfig.java         — Конфиг одного торговца
    ├── TraderManager.java        — Загрузка config.yml
    └── TraderShop.java           — GUI, покупка, таймер обновления
```

---

## 🔨 Сборка из исходников

Требуется: **Java 21**, **Maven 3.8+**

```bash
git clone https://github.com/ТВОЙ_НИКНЕЙМ/SUDORUDialoges.git
cd SUDORUDialoges
mvn clean package
```

JAR будет в `target/SUDORUDialoges-1.0-SNAPSHOT.jar`

---

## 📄 Лицензия

Распространяется под лицензией **MIT** — используй, модифицируй, делись свободно.

---

<div align="center">
Сделано с ❤️ командой <b>SUDORU</b>
</div>

