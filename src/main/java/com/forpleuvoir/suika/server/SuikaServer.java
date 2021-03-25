package com.forpleuvoir.suika.server;

import com.forpleuvoir.suika.server.command.ServerCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
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

public class SuikaServer implements DedicatedServerModInitializer {

    public static final String MOD_ID = "suika";
    public static final Logger LOGGER = LogManager.getLogger(SuikaServer.MOD_ID);

    @Override
    public void onInitializeServer() {
        LOGGER.info("suika mod server initialize...");
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> ServerCommand.commandRegister(dispatcher)));
    }
}
