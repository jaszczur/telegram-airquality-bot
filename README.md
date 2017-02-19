# Telegram Air Quality Bot

Bot returns current air quality data. It currently supports only Polish language and [official stations](http://powietrze.gios.gov.pl/).
Bot is available at http://t.me/pl_airquality_bot. Or might be as it is under development :)

## Libraries used

It uses Jersey to access REST API, JSON Processing API to parse the result, java-telegram-bot-api for Telegram integration and RxJava 2 for internal flow of data.
