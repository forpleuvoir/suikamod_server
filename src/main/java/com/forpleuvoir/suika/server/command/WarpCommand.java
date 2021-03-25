package com.forpleuvoir.suika.server.command;

import com.forpleuvoir.suika.server.data.WarpPoint;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.HashSet;
import java.util.Set;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name WarpCommand
 * @create_time 2020/11/23 11:59
 */

public class WarpCommand {
    private static final SimpleCommandExceptionType warpException = new SimpleCommandExceptionType(new TranslatableText("传送点不存在"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("warp")
                .then(CommandManager.argument("warp", StringArgumentType.string())
                        .suggests((c, b) -> {
                            Set<String> keys = new HashSet<>();
                            Set<String> strings = WarpPoint.warpPoints.keySet();
                            strings.forEach(s -> {
                                keys.add("\"" + s + "\"");
                            });
                            return CommandSource.suggestMatching(keys, b);
                        })
                        .executes(WarpCommand::warp))
        );
        dispatcher.register(CommandManager.literal("warps").executes(WarpCommand::warps));
        dispatcher.register(CommandManager.literal("setwarp")
                .then(CommandManager.argument("warp", StringArgumentType.string())
                        .executes(WarpCommand::setWarp)).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)));
        dispatcher.register(CommandManager.literal("removewarp")
                .then(CommandManager.argument("warp", StringArgumentType.string())
                        .suggests((c, b) -> {
                            Set<String> keys = new HashSet<>();
                            Set<String> strings = WarpPoint.warpPoints.keySet();
                            strings.forEach(s -> {
                                keys.add("\"" + s + "\"");
                            });
                            return CommandSource.suggestMatching(keys, b);
                        })
                        .executes(WarpCommand::removeWarp)).requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)));
    }

    private static int warp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        String arg = StringArgumentType.getString(context, "warp");
        if (WarpPoint.warp(player, arg))
            source.sendFeedback(new TranslatableText("将玩家")
                    .append(new LiteralText(" §b" + player.getEntityName()))
                    .append(new TranslatableText("传送到"))
                    .append(new LiteralText(" §b" + arg)), false);
        else {
            throw warpException.create();
        }
        return 1;
    }

    private static int removeWarp(CommandContext<ServerCommandSource> context) {
        String arg = StringArgumentType.getString(context, "warp");
        WarpPoint.remove(arg);
        return 1;
    }

    private static int setWarp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String arg = StringArgumentType.getString(context, "warp");
        WarpPoint.addWarp(arg, player);
        return 1;
    }

    private static int warps(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BaseText text = new TranslatableText("世界传送点 : ");
        WarpPoint.warpPoints.keySet().forEach(e -> text.append(e).append(","));
        source.sendFeedback(text, false);
        return 1;
    }
}
