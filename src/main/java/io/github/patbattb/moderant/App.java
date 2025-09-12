package io.github.patbattb.moderant;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class App {
    public static void main(String[] args) {
        Parameters.init();
        BotSync botSync = new BotSync(Parameters.getBotToken());
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
            tgApp.registerBot(Parameters.getBotToken(), botSync);
            Thread.currentThread().join(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}