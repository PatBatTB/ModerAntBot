package io.github.patbattb.moderant;

import io.github.patbattb.moderant.database.SQLiteConnectionPool;
import io.github.patbattb.moderant.service.DeletingService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.sql.SQLException;


public class App {

    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        Parameters.init();
        setLoggerLevelFromConfig(Parameters.getLogLevel());
        LOG.info("Parameters initialization finished.");

        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication())
        {
            BotSync botSync = new BotSync(Parameters.getBotToken());
            startDB();
            startBot(tgApp, botSync);
            startDeletingService(botSync);

            Thread.currentThread().join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void startBot(TelegramBotsLongPollingApplication tgApp, LongPollingUpdateConsumer bot) {
        try {
            tgApp.registerBot(Parameters.getBotToken(), bot);
            LOG.info("Bot started successfully.");
        } catch (Exception e) {
            LOG.error("Error during bot registration.", e);
            throw new RuntimeException(e);
        }
    }

    private static void startDB() {
        try {
            SQLiteConnectionPool.initializeDatabase();
            LOG.info("Database initialized successfully.");
        } catch (SQLException e) {
            LOG.error("Database initialization failed.");
            throw new RuntimeException(e);
        }
    }

    private static void startDeletingService(BotSync bot) {
        DeletingService deletingService = new DeletingService(60 * 1000, bot);
        try {
            deletingService.runRepeatableDeleting();
        } catch (Exception e) {
            LOG.error("Error during runs deleting service.", e);
        }
    }

    private static void setLoggerLevelFromConfig(Level level) {
        if (level != null) {
            Configurator.setLevel("io.github.patbattb", level);
        }
    }
}