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
 */package me.lorenzo0111.packselector.database.impl;

import me.lorenzo0111.packselector.PackSelector;
import me.lorenzo0111.packselector.database.DatabaseManager;
import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FlatManager implements DatabaseManager {
    private final PackSelector plugin;
    private final Executor executor;
    private Connection connection;

    public FlatManager(PackSelector plugin) {
        this.plugin = plugin;

        this.executor = (cmd) -> Bukkit.getScheduler().runTaskAsynchronously(plugin,cmd);
    }

    @Override
    public void save(@NotNull Player player, @NotNull Pack pack) {
        CompletableFuture.runAsync(() -> {
            try {
                PreparedStatement first = connection.prepareStatement("DELETE FROM `ps_users` WHERE `uuid` = ?;");
                first.setString(1,player.getUniqueId().toString());

                first.executeUpdate();

                PreparedStatement two = connection.prepareStatement("INSERT INTO `ps_users`(`uuid`, `name`) VALUES (?,?);");
                two.setString(1, player.getUniqueId().toString());
                two.setString(2, pack.getName());

                two.executeUpdate();

                first.close();
                two.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("An error has occurred while saving data: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<@Nullable String> get(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            String data = null;

            try {
                PreparedStatement statement = connection.prepareStatement("SELECT `name` FROM `ps_users` WHERE `uuid` = ?;" );
                statement.setString(1,player.getUniqueId().toString());

                ResultSet set = statement.executeQuery();
                statement.close();
                if (set.next()) {
                    data = set.getString("name");
                }

                set.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("An error has occurred while saving data: " + e.getMessage());
            }

            return data;
        }, executor);
    }

    @Override
    public void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");

        File file = new File(plugin.getDataFolder(), "database.db");
        if (!file.exists() && !file.createNewFile()) {
            plugin.getLogger().severe("An error has occurred while loading database. Shutting down..");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        connection.createStatement()
                .executeUpdate(this.table());
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null) connection.close();
    }

    @Override
    public String getImplementation() {
        return "SQLite";
    }
}
