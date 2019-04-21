package moe.yuuta.suicide

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.util.*

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 2) {
            System.err.println("Unexpected arguments. ${args.size}")
            System.err.println("Usage: Launcher <@ user name> <token>")
            System.err.println("For example: Launcher @suicide 123456:abcdef")
            System.exit(1)
            return
        }
        System.out.println("DEBUG: Starting with ID ${args[0]} and token begins with ${args[1].toCharArray()[0]}")
        Locale.setDefault(Locale("en"))
        ApiContextInitializer.init()
        val api = TelegramBotsApi()
        api.registerBot(SuicideBot(args[0], args[1]))
        System.out.println("DEBUG: All setup")
    }
}