package com.forpleuvoir.suika.server;

import com.forpleuvoir.suika.server.command.ServerCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author forpleuvoir
 * @project_name suika_server
 * @package com.forpleuvoir.suika.server
 * @class_name Suika
 * @create_time 2021/3/25 15:27
 */

public class Suika implements ModInitializer {

    public static final String MOD_ID = "suika";
    public static final Logger LOGGER = LogManager.getLogger(Suika.MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("suika mod initialize...");
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> ServerCommand.commandRegister(dispatcher)));
    }
}
