/*
 *  This file is part of RocketJoin, licensed under the MIT License.
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
 */

package me.lorenzo0111.packselector.command;

import com.google.common.base.Joiner;
import me.lorenzo0111.packselector.PackSelector;
import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PackCommand implements TabExecutor {
    private final PackSelector plugin;

    public PackCommand(PackSelector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eLoaded packs: " + Joiner.on(',').join(this.getNames())));
            return true;
        }

        Optional<Pack> first = plugin.getPacks()
                .stream()
                .filter(pack -> pack.getName().equalsIgnoreCase(args[0]))
                .findFirst();

        if (!first.isPresent()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis pack does not exists."));
            return true;
        }

        Pack pack = first.get();
        ((Player) sender).setResourcePack(pack.getUrl());
        plugin.getDatabase()
                .save((Player) sender, pack);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return this.getNames();
    }

    public List<String> getNames() {
        return plugin.getPacks()
                .stream()
                .map(Pack::getName)
                .collect(Collectors.toList());
    }
}
