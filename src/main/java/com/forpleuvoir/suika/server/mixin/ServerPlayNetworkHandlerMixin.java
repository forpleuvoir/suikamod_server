package com.forpleuvoir.suika.server.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.MessageType;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 服务器玩家聊天信息处理
 *
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.mixin.server
 * @class_name ServerPlayNetworkHandlerMixin
 * @create_time 2021/1/5 15:59
 */

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    public abstract void disconnect(Text reason);

    @Shadow
    protected abstract void executeCommand(String input);

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    private int messageCooldown;

    @Inject(method = "method_31286", at = @At("HEAD"), cancellable = true)
    public void method_31286(String message, CallbackInfo ci) {

        if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
        } else {
            this.player.updateLastActionTime();

            for (int i = 0; i < message.length(); ++i) {
                if (!SharedConstants.isValidChar(message.charAt(i))) {
                    disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
                    return;
                }
            }

            if (message.startsWith("/")) {
                executeCommand(message);
            } else {
                message = message.replace("&", "§");
                MutableText mutableText = new LiteralText("");
                if (message.contains("[i]")) {
                    String[] s = message.split("\\[i]", -1);
                    for (int i = 0; i < s.length; i++) {
                        mutableText.append(s[i]);
                        if (i != s.length - 1) {
                            ItemStack stack = this.player.getMainHandStack();
                            if (stack.getItem() != Items.AIR) {
                                mutableText.append(stack.toHoverableText());
                            } else {
                                mutableText.append("[i]");
                            }
                        }
                    }
                } else {
                    mutableText.append(message);
                }
                MutableText text = new TranslatableText("chat.type.text", this.player.getDisplayName(), mutableText);
                this.server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, this.player.getUuid());
            }
            this.messageCooldown += 20;
            if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
                this.disconnect(new TranslatableText("disconnect.spam"));
            }

        }
        //覆盖原本的方法
        ci.cancel();
    }
}
