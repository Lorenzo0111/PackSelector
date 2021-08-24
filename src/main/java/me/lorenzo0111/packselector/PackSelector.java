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
 */package me.lorenzo0111.packselector;

import me.lorenzo0111.packselector.command.PackCommand;
import me.lorenzo0111.packselector.database.DatabaseManager;
import me.lorenzo0111.packselector.database.impl.FlatManager;
import me.lorenzo0111.packselector.database.impl.MySQLManager;
import me.lorenzo0111.packselector.listeners.PlayerListener;
import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PackSelector extends JavaPlugin {
    private final List<Pack> packs = new ArrayList<>();
    private DatabaseManager database;

    @Override
    public void onEnable() {
        this.getLogger().info("Loading configured packs..");

        this.saveDefaultConfig();
        this.reload();

        this.getLogger().info("Loading database connection..");
        if ("MYSQL".equalsIgnoreCase(this.getConfig().getString("database.type", "FLAT"))) {
            this.database = new MySQLManager(this);
        } else {
            this.database = new FlatManager(this);
        }

        try {
            database.connect();
        } catch (Exception e) {
            this.getLogger().severe("An error has occurred while connecting to the database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Loaded database connection. [" + database.getImplementation() + "]");

        this.getLogger().info("Loading listeners..");
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Objects.requireNonNull(this.getCommand("pack")).setExecutor(new PackCommand(this));

        this.getLogger().info("PackSelector loaded.");
    }

    public void reload() {
        this.reloadConfig();
        packs.clear();

        ConfigurationSection section = this.getConfig().getConfigurationSection("packs");
        if (section == null) {
            this.getLogger().severe("Cannot find 'packs' section into the config. Aborting..");
            return;
        }

        for (String key: section.getKeys(false)) {
            String url = section.getString(key);

            Pack pack = new Pack(key,url);

            if (pack.verify()) {
                packs.add(pack);
            } else {
                this.getLogger().warning("The url of the pack '" + pack.getName() + "' is invalid. Ignoring.." );
            }
        }
    }

    @Override
    public void onDisable() {
        if (database != null) {
            this.getLogger().info("Closing database connection..");
            try {
                database.disconnect();
            } catch (SQLException e) {
                this.getLogger().info("An error has occurred while shutting down: " + e.getMessage());
            }
        }
    }

    public List<Pack> getPacks() {
        return packs;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public String getDefault() {
        return this.getConfig().getString("default");
    }
}
