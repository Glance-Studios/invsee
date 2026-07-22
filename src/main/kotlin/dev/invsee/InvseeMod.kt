package dev.invsee

import com.mojang.brigadier.Command
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import org.slf4j.LoggerFactory

/**
 * Server-side inventory viewer. `/invsee <player>` opens a live permission-gated view of another
 * player's inventory, and `/invsee show <viewer> <target>` forces one on a viewer under a separate
 * node. Permissions go through fabric-permissions-api, honouring LuckPerms and falling back to op
 * level 2. The `[inv]` chat token only runs the command, so the two mods are not coupled.
 */
object InvseeMod : DedicatedServerModInitializer {

    private val LOG = LoggerFactory.getLogger("invsee")

    lateinit var config: Config
        private set

    override fun onInitializeServer() {
        config = Config.load()
        registerCommand()
        LOG.info("Invsee ready. Permission node '{}' (fallback op {}).", config.permissionNode, config.defaultOpLevel)
    }

    private fun registerCommand() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("invsee")
                    .requires { Permissions.check(it, config.permissionNode, config.defaultOpLevel) }
                    // /invsee <target>: open target's inventory for yourself.
                    .then(
                        Commands.argument("target", EntityArgument.player())
                            .executes { ctx ->
                                val viewer = ctx.source.playerOrException
                                val target = EntityArgument.getPlayer(ctx, "target")
                                open(viewer, target)
                                Command.SINGLE_SUCCESS
                            },
                    )
                    // /invsee show <viewer> <target>: force <viewer> to open <target>'s inventory.
                    .then(
                        Commands.literal("show")
                            .requires { Permissions.check(it, config.showPermissionNode, config.defaultOpLevel) }
                            .then(
                                Commands.argument("viewer", EntityArgument.player())
                                    .then(
                                        Commands.argument("target", EntityArgument.player())
                                            .executes { ctx ->
                                                val viewer = EntityArgument.getPlayer(ctx, "viewer")
                                                val target = EntityArgument.getPlayer(ctx, "target")
                                                open(viewer, target)
                                                ctx.source.sendSuccess(
                                                    {
                                                        Fmt.legacy(
                                                            "&7Opened &f${target.gameProfile.name}&7's inventory for " +
                                                                "&f${viewer.gameProfile.name}&7.",
                                                        )
                                                    },
                                                    true,
                                                )
                                                Command.SINGLE_SUCCESS
                                            },
                                    ),
                            ),
                    ),
            )
        }
    }

    /** Open [target]'s inventory for [viewer]. Both must be online. */
    fun open(viewer: ServerPlayer, target: ServerPlayer) {
        val title: Component = Fmt.legacy(config.titleFormat.format(target.gameProfile.name))
        val provider = SimpleMenuProvider(
            { id, inv, _ -> InvseeMenu(id, inv, InventoryView(target), config.allowEdit) },
            title,
        )
        viewer.openMenu(provider)
        viewer.sendSystemMessage(Fmt.legacy("&7Viewing &f${target.gameProfile.name}&7's inventory."))
    }
}
