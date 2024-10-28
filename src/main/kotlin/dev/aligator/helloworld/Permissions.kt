package dev.aligator.helloworld

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.entity.Entity
import java.util.UUID
import java.util.concurrent.CompletableFuture

val USER = 0
val OP = 4

class Permissions {
    companion object {
        private fun isPermissionsApiAvailable(): Boolean {
            try {
                Class.forName("me.lucko.fabric.api.permissions.v0.Permissions")
                return true
            } catch (_: ClassNotFoundException) {
                return false // not available
            }
        }

        fun checkPermission(source: Entity, permission: String, defaultRequiredLevel: Int): Boolean {
            if (!isPermissionsApiAvailable()) {
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
        fun checkPermission(uuid: UUID, source: Entity, permission: String, defaultRequiredLevel: Int): CompletableFuture<Boolean> {
            if (!isPermissionsApiAvailable()) {
                return CompletableFuture.completedFuture(source.hasPermissionLevel(defaultRequiredLevel))
            }

            return Permissions.check(uuid, permission, source.hasPermissionLevel(defaultRequiredLevel))
        }

        fun permissionRequire(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> {
            return Predicate { source: ServerCommandSource ->
                Boolean
                if (source.isExecutedByPlayer) {
                    if (!isPermissionsApiAvailable()) {
                        return@Predicate source.entity!!.hasPermissionLevel(defaultRequiredLevel)
                    } else {
                        return@Predicate Permissions.check(source.entity!!, permission, defaultRequiredLevel)
                    }
                } else {
                    return@Predicate true
                }
            }
        }

    }
}