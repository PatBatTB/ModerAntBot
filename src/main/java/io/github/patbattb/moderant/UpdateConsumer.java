package io.github.patbattb.moderant;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
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

public class UpdateConsumer implements LongPollingUpdateConsumer {

    private final TelegramClient botClient;

    public UpdateConsumer(String botToken) {
        this.botClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::consume);
    }

    private void consume(Update update) {
        if (update.hasMessage() && "supergroup".equals(update.getMessage().getChat().getType())) {
            handleGroupMessage(update.getMessage());
        }
    }

    private void handleGroupMessage(Message message) {

        // необходимо проверить в какой топик отправлено сообщение.
            //если отправлено в основной:
                //проверить разрешено ли пользователю отправлять сообщение:
                    //если разрешено:
                        // необходимо проверить сообщение не предмет всего лишнего:
                        // сообщение не должно быть стикером, изображением, видео, геолокацией.
                            //если сообщение содержит что-то запрещеное:
                                //написать сообщение, что допускается отправлять только текст, сообщение будет удалено через минуту.
                                //создать задачу по удалению сообщения и предупреждения.
                            // если сообщение содержит только текст: ОК
                    // если запрещено:
                        // написать сообщение, что допускется отправка только одного сообщения в день и время снятия ограничения.
                        // написать, что сообщение будет удалено через минуту
                        // создать задачу по удалению сообщения и предупреждения.
        // если в доп.топики: ОК
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
