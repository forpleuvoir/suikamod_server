package com.forpleuvoir.suika.server.util;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;


/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.client.util
 * @class_name PlayerHeadUtil
 * @create_time 2021/1/18 12:56
 */

public class PlayerHeadUtil {
    public static ItemStack getPlayerHead(String playerName) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound tag = new NbtCompound();
        tag.put("SkullOwner", NbtString.of(playerName));
        stack.setTag(tag);
        return stack;
    }

    public static ItemEntity getItemEntity(ServerWorld world, PlayerEntity player) {
        return new ItemEntity(world, player.getX(), player.getY(), player.getZ(), PlayerHeadUtil.getPlayerHead(player.getEntityName()));
    }


    public static ItemEntity getItemEntity(ServerWorld world, Vec3d pos, String name) {
        return new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), PlayerHeadUtil.getPlayerHead(name));
    }


}
