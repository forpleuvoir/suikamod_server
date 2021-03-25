package com.forpleuvoir.suika.server.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@SuppressWarnings("EntityConstructor")
public class EntityPlayerMPFake extends ServerPlayerEntity
{
    public Runnable fixStartingPosition = () -> {};
    public boolean isAShadow;

    public static EntityPlayerMPFake createFake(String username, MinecraftServer server, double d0, double d1, double d2, double yaw, double pitch, RegistryKey<World> dimensionId, GameMode gamemode)
    {
        //prolly half of that crap is not necessary, but it works
        ServerWorld worldIn = server.getWorld(dimensionId);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null)
        {
            return null;
        }
        if (gameprofile.getProperties().containsKey("textures"))
        {
            gameprofile = SkullBlockEntity.loadProperties(gameprofile);
        }
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, false);
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(d0, d1, d2, (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new NetworkManagerFake(NetworkSide.SERVERBOUND), instance);
        instance.teleport(worldIn, d0, d1, d2, (float)yaw, (float)pitch);
        instance.setHealth(20.0F);
        instance.unsetRemoved();
        instance.stepHeight = 0.6F;
        instance.interactionManager.changeGameMode(gamemode);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), dimensionId);//instance.dimension);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), dimensionId);//instance.dimension);
        instance.getServerWorld().getChunkManager().updateCameraPosition(instance);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        return instance;
    }



    private EntityPlayerMPFake(MinecraftServer server, ServerWorld worldIn, GameProfile profile, boolean shadow)
    {
        super(server, worldIn, profile);
        isAShadow = shadow;
    }

    @Override
    protected void onEquipStack(ItemStack stack)
    {
        if (!isUsingItem()) super.onEquipStack(stack);
    }


    public void kill(Text reason)
    {
        this.server.send(new ServerTask(this.server.getTicks(), () -> {
            this.networkHandler.onDisconnected(reason);
        }));
    }

    @Override
    public void tick()
    {
        if (this.getServer().getTicks() % 10 == 0)
        {
            this.networkHandler.syncWithPlayerPosition();
            this.getServerWorld().getChunkManager().updateCameraPosition(this);
            onTeleportationDone(); //<- causes hard crash but would need to be done to enable portals // not as of 1.17
        }
        super.tick();
        this.playerTick();
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill(this.getDamageTracker().getDeathMessage());
    }

    @Override
    public String getIp()
    {
        return "127.0.0.1";
    }
}
