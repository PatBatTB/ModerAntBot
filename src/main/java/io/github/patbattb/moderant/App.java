package io.github.patbattb.moderant;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class App {
    public static void main(String[] args) {
        Parameters.init();
        Bot bot = new Bot(Parameters.getBotToken());
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
            tgApp.registerBot(Parameters.getBotToken(), bot);
            Thread.currentThread().join(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}