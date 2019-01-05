package com.rojel.wesv;

import fr.mrmicky.fastparticle.FastParticle;
import fr.mrmicky.fastparticle.ParticleType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.Collection;

public class ClipboardParticleTask extends BukkitRunnable {

    private final WorldEditSelectionVisualizer plugin;

    public ClipboardParticleTask(final WorldEditSelectionVisualizer plugin) {
        this.plugin = plugin;

        runTaskTimer(plugin, 1, plugin.getCustomConfig().getUpdateClipboardParticlesInterval());
    }

    @Override
    public void run() {
        final int particleDistance = plugin.getCustomConfig().getParticleDistance();
        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            final Location loc = player.getLocation();

            /** Handles clipboard particles */
            final Collection<ImmutableVector> clipboardVectors = plugin.getPlayerClipboardParticleMap().get(player.getUniqueId());

            if (clipboardVectors == null || clipboardVectors.isEmpty()) {
                continue;
            }

            for (final ImmutableVector vec : clipboardVectors) {
                if (vec.distanceSquared(loc.getX(), loc.getY(), loc.getZ()) > NumberConversions.square(particleDistance)) {
                    continue;
                }

                final ParticleType particle = plugin.getCustomConfig().getClipboardParticle();
                final Object particleData = plugin.getCustomConfig().getClipboardParticleData();

                FastParticle.spawnParticle(player, particle, vec.getX(), vec.getY(), vec.getZ(), 1, 0.0, 0.0, 0.0, 0.0, particleData);
            }
        }
    }
}
