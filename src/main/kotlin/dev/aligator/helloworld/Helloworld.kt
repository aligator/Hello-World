package dev.aligator.helloworld

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.Entity
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

val PERMISSION_SHOW = "dev.aligator.helloworld.action.show"
val PERMISSION_COMMAND_SHOW = "dev.aligator.helloworld.command.helloworld.show"
val PERMISSION_COMMAND_RELOAD = "dev.aligator.helloworld.command.helloworld.reload"

val USER = 0
val OP = 4

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

    private fun isPermissionsApiAvailable(): Boolean {
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions")
            return true
        } catch (_: ClassNotFoundException) {
            return false // not available
        }
    }

    private fun checkPermission(source: Entity, permission: String, defaultRequiredLevel: Int): Boolean {
        if (!isPermissionsApiAvailable() && source is ServerPlayerEntity) {
            return source.hasPermissionLevel(defaultRequiredLevel)
        }

        return Permissions.check(source, permission, defaultRequiredLevel)
    }

    /**
     * The login event does not seem to have the player loaded properly already.
     *
     * So in case of a permission manager check the uuid is used instead - which resolves in the future.
     * This seems to fix this problem for this case.
     *
     * Note that I still use the permission level check of the source! So the source and uuid must match!
     *
     * See also https://luckperms.net/wiki/Developer-API-Usage#distinction-between-online--offline-players
     * But since I do not use luckyperms directly, but the simpler fabric-permission api, I have to handle this myself.
     */
    private fun checkPermission(
        uuid: UUID,
        source: ServerPlayerEntity,
        permission: String,
        defaultRequiredLevel: Int
    ): CompletableFuture<Boolean> {
        if (!isPermissionsApiAvailable()) {
            return CompletableFuture<Boolean>.completedFuture(source.hasPermissionLevel(defaultRequiredLevel))
        }

        return Permissions.check(uuid, permission, source.hasPermissionLevel(defaultRequiredLevel))
    }

    private fun permissionRequire(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> {
        return Predicate { source: ServerCommandSource ->
            Boolean
            if (source.isExecutedByPlayer && source.entity is ServerPlayerEntity) {
                if (!isPermissionsApiAvailable()) {
                    return@Predicate (source.entity as ServerPlayerEntity).hasPermissionLevel(defaultRequiredLevel)
                } else {
                    return@Predicate Permissions.check(source.entity!!, permission, defaultRequiredLevel)
                }
            } else {
                return@Predicate true
            }
        }
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register { dispatcher, registry, environment ->
            dispatcher.register(
                literal("helloworld").executes { context ->
                    return@executes 0
                }.then(literal("show")
                    .requires(permissionRequire(PERMISSION_COMMAND_SHOW, USER))
                    .executes { context ->
                        checkPermission(context.source.entity!!, PERMISSION_SHOW, USER)
                        context.source.sendFeedback({ message }, false)
                        return@executes 1
                    }).then(literal("reload")
                    .requires(permissionRequire(PERMISSION_COMMAND_RELOAD, OP))
                    .executes { context ->

                        loadMessage()
                        context.source.sendFeedback({ Text.literal("Message reloaded from config file:") }, false)
                        context.source.sendFeedback({ message }, false)
                        return@executes 1
                    })
            )
        }
    }

    override fun onInitialize() {
        loadMessage()
        registerCommands()

        ServerPlayConnectionEvents.INIT.register { handler, server ->
            if (message == null) {
                return@register
            }

            try {
                checkPermission(handler.player.uuid, handler.player, PERMISSION_SHOW, USER).thenApply { result ->
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
}
