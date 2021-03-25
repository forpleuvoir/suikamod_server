package com.forpleuvoir.suika.server.data;

import com.google.common.collect.Maps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.data
 * @class_name Tpa
 * @create_time 2020/11/30 11:24
 */

public class Tpa {
    //String是目标玩家的uuid
    public static Map<String, Tpa> tpas = null;
    private static final Long time = 20 * 120L;

    public static void initialize() {
        tpas = Maps.newHashMap();
    }

    private final ServerPlayerEntity player;
    private final Vec3d target;
    private final ServerWorld targetWorld;
    private final Long expireTime;

    public Tpa(ServerPlayerEntity player, ServerPlayerEntity target, Long nowTime) {
        this.player = player;
        this.target = target.getPos();
        this.targetWorld = target.getServerWorld();
        this.expireTime = nowTime + time;
    }


    public boolean tpa(long nowTime) {
        if (canTp(nowTime)) {
            teleport(this.player, targetWorld, target);
            return true;
        } else {
            return false;
        }
    }

    private boolean canTp(long nowTime) {
        return this.expireTime > nowTime;
    }


    public ServerPlayerEntity getSender() {
        return player;
    }


    public Long getExpireTime() {
        return expireTime;
    }

    private static void teleport(ServerPlayerEntity player, ServerWorld serverWorld, Vec3d pos) {
        WarpPoint.teleport(player, serverWorld, pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch);
    }
}
