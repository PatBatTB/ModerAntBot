package io.github.patbattb.moderant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.patbattb.moderant.database.SQLiteConnectionPool;
import io.github.patbattb.moderant.domain.Proxy;
import io.github.patbattb.moderant.service.DeletingService;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import java.net.InetSocketAddress;
import java.sql.SQLException;


public class App {

    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        Parameters.init();
        setLoggerLevelFromConfig(Parameters.getLogLevel());
        LOG.info("Parameters initialization finished.");

        OkHttpClient client = getClient();

        try (TelegramBotsLongPollingApplication tgApp = new TelegramBotsLongPollingApplication(
                ObjectMapper::new,
                () -> client
        ))
        {

            BotSync botSync = new BotSync(client, Parameters.getBotToken());
            startDB();
            startBot(tgApp, botSync);
            startDeletingService(botSync);

            Thread.currentThread().join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static OkHttpClient getClient() {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        Proxy proxyConfig = Parameters.getProxy();

        if (proxyConfig != null) {
            InetSocketAddress address = new InetSocketAddress(proxyConfig.host(), proxyConfig.port());
            java.net.Proxy proxy = new java.net.Proxy(proxyConfig.type(), address);
            okHttpBuilder.proxy(proxy);

            if (proxyConfig.auth() != null) {
                String login = Parameters.getProxy().auth().login();
                char[] pass = Parameters.getProxy().auth().pass().toCharArray();
                java.net.Authenticator.setDefault(new java.net.Authenticator() {
                    @Override
                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
                        return new java.net.PasswordAuthentication(login, pass);
                    }
                });
            }
        }

        okHttpBuilder.connectTimeout(java.time.Duration.ofSeconds(30));
        okHttpBuilder.readTimeout(java.time.Duration.ofSeconds(30));

        return okHttpBuilder.build();
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