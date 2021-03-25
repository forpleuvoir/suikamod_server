package com.forpleuvoir.suika.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name SuicideCommand
 * @create_time 2021/1/30 18:18
 */

public class SuicideCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("suicide")
                .executes(SuicideCommand::suicide));
    }

    public static int suicide(CommandContext<ServerCommandSource> context)throws CommandSyntaxException {
        context.getSource().getPlayer().setHealth(0);
        context.getSource().sendFeedback(new LiteralText("你结束了自己的生命"),false);
        return 0;
    }
}
