package com.forpleuvoir.suika.server.command;

import com.forpleuvoir.suika.server.data.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name PlayerCommand
 * @create_time 2021/2/16 21:16
 */

public class PlayerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("player")
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .then(CommandManager.literal("join").executes(PlayerCommand::join))
                        .then(CommandManager.literal("exit").executes(PlayerCommand::exit))
                )
        );
    }

    private static int join(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (cantSpawn(context)) return 0;
        ServerCommandSource source = context.getSource();
        Vec3d pos = tryGetArg(
                () -> Vec3ArgumentType.getVec3(context, "position"),
                source::getPosition
        );
        Vec2f facing = tryGetArg(
                () -> RotationArgumentType.getRotation(context, "direction").toAbsoluteRotation(context.getSource()),
                source::getRotation
        );
        RegistryKey<World> dimType = tryGetArg(
                () -> DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey(),
                () -> source.getWorld().getRegistryKey() // dimension.getType()
        );
        GameMode mode = GameMode.CREATIVE;
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            mode = player.interactionManager.getGameMode();
        } catch (CommandSyntaxException ignored) {
        }
        String playerName = StringArgumentType.getString(context, "player");
        if (playerName.length() > 40) {
            context.getSource().sendFeedback(new LiteralText("玩家名称 " + playerName + "  太长了"), false);
            return 0;
        }
        MinecraftServer server = source.getMinecraftServer();
        PlayerEntity player = EntityPlayerMPFake.createFake(playerName, server, pos.x, pos.y, pos.z, facing.y, facing.x, dimType, mode);
        if (player == null) {
            context.getSource().sendFeedback(new LiteralText("rb Player " + StringArgumentType.getString(context, "player") + " doesn't exist " +
                    "and cannot spawn in online mode. Turn the server offline to spawn non-existing players"), false);
            return 0;
        }
        return 1;
    }

    private static int exit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (cantReMove(context)) return 0;
        getPlayer(context).kill();
        return 1;
    }

    //引用自carpet
    private static boolean cantManipulate(CommandContext<ServerCommandSource> context) {
        PlayerEntity player = getPlayer(context);
        if (player == null) {
            error(context.getSource(), "Can only manipulate existing players");
            return true;
        }
        PlayerEntity sendingPlayer;
        try {
            sendingPlayer = context.getSource().getPlayer();
        } catch (CommandSyntaxException e) {
            return false;
        }

        if (!context.getSource().getMinecraftServer().getPlayerManager().isOperator(sendingPlayer.getGameProfile())) {
            if (sendingPlayer != player && !(player instanceof EntityPlayerMPFake)) {
                error(context.getSource(), "Non OP players can't control other real players");
                return true;
            }
        }
        return false;
    }

    //引用自carpet
    private static boolean cantReMove(CommandContext<ServerCommandSource> context) {
        if (cantManipulate(context)) return true;
        PlayerEntity player = getPlayer(context);
        if (player instanceof EntityPlayerMPFake) return false;
        context.getSource().sendError(new LiteralText("Only fake players can be moved or killed"));
        return true;
    }

    //引用自carpet
    private static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getMinecraftServer();
        return server.getPlayerManager().getPlayer(playerName);
    }

    //引用自carpet
    private static boolean cantSpawn(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getMinecraftServer();
        PlayerManager manager = server.getPlayerManager();
        PlayerEntity player = manager.getPlayer(playerName);
        if (player != null) {
            context.getSource().sendFeedback(new LiteralText("玩家 " + playerName + " 已经登录"), false);
            return true;
        }
        GameProfile profile = server.getUserCache().findByName(playerName);
        if (profile == null) {
            context.getSource().sendFeedback(new LiteralText("Player " + playerName + " is either banned by Mojang, or auth servers are down. " +
                    "Banned players can only be summoned in Singleplayer and in servers in off-line mode."), false);

            return true;
        }
        if (manager.getUserBanList().contains(profile)) {
            context.getSource().sendFeedback(new LiteralText("玩家 " + playerName + "  is banned on this server"), false);
            return true;
        }
        if (manager.isWhitelistEnabled() && manager.isWhitelisted(profile) && !context.getSource().hasPermissionLevel(2)) {
            context.getSource().sendFeedback(new LiteralText("Whitelisted players can only be spawned by operators"), false);
            return true;
        }
        return false;
    }

    //引用自carpet
    @FunctionalInterface
    interface SupplierWithCommandSyntaxException<T> {
        T get() throws CommandSyntaxException;
    }

    //引用自carpet
    private static <T> T tryGetArg(SupplierWithCommandSyntaxException<T> a, SupplierWithCommandSyntaxException<T> b) throws CommandSyntaxException {
        try {
            return a.get();
        } catch (IllegalArgumentException e) {
            return b.get();
        }
    }

    private static void error(ServerCommandSource source, String string) {
        source.sendError(new LiteralText(string));
    }
}
