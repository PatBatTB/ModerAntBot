package io.github.patbattb.moderant;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class App {
    public static void main(String[] args) {
        //TODO парсинг настроек из файла конфига
        ArgsParser argsParser = new ArgsParser();
        String botToken = argsParser.getBotToken(args);
        //TODO создание объектов-топиков с ограничениями.
        Parameters.init();
        Bot bot = new Bot(botToken);
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
            tgApp.registerBot(botToken, bot);
            Thread.currentThread().join(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}