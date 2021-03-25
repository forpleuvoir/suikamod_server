package com.forpleuvoir.suika.server.command;

import com.forpleuvoir.suika.server.data.WarpPoint;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name BackCommand
 * @create_time 2020/11/23 13:49
 */

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("back").executes(BackCommand::back));
    }

    private static int back(CommandContext<ServerCommandSource> context)throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        WarpPoint.back(player);
        return 1;
    }
}
