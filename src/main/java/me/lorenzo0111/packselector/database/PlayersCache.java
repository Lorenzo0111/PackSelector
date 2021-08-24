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
 */package me.lorenzo0111.packselector.database;

import me.lorenzo0111.packselector.objects.Pack;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayersCache {
    private static final Map<Player, Pack> CACHE = new HashMap<>();

    public static void compute(Player player, Pack pack) {
        CACHE.put(player,pack);
    }

    public static Pack get(Player player) {
        return CACHE.get(player);
    }

    public static void remove(Player player) {
        CACHE.remove(player);
    }

    public static boolean has(Player player) {
        return CACHE.containsKey(player);
    }

    public static void update(Player player, Pack newPack) {
        remove(player);
        compute(player,newPack);
    }


    public static Map<Player, Pack> getCache() {
        return CACHE;
    }
}
