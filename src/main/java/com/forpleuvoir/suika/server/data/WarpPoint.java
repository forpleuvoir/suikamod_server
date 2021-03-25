package com.forpleuvoir.suika.server.data;

import com.forpleuvoir.suika.server.SuikaServer;
import com.forpleuvoir.suika.server.util.ReflectionUtils;
import com.forpleuvoir.suikalib.util.FileUtil;
import com.forpleuvoir.suikalib.util.JsonUtil;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

/**
 * @author forpleuvoir
 * @project_name suikamod
 * @package com.forpleuvoir.suika.server.data
 * @class_name WarpPoint
 * @create_time 2020/11/22 14:08
 */

public class WarpPoint {
    public static final Map<String, Pos> warpPoints = Maps.newHashMap();
    public static final Map<String, Pos> homePoints = Maps.newHashMap();
    public static final Map<String, Pos> backPoints = Maps.newHashMap();
    private static File file;
    private static File filePath;

    public static void initialize(LevelStorage.Session session) {
        Constructor<WorldSavePath> constructor;
        try {
            constructor = WorldSavePath.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            WorldSavePath worldSavePath = constructor.newInstance("suika");
            assert session != null;
            filePath = session.getDirectory(worldSavePath).toFile();
            file = new File(filePath, "warp_point.json");
            load();
        } catch (Exception e) {
            SuikaServer.LOGGER.warn("suika mod warp_point load failed...");
            if (!file.exists()) {
                try {
                    createFile();
                } catch (IOException ioException) {
                    SuikaServer.LOGGER.warn("suika mod warp_point create failed...");
                    ioException.printStackTrace();
                }
            }
        }
    }

    private static void createFile() throws IOException {
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void setBack(String uuid, Pos pos) {
        backPoints.put(uuid, pos);
        save();
    }

    public static void setBack(ServerPlayerEntity player) {
        backPoints.put(player.getUuidAsString(), new Pos(player.getPos(), player));
        save();
    }

    public static void addWarp(String key, ServerPlayerEntity player) {
        warpPoints.put(key, new Pos(player.getPos(), player));
        save();
    }

    public static void remove(String arg) {
        warpPoints.remove(arg);
        save();
    }

    public static void sethome(ServerPlayerEntity player) {
        homePoints.put(player.getUuidAsString(), new Pos(player.getPos(), player));
        save();
    }


    private static void save() {
        JsonObject warps = JsonUtil.gson.toJsonTree(warpPoints).getAsJsonObject();
        JsonObject homes = JsonUtil.gson.toJsonTree(homePoints).getAsJsonObject();
        JsonObject backs = JsonUtil.gson.toJsonTree(backPoints).getAsJsonObject();
        JsonObject json = new JsonObject();
        json.add("warps", warps);
        json.add("homes", homes);
        json.add("backs", backs);
        try {
            FileUtil.writeFile(file, JsonUtil.gson.toJson(json), false);
        } catch (IOException e) {
            SuikaServer.LOGGER.warn("suika mod warp_point file write failed...");
        }
    }

    private static void load() throws FileNotFoundException {
        warpPoints.clear();
        homePoints.clear();
        backPoints.clear();
        JsonObject json = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
        Map<String, Pos> homes = JsonUtil.fromJson(json.get("homes"), new TypeToken<Map<String, Pos>>() {
        }.getType());
        Map<String, Pos> warps = JsonUtil.fromJson(json.get("warps"), new TypeToken<Map<String, Pos>>() {
        }.getType());
        Map<String, Pos> backs = JsonUtil.fromJson(json.get("backs"), new TypeToken<Map<String, Pos>>() {
        }.getType());
        warpPoints.putAll(warps);
        homePoints.putAll(homes);
        backPoints.putAll(backs);
    }


    public static boolean back(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        if (backPoints.isEmpty())
            return false;
        if (backPoints.containsKey(uuid)) {
            Pos pos = backPoints.get(uuid);
            ServerWorld serverWorld = pos.getServerWorld(player);
            Vec3d vec3d = pos.position;
            teleport(player, serverWorld, vec3d.getX(), vec3d.getY(), vec3d.getZ(), player.yaw, player.pitch);
            player.sendMessage(new LiteralText("返回上一个标记点"), true);
            return true;
        }
        player.sendMessage(new LiteralText("记录中没有发现标记点"), true);
        return false;
    }

    public static boolean home(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        if (homePoints.isEmpty())
            return false;
        if (homePoints.containsKey(uuid)) {
            Pos pos = homePoints.get(uuid);
            ServerWorld serverWorld = pos.getServerWorld(player);
            Vec3d vec3d = pos.position;
            teleport(player, serverWorld, vec3d.getX(), vec3d.getY(), vec3d.getZ(), player.yaw, player.pitch);
            return true;
        }
        return false;
    }


    public static boolean warp(ServerPlayerEntity player, String key) {
        if (warpPoints.isEmpty()) {
            return false;
        }
        if (!warpPoints.containsKey(key))
            return false;
        Pos pos = warpPoints.get(key);
        ServerWorld serverWorld = pos.getServerWorld(player);
        Vec3d vec3d = pos.position;
        teleport(player, serverWorld, vec3d.getX(), vec3d.getY(), vec3d.getZ(), player.yaw, player.pitch);
        return true;
    }


    public static class Pos {
        public Vec3d position;
        public String dimensionKey;

        public Pos(Vec3d position, DimensionType dimensionType) {
            this.position = position;
            Identifier skyProperties = (Identifier) ReflectionUtils.getPrivateFieldValueByType(dimensionType, DimensionType.class, Identifier.class, 4);
            assert skyProperties != null;
            this.dimensionKey = skyProperties.toString();
        }

        public Pos(Vec3d position, ServerPlayerEntity player) {
            this(position, player.getServerWorld().getDimension());
        }

        public RegistryKey<World> getWorldKey() {
            return RegistryKey.of(Registry.DIMENSION, Identifier.tryParse(dimensionKey));
        }

        public ServerWorld getServerWorld(ServerPlayerEntity playerEntity) {
            ServerWorld serverWorld = Objects.requireNonNull(playerEntity.getServer()).getWorld(getWorldKey());
            return serverWorld != null ? serverWorld : playerEntity.getServer().getWorld(World.OVERWORLD);
        }
    }


    public static void teleport(ServerPlayerEntity player, ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch) {
        player.teleport(targetWorld, x, y, z, yaw, pitch);
    }
}
