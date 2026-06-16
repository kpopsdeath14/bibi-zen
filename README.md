# bibi-zen

Telegram-магазин. Состоит из двух частей:

- **bibi-zen-back** — бэкенд на Clojure (http-kit, compojure, Ring). REST API + взаимодействие с Telegram Bot API.
- **bibi-zen-front** — фронтенд на ClojureScript (Reagent, shadow-cljs). SPA для отображения каталога и оформления заказов.

## Запуск

**Бэкенд:**
```bash
cd bibi-zen-back
lein run
```

**Фронтенд:**
```bash
cd bibi-zen-front
npm install
npx shadow-cljs watch app
```

## Конфигурация

Создай `bibi-zen-back/app_state.edn` и `bibi-zen-front/public/config.edn` с параметрами бота и БД (см. структуру в коде).
