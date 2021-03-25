package com.forpleuvoir.suika.server.command;

import com.forpleuvoir.suika.server.data.Tpa;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name TpaCommand
 * @create_time 2020/11/30 11:25
 */

public class TpaCommand {
    private static final SimpleCommandExceptionType tpaException = new SimpleCommandExceptionType(new LiteralText("如蜜传如蜜啊"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tpa")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(TpaCommand::tpa)));
        dispatcher.register(CommandManager.literal("tpahere")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(TpaCommand::tpahere)));
        dispatcher.register(CommandManager.literal("tpaccept").executes(TpaCommand::tpaccept));
    }

    private static int tpahere(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        if (sender.getUuidAsString().equals(target.getUuidAsString())) {
            throw tpaException.create();
        }
        Tpa.tpas.put(target.getUuidAsString(), new Tpa(target, sender, source.getWorld().getTime()));
        target.sendSystemMessage(new LiteralText("玩家 ")
                        .append(new LiteralText("§b" + sender.getEntityName()))
                        .append(new LiteralText(" 请求将你传送到ta的位置。"))
                , sender.getUuid());
        target.sendSystemMessage(
                new LiteralText("在120秒内输入§c /tpaccept §f接受请求")
                        .append(
                                new LiteralText("[√]")
                                        .styled(
                                                style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("点击此处接受传送请求")))
                                                        .withColor(Formatting.GREEN)
                                        )),
                sender.getUuid());
        source.sendFeedback(new LiteralText("请求已发送"), false);
        return 1;
    }


    private static int tpa(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity sender = source.getPlayer();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        if (sender.getUuidAsString().equals(target.getUuidAsString())) {
            throw tpaException.create();
        }
        Tpa.tpas.put(target.getUuidAsString(), new Tpa(sender, target, source.getWorld().getTime()));
        target.sendSystemMessage(new LiteralText("玩家 ")
                        .append(new LiteralText("§b" + sender.getEntityName()))
                        .append(new LiteralText(" 请求传送到你的位置。"))
                , sender.getUuid());
        target.sendSystemMessage(
                new LiteralText("在120秒内输入§c /tpaccept §f接受请求")
                        .append(
                                new LiteralText("[√]")
                                        .styled(
                                                style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("点击此处接受传送请求")))
                                                        .withColor(Formatting.GREEN)
                                        )),
                sender.getUuid());
        source.sendFeedback(new LiteralText("请求已发送"), false);
        return 1;
    }

    private static int tpaccept(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (Tpa.tpas.containsKey(player.getUuidAsString())) {
            boolean canTp = Tpa.tpas.get(player.getUuidAsString()).tpa(source.getWorld().getTime());
            if (!canTp) {
                player.sendSystemMessage(new LiteralText("没有等待接受的请求"), player.getUuid());
            }
            Tpa.tpas.remove(player.getUuidAsString());
        } else {
            player.sendSystemMessage(new LiteralText("没有等待接受的请求"), player.getUuid());
        }
        return 1;
    }
}
