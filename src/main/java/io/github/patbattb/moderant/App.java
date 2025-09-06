package io.github.patbattb.moderant;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class App {
    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser();
        String botToken = argsParser.getBotToken(args);
        UpdateConsumer updateConsumer = new UpdateConsumer(botToken);
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
            tgApp.registerBot(botToken, updateConsumer);
            Thread.currentThread().join(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}