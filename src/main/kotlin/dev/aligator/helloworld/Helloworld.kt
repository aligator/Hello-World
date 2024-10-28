package dev.aligator.helloworld

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.Entity
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import me.aligator.helloworld.Permissions

val PERMISSION_SHOW = "dev.aligator.helloworld.action.show"
val PERMISSION_COMMAND_SHOW = "dev.aligator.helloworld.command.helloworld.show"
val PERMISSION_COMMAND_RELOAD = "dev.aligator.helloworld.command.helloworld.reload"



class Helloworld : ModInitializer {
    private val formatter: Formatter = Formatter()
    private var message: Text? = null

    private fun loadMessage() {
        try {
            val filePath = FabricLoader.getInstance().configDir.resolve("hello_world_on_join.txt").toFile()
            if (!filePath.exists()) {
                filePath.createNewFile()
                filePath.writeText(
                    """§aWelcome to the server, §lAdventurer§r!

This message is built with §n§b[HelloWorld](https://github.com/aligator/Hello-World)§r

§9Have Fun!"""
                )
            }
            val onJoinMessage: String = filePath.readText()
            message = formatter.format(onJoinMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, registry, environment ->
            dispatcher.register(
                literal("helloworld").executes { context ->
                    return@executes 0
                }.then(literal("show")
                    .requires(Permissions.permissionRequire(PERMISSION_COMMAND_SHOW, USER))
                    .executes { context ->
                        checkPermission(context.source.entity!!, PERMISSION_SHOW, USER)
                        context.source.sendFeedback({ message }, false)
                        return@executes 1
                    }).then(literal("reload")
                    .requires(Permissions.permissionRequire(PERMISSION_COMMAND_RELOAD, OP))
                    .executes { context ->

                        loadMessage()
                        context.source.sendFeedback({ Text.literal("Message reloaded from config file:") }, false)
                        context.source.sendFeedback({ message }, false)
                        return@executes 1
                    })
            )
        }
    }

    private fun registerEvents() {
        ServerPlayConnectionEvents.INIT.register { handler, server ->
            if (message == null) {
                return@register
            }

            try {
                Permissions.checkPermission(handler.player.uuid, handler.player, PERMISSION_SHOW, USER).thenApply { result ->
                    if (!result) {
                        return@thenApply
                    }
                    try {
                        handler.player.sendMessageToClient(message, false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onInitialize() {
        loadMessage()
        registerCommands()
        registerEvents()
    }
}
