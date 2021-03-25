package com.forpleuvoir.suika.server.mixin;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.mixin.item
 * @class_name ItemStackMixin
 * @create_time 2021/1/18 14:15
 */

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract NbtCompound getOrCreateSubTag(String key);

    @Inject(method = "setCustomName",at=@At("RETURN"),cancellable = true)
    public void setCustomName(@Nullable Text name, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        NbtCompound compoundTag = this.getOrCreateSubTag("display");
        if (name != null) {
            Text newText= new LiteralText(name.getString().replace("&","§"));
            compoundTag.putString("Name", Text.Serializer.toJson(newText));
        } else {
            compoundTag.remove("Name");
        }
    }
}
