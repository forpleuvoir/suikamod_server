package com.forpleuvoir.suika.server.mixin;

import com.forpleuvoir.suika.server.data.WarpPoint;
import com.forpleuvoir.suika.server.util.PlayerHeadUtil;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.UUID;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.mixin.server
 * @class_name ServerPlayerEntityMixin
 * @create_time 2020/11/23 14:19
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {


    @Shadow
    public abstract void sendSystemMessage(Text message, UUID senderUuid);

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @Shadow
    public abstract void addExperience(int experience);


    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }


    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        DimensionType type = this.world.getDimension();
        WarpPoint.setBack(getUuidAsString(), new WarpPoint.Pos(getPos(), type));
        Text text = new TranslatableText("输入 §c/back §r返回死亡地点").styled((style) -> style.withColor(Formatting.WHITE).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/back")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip"))));
        sendMessage(text, false);

        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity) {
            //如果攻击者是玩家 则有可能掉落头颅
            //基础概率
            float baseProbability = 5.0f;
            ListTag enchantments = ((PlayerEntity) attacker).getMainHandStack().getEnchantments();
            if (!enchantments.isEmpty()) {
                for (int i = 0; i < enchantments.size(); i++) {
                    CompoundTag compoundTag = enchantments.getCompound(i);
                    String str = compoundTag.getString("id");
                    //如果附魔是抢夺增加掉落头颅的几率
                    if (str.split(":")[1].equals("looting")) {
                        //抢夺增加的基础概率
                        float addProbability = 3.0f;
                        //每一级抢夺额外增加的概率
                        float lootingAddProbability = 1.0f;
                        int lvl = compoundTag.getInt("lvl");
                        baseProbability += addProbability + (lvl * lootingAddProbability);
                    }
                }
            }
            Random random = getRandom();
            int a=random.nextInt(100)+1;
            if (a <= baseProbability) {
                ItemEntity itemEntity =  PlayerHeadUtil.getItemEntity((ServerWorld) world,this);
                this.world.spawnEntity(itemEntity);
            }
        }
    }

    @Inject(method = "teleport", at = @At("HEAD"))
    public void teleportHEAD(CallbackInfo ci) {
        WarpPoint.setBack(getUuidAsString(), new WarpPoint.Pos(getPos(), getEntityWorld().getDimension()));
    }

    @Inject(method = "teleport", at = @At("RETURN"))
    public void teleportRETURN(CallbackInfo ci) {
        addExperience(0);
    }
}
