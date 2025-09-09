package io.github.patbattb.moderant;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Bot implements LongPollingUpdateConsumer {

    private final TelegramClient botClient;

    public Bot(String botToken) {
        this.botClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::consume);
    }

    private void consume(Update update) {
        try {
            if (update.hasMessage() && "supergroup".equals(update.getMessage().getChat().getType())) {
                handleGroupMessage(update.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGroupMessage(Message message) {

        // ОЧИСТКА СИСЕТМНЫХ СООБЩЕНИЙ
        if (isSystemMessage(message)) {
            DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(), message.getMessageId());
            try {
                botClient.execute(deleteMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        // ПРОВЕРКА ДОПУСТИМОСТИ СООБЩЕНИЯ
            // необходимо проверить в какой топик отправлено сообщение.
            // сравнить ограничение пользователя по времени с разрешениями в данном топике
            // сравнить медиа в сообщении пользователя с ограничениями для топика
            // проверить ограничения по смайликам

        // если все ок - пропускаем сообщение
        // если нет:
            // сохраняем сообщение в файл
            // архивируем в zip
            // удаляем сообщение пользователя
            // отправляем сообщение в топик #recycle
                // > <@nick> <#topic> <HH:MM:SS> с вложением архива
                // записываем в базу ид сообщения и топик
            // в текущем топике:
                // > <@nick> ваше сообщение было удалено, потому что <причина>.
                // > Его текст временно [доступен](ссылка на сообщение в #recycle) в корзине.
                // записываем ид сообщения в базу
            // планируем удаление основного сообщение через n минут (из конфига)
            // планируем удаление сообщения из корзины через m минут (из конфига)
    }

    private boolean isSystemMessage(Message message) {
        return message.getForumTopicClosed() != null ||
                message.getForumTopicReopened() != null;
    }

    private @NotNull ChatMember getChatMember(Message message) throws TelegramApiException {
        GetChatMember get = new GetChatMember(message.getChatId().toString(), message.getFrom().getId());
        ChatMember member = botClient.execute(get);
        if (member == null) {
            throw new NullPointerException("ChatMember cannot be null");
        }
        return member;
    }

    private int getRestrictUntilUnixTime(int messageDate) {
        Instant instant = Instant.ofEpochSecond(messageDate);
        ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneId.of("+3"));
        time = time.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        long epochSecond = time.toInstant().getEpochSecond();
        epochSecond += 31;
        return (int) epochSecond;
    }

    private RestrictChatMember getRestrictChatMember(int restrictionDate, Message message) {
        ChatPermissions permissions = new ChatPermissions();
        permissions.setCanSendMessages(false);
        return new RestrictChatMember(
                message.getChatId().toString(),
                message.getFrom().getId(),
                permissions,
                restrictionDate,
                false);
    }
}
