package moe.yuuta.suicide

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.Duration
import java.util.*

class SuicideBot(private val mID: String, private val mToken: String) : TelegramLongPollingBot() {
    override fun getBotUsername(): String = mID

    override fun getBotToken(): String = mToken

    override fun onUpdateReceived(update: Update) {
        System.out.println(update)
        val user =
            when {
                update.hasMessage() -> update.message.from
                update.hasCallbackQuery() -> update.callbackQuery.from
                update.hasInlineQuery() -> update.inlineQuery.from
                else -> {
                    System.err.println("Cannot gather user information for $update")
                    return
                }
            }
        val userLangCode = (user.languageCode ?: "en").replace("-", "_")
        val strings = ResourceBundle.getBundle("strings", Locale(userLangCode))
        if (update.hasMessage()) {
            if (update.message.hasText()) {
                val entityStream = update.message.entities?.stream()
                    ?.filter {
                        return@filter it.type == "bot_command"
                    }
                    ?.findFirst()
                val botCommandEntity = if (entityStream != null &&
                    entityStream.isPresent
                ) entityStream.get() else null
                if (update.message.hasText() && botCommandEntity != null) {
                    val mainCommand = update.message.text.substring(0, botCommandEntity.length)
                    System.out.println(mainCommand)
                    when (mainCommand) {
                        "/start" -> {
                            val reply = SendMessage()
                                .setChatId(update.message.chatId)
                                .setReplyToMessageId(update.message.messageId)
                                .setText(strings.getString("welcome"))
                            execute(reply)
                        }
                        "/suicide",
                        "/suicide@$mID" -> {
                            val isGroup = update.message.chat.isGroupChat || update.message.chat.isSuperGroupChat
                            if (!isGroup) {
                                execute(SendMessage()
                                    .setChatId(update.message.chatId)
                                    .setReplyToMessageId(update.message.messageId)
                                    .setText(strings.getString("fail_not_group")))
                            } else {
                                val callerUser = execute(GetChatMember()
                                    .setChatId(update.message.chatId)
                                    .setUserId(update.message.from.id))
                                if (callerUser.status == "creator") {
                                    execute(SendMessage()
                                        .setChatId(update.message.chatId)
                                        .setReplyToMessageId(update.message.messageId)
                                        .setText(strings.getString("fail_is_creator")))
                                } else if (callerUser.status == "administrator") {
                                    execute(SendMessage()
                                        .setChatId(update.message.chatId)
                                        .setReplyToMessageId(update.message.messageId)
                                        .setText(strings.getString("fail_is_administrator")))
                                } else {
                                    val selfAdminStream = execute(GetChatAdministrators()
                                        .setChatId(update.message.chatId))
                                        .stream()
                                        .filter {
                                            return@filter it.user.userName == mID
                                        }
                                        .findFirst()
                                    if (!selfAdminStream.isPresent) {
                                        execute(SendMessage()
                                            .setChatId(update.message.chatId)
                                            .setReplyToMessageId(update.message.messageId)
                                            .enableMarkdown(true)
                                            .setText(strings.getString("fail_missing_permission")))
                                    } else {
                                        val min = 30
                                        val max = 60 * 60 * 24 // 1 day
                                        val seconds = Random().nextInt(max - min + 1) + min
                                        execute(RestrictChatMember()
                                            .setChatId(update.message.chatId)
                                            .setUserId(update.message.from.id)
                                            .setCanSendMediaMessages(false)
                                            .setCanAddWebPagePreviews(false)
                                            .setCanSendMessages(false)
                                            .setCanSendOtherMessages(false)
                                            .forTimePeriod(Duration.ofSeconds(seconds.toLong())))
                                        execute(SendMessage()
                                            .setChatId(update.message.chatId)
                                            .setReplyToMessageId(update.message.messageId)
                                            .enableMarkdown(true)
                                            .setText(String.format(strings.getString("done"), seconds)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}