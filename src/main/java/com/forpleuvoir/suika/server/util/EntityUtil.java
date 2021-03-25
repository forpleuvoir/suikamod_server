package com.forpleuvoir.suika.server.util;

import com.google.common.collect.Lists;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.util
 * @class_name EntityUtil
 * @create_time 2020/12/6 19:09
 */

public class EntityUtil {


    public static List<LivingEntity> getLivingEntities(ClientWorld clientWorld) {
        List<LivingEntity> entityList = Lists.newArrayList();
        Iterable<Entity> iterable = clientWorld.getEntities();
        iterable.forEach(entity -> {
            if (entity instanceof LivingEntity) {
                entityList.add((LivingEntity) entity);
            }
        });
        return entityList;
    }

    public static List<LivingEntity> getLivingEntities(ClientWorld clientWorld, Entity otherEntity, double distance) {
        List<LivingEntity> entityList = Lists.newArrayList();
        Iterable<Entity> iterable = clientWorld.getEntities();
        iterable.forEach(entity -> {
            if (entity instanceof LivingEntity && !entity.equals(otherEntity)) {
                if (otherEntity.getPos().distanceTo(entity.getPos()) <= distance)
                    entityList.add((LivingEntity) entity);
            }
        });
        return entityList;
    }


    public static List<Entity> getLivingEntities(ServerWorld serverWorld) {
        Predicate<? super Entity> predicate = EntityPredicates.EXCEPT_SPECTATOR;
        List<Entity> list = Lists.newArrayList();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        Iterator<Entity> var5 = serverWorld.iterateEntities().iterator();
        while (true) {
            Entity entity;
            do {
                if (!var5.hasNext()) {
                    return list;
                }
                entity = var5.next();
            } while (!(entity instanceof LivingEntity));
            if (serverChunkManager.isChunkLoaded(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4) && predicate.test(entity)) {
                list.add(entity);
            }
        }
    }


    public static List<LivingEntity> getLivingEntities(ServerWorld serverWorld, double distance, LivingEntity other) {
        Predicate<? super Entity> predicate = EntityPredicates.EXCEPT_SPECTATOR;
        List<LivingEntity> list = Lists.newArrayList();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        Iterator<Entity> var5 = serverWorld.iterateEntities().iterator();
        while (true) {
            Entity entity;
            do {
                if (!var5.hasNext()) {
                    return list;
                }
                entity = var5.next();
            } while (!(entity instanceof LivingEntity));
            if (serverChunkManager.isChunkLoaded(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4) && predicate.test(entity)) {
                if (other.getPos().distanceTo(entity.getPos()) <= distance && entity != other) {
                    list.add((LivingEntity) entity);
                }

            }
        }
    }

    public static List<ProjectileEntity> getProjectileEntities(ServerWorld serverWorld, double distance, Entity other) {
        Predicate<? super Entity> predicate = EntityPredicates.EXCEPT_SPECTATOR;
        List<ProjectileEntity> list = Lists.newArrayList();
        ServerChunkManager serverChunkManager = serverWorld.getChunkManager();
        Iterator<Entity> var5 = serverWorld.iterateEntities().iterator();
        while (true) {
            Entity entity;
            do {
                if (!var5.hasNext()) {
                    return list;
                }
                entity = var5.next();
            } while (!(entity instanceof ProjectileEntity));
            if (serverChunkManager.isChunkLoaded(MathHelper.floor(entity.getX()) >> 4, MathHelper.floor(entity.getZ()) >> 4) && predicate.test(entity)) {
                if (other.getPos().distanceTo(entity.getPos()) <= distance && entity != other) {
                    list.add((ProjectileEntity) entity);
                }
            }
        }
    }
}
