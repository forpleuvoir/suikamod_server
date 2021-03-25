package com.forpleuvoir.suika.server.mixin;



import com.forpleuvoir.suika.server.util.PlayerHeadUtil;
import com.forpleuvoir.suika.server.util.ReflectionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.mixin.server
 * @class_name LivingEntityMixin
 * @create_time 2020/12/6 18:15
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getMainHandStack();

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        getMainHandStack().inventoryTick(getEntityWorld(), this, 0, true);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        if (getType().equals(EntityType.SNOW_GOLEM)) {
            if (source.getAttacker() instanceof CreeperEntity) {
                if (((CreeperEntity) source.getAttacker()).shouldRenderOverlay()) {
                    if (hasPumpkin() && hasCustomName()) {
                        world.spawnEntity(PlayerHeadUtil.getItemEntity((ServerWorld) world, getPos(), Objects.requireNonNull(getCustomName()).asString()));
                    }
                }
            }
        }
    }

    public boolean hasPumpkin() {
        return ((Byte) this.dataTracker.get((TrackedData<Byte>) ReflectionUtils.getPrivateFieldValueByType(null, SnowGolemEntity.class, TrackedData.class, 0)) & 16) != 0;
    }
}
