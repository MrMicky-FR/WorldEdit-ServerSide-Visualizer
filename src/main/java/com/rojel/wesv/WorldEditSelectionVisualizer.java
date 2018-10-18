package com.rojel.wesv;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.HandSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class WorldEditSelectionVisualizer extends JavaPlugin {

    private Configuration config;
    private WorldEditHelper worldEditHelper;
    private ShapeHelper shapeHelper;
    private boolean faweEnabled;

    private final List<UUID> shown = new ArrayList<>();
    private final List<UUID> lastSelectionTooLarge = new ArrayList<>();
    private final Map<UUID, Region> lastSelectedRegions = new HashMap<>();
    private final Map<UUID, Integer> fadeOutTasks = new HashMap<>();
    private final Map<UUID, Collection<Vector>> playerParticleMap = new HashMap<>();

    @Override
    public void onEnable() {
        config = new Configuration(this);
        config.load();
        worldEditHelper = new WorldEditHelper(this);
        shapeHelper = new ShapeHelper(this);

        new ParticleTask(this);

        getServer().getPluginManager().registerEvents(new WesvListener(this), this);
        getCommand("wesv").setExecutor(new CommandWesv(this));

        for (final Player player : getServer().getOnlinePlayers()) {
            addPlayer(player);
        }

        faweEnabled = getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null;

        MetricsUtils.register(this);

        if (config.isUpdateCheckerEnabled()) {
            getServer().getScheduler().runTaskAsynchronously(this, this::checkUpdate);
        }
    }

    @SuppressWarnings("deprecation")
    public boolean isHoldingSelectionItem(final Player player) {
        final ItemStack item = player.getItemInHand();
        if (item != null) {
            try {
                final Field wandItemField = LocalConfiguration.class.getDeclaredField("wandItem");

                if (wandItemField.getType() == int.class) {
                    return item.getType().getId() == wandItemField.getInt(WorldEdit.getInstance().getConfiguration());
                } else if (wandItemField.getType() == String.class) {
                    final String itemTypeId = BukkitAdapter.adapt(player).getItemInHand(HandSide.MAIN_HAND).getType().getId();
                    if (itemTypeId.equals(wandItemField.get(WorldEdit.getInstance().getConfiguration()))) {
                        return true;
                    }
                    final String secondItemTypeId = BukkitAdapter.adapt(player).getItemInHand(HandSide.OFF_HAND).getType().getId();
                    return secondItemTypeId.equals(wandItemField.get(WorldEdit.getInstance().getConfiguration()));
                }
            } catch (ReflectiveOperationException e) {
                getLogger().log(Level.WARNING, "An error occured on isSelectionItem", e);
            }
        }

        return false;
    }

    public boolean isSelectionShown(final Player player) {
        return shown.contains(player.getUniqueId()) && shouldShowSelection(player);
    }

    public boolean shouldShowSelection(final Player player) {
        return config.isEnabled(player)
                && (!config.isCheckForAxeEnabled() || isHoldingSelectionItem(player));
    }

    public void showSelection(final Player player) {
        if (!player.hasPermission("wesv.use")) {
            return;
        }

        final Region region = worldEditHelper.getSelectedRegion(player);
        final UUID uuid = player.getUniqueId();

        if (region != null && region.getArea() > config.getMaxSize()) {
            setParticlesForPlayer(player, null);

            if (!lastSelectionTooLarge.contains(uuid)) {
                player.sendMessage(config.getLangMaxSelection().replace("%blocks%", Integer.toString(config.getMaxSize())));
                lastSelectionTooLarge.add(uuid);
            }
        } else {
            lastSelectionTooLarge.remove(player.getUniqueId());

            if (region != null && region.getWorld() != null && region.getWorld().getName().equals(player.getWorld().getName())) {
                setParticlesForPlayer(player, shapeHelper.getVectorsFromRegion(region));
            } else {
                setParticlesForPlayer(player, null);
            }
        }
        shown.add(player.getUniqueId());
    }

    public void hideSelection(final Player player) {
        shown.remove(player.getUniqueId());
        playerParticleMap.remove(player.getUniqueId());
        cancelAndRemoveFadeOutTask(player.getUniqueId());
    }

    public void setParticlesForPlayer(final Player player, final Collection<Vector> vectors) {
        cancelAndRemoveFadeOutTask(player.getUniqueId());

        if (vectors == null || vectors.isEmpty()) {
            playerParticleMap.remove(player.getUniqueId());
        } else {
            playerParticleMap.put(player.getUniqueId(), vectors);

            final int fade = config.getParticleFadeDelay();

            if (fade > 0) {
                final int id = getServer().getScheduler().runTaskLater(this, () -> {

                    fadeOutTasks.remove(player.getUniqueId());
                    playerParticleMap.remove(player.getUniqueId());
                }, fade).getTaskId();

                fadeOutTasks.put(player.getUniqueId(), id);
            }
        }
    }

    private void cancelAndRemoveFadeOutTask(final UUID uuid) {
        if (fadeOutTasks.containsKey(uuid)) {
            getServer().getScheduler().cancelTask(fadeOutTasks.get(uuid));
            fadeOutTasks.remove(uuid);
        }
    }

    public void addPlayer(final Player player) {
        if (shouldShowSelection(player)) {
            showSelection(player);
        }
    }

    public void removePlayer(final Player player) {
        final UUID uuid = player.getUniqueId();
        shown.remove(uuid);
        lastSelectionTooLarge.remove(uuid);
        lastSelectedRegions.remove(uuid);
        playerParticleMap.remove(uuid);

        cancelAndRemoveFadeOutTask(uuid);
    }

    public boolean isFaweEnabled() {
        return faweEnabled;
    }

    public Configuration getCustomConfig() {
        return config;
    }

    public Map<UUID, Region> getLastSelectedRegions() {
        return lastSelectedRegions;
    }

    public Map<UUID, Collection<Vector>> getPlayerParticleMap() {
        return playerParticleMap;
    }

    private void checkUpdate() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=17311");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String lastVersion = reader.readLine();
                if (!getDescription().getVersion().equalsIgnoreCase(lastVersion)) {
                    getLogger().warning("A new version is available ! Last version is " + lastVersion + " and you are on " + getDescription().getVersion());
                    getLogger().warning("You can download it on: " + getDescription().getWebsite());
                }
            }
        } catch (IOException e) {
            // Don't display an error
        }
    }
}
