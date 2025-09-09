package io.github.patbattb.moderant;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class App {
    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser();
        String botToken = argsParser.getBotToken(args);
        Bot bot = new Bot(botToken);
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
            tgApp.registerBot(botToken, bot);
            Thread.currentThread().join(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}