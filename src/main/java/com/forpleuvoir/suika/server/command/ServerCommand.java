package com.forpleuvoir.suika.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.command
 * @class_name ServerCommand
 * @create_time 2020/11/22 13:47
 */

public class ServerCommand {

    public static void commandRegister(CommandDispatcher<ServerCommandSource> dispatcher){
        HomeCommand.register(dispatcher);
        WarpCommand.register(dispatcher);
        BackCommand.register(dispatcher);
        TpaCommand.register(dispatcher);
    }

}
