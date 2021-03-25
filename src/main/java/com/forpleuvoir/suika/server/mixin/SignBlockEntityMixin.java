package com.forpleuvoir.suika.server.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.mixin.block
 * @class_name SignBlockEntityMixin
 * @create_time 2020/11/20 9:00
 */
@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {

    @Shadow
    private boolean editable;

    @Inject(method = "onActivate", at = @At("HEAD"))
    public void useOnBlock(PlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
        if (player.abilities.allowModifyWorld) {
            editable = true;
            SignBlockEntity sign = (SignBlockEntity) (Object) this;
            player.openEditSignScreen(sign);
        }
    }


}
