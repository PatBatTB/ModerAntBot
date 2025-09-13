package io.github.patbattb.moderant;

import io.github.patbattb.moderant.database.SQLiteConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.sql.SQLException;


public class App {

    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication()) {
        LOG.trace("App starts.");
        LOG.trace("Parameters initialization starts.");
        Parameters.init();
        LOG.trace("Parameters initialization finish.");
        startDB();
        BotSync botSync = new BotSync(Parameters.getBotToken());
        startBot(tgApp, botSync);
        Thread.currentThread().join(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void startBot(TelegramBotsLongPollingApplication tgApp, LongPollingUpdateConsumer bot) {
        try {
            tgApp.registerBot(Parameters.getBotToken(), bot);
            LOG.info("Bot started successfully.");
        } catch (Exception e) {
            LOG.error("Error during bot registering.", e);
            throw new RuntimeException(e);
        }
    }

    private static void startDB() {
        try {
            SQLiteConnectionPool.initializeDatabase();
            LOG.info("Database initialized successfully");
        } catch (SQLException e) {
            LOG.error("Database initialization failed.");
            throw new RuntimeException(e);
        }
    }
}