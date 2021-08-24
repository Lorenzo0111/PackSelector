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

import com.glyart.mystral.database.AsyncDatabase;
import com.glyart.mystral.database.Credentials;
import com.glyart.mystral.database.Mystral;
import me.lorenzo0111.packselector.PackSelector;
import me.lorenzo0111.packselector.database.DatabaseManager;
import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.concurrent.CompletableFuture;

public class MySQLManager implements DatabaseManager {
    private final PackSelector plugin;
    private AsyncDatabase database;

    public MySQLManager(PackSelector plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(@NotNull Player player, @NotNull Pack pack) {
        CompletableFuture<Integer> future = database.update("DELETE FROM `ps_users` WHERE `uuid` = ?;", false);
        future.whenComplete((data,ex) -> {
            if (ex != null) {
                plugin.getLogger().severe("An error has occurred while saving data: " + ex.getMessage());
                return;
            }

            database.update("INSERT INTO `ps_users`(`uuid`, `name`) VALUES (?,?);", new Object[]{player.getUniqueId().toString(), pack.getUrl()}, false, Types.VARCHAR, Types.VARCHAR);
        });
    }

    @Override
    public CompletableFuture<@Nullable String> get(@NotNull Player player) {
        return database.queryForObject("SELECT `name` FROM `ps_users` WHERE `uuid` = ?;", new Object[]{player.getUniqueId()} ,(set,i) -> set.getString("url"), Types.VARCHAR);
    }

    @Override
    public void connect() {
        Credentials credentials = Credentials.builder()
                .host(plugin.getConfig().getString("database.host", "localhost"))
                .port(plugin.getConfig().getInt("database.port",3306))
                .user(plugin.getConfig().getString("database.user", "root"))
                .password(plugin.getConfig().getString("database.password"))
                .schema(plugin.getConfig().getString("database.name"))
                .pool("PackSelector Pool")
                .build();

        this.database = Mystral.newAsyncDatabase(credentials, (cmd) -> Bukkit.getScheduler().runTaskAsynchronously(plugin,cmd));
        database.update(this.table(),false);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public String getImplementation() {
        return "MySQL";
    }


}
