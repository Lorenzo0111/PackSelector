/*
 *  This file is part of PackSelector, licensed under the MIT License.
 *
 *  Copyright (c) Lorenzo0111
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */package me.lorenzo0111.packselector.listeners;

import me.lorenzo0111.packselector.PackSelector;
import me.lorenzo0111.packselector.database.PlayersCache;
import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PlayerListener implements Listener {
    private final PackSelector plugin;

    public PlayerListener(PackSelector plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        List<Pack> packs = plugin.getPacks();
        Pack pack = PlayersCache.get(event.getPlayer());

        if (pack != null && packs.contains(pack)) {
            event.getPlayer().setResourcePack(pack.getUrl());
            return;
        }

        plugin.getDatabase()
                .get(event.getPlayer())
                .whenComplete((name,ex) -> {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }

                    if (name != null) {
                        Optional<Pack> data = packs.stream()
                                .filter((filter) -> filter.getName().equals(name))
                                .findFirst();

                        if (data.isPresent()) {
                            PlayersCache.compute(event.getPlayer(), data.get());

                            event.getPlayer().setResourcePack(data.get().getUrl());
                        }

                        return;
                    }

                    PlayersCache.compute(event.getPlayer(), new Pack("default", plugin.getDefault()));

                    event.getPlayer().setResourcePack(plugin.getDefault());
                });
    }

    @EventHandler
    public void onPack(@NotNull PlayerResourcePackStatusEvent event) {
        Pack pack = PlayersCache.get(event.getPlayer());

        if (event.getStatus() == Status.SUCCESSFULLY_LOADED) {
            String string = plugin.getConfig().getString("done-message");
            if (pack != null && string != null) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                                string.replace("%pack-name%", pack.getName())
                                        .replace("%pack-url%", pack.getUrl())));
            }
        } else if (event.getStatus() == Status.FAILED_DOWNLOAD) {
            String string = plugin.getConfig().getString("error-message");
            if (pack != null && string != null) {
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        string.replace("%pack-name%", pack.getName())
                                .replace("%pack-url%", pack.getUrl())));
            }
        } else if (event.getStatus() == Status.DECLINED && plugin.getConfig().getBoolean("kick-refuse")) {
            event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kick-message", "&cYou declined the texture pack.")));
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> PlayersCache.remove(event.getPlayer()), 5 * 20L);
    }
}
