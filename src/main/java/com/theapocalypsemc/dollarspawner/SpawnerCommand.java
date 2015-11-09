/*
 * Copyright (c) 2015 SirFaizdat. All rights reserved.
 * The Apocalypse MC internal use only.
 */

package com.theapocalypsemc.dollarspawner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The command executor for /spawner.
 *
 * @author SirFaizdat
 */
public class SpawnerCommand implements CommandExecutor {

    private DollarSpawner plugin;
    private Map<String, Double> prices = new HashMap<>();
    private String helpText;
    private NumberFormat c = NumberFormat.getCurrencyInstance();

    public SpawnerCommand(DollarSpawner plugin) {
        this.plugin = plugin;

        // Retrieve a list of mobs that are set in the configuration.
        Set<String> mobs = plugin.getConfig().getConfigurationSection("price").getKeys(false);
        for (String mob : mobs) {
            // Add them to the list
            double price = plugin.getConfig().getDouble("price." + mob + ".price");
            prices.put(mob, price);
        }
        helpText = createHelpText(); // Create the help text after all the mobs are loaded.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(helpText);
            return true;
        }

        // Validate the mob's name

        String mobName = args[0];
        if (!sender.hasPermission("dollarspawner." + mobName) && !sender.hasPermission("dollarspawner.*")) {
            sender.sendMessage(DollarSpawner.color("&cAw nargs! &7You don't have permission to do that. You need the permission &cdollarspawner." + mobName));
            return true;
        }

        if (!prices.containsKey(mobName.toLowerCase().trim())) {
            sender.sendMessage(helpText);
            return true;
        }

        // Get the spawner that they're looking at
        Player player = (Player) sender;
        Set<Material> trans = new HashSet<>();
        trans.add(Material.AIR);
        Block block = player.getTargetBlock(trans, 100);
        if (block.getType() != Material.MOB_SPAWNER) {
            sender.sendMessage(ChatColor.RED + "You need to look at a spawner.");
            return true;
        }
        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();

        if (creatureSpawner.getSpawnedType() == EntityType.fromName(mobName)) {
            sender.sendMessage(ChatColor.RED + "That spawner is already a " + mobName + " spawner.");
            return true;
        }

        // Take their money if they can afford it
        double price = prices.get(mobName.toLowerCase().trim());
        if (!plugin.economy.has(player, price)) {
            sender.sendMessage(ChatColor.RED + "You need " + c.format(price) + " to purchase that.");
            return true;
        }

        // Do it!
        plugin.economy.withdrawPlayer(player, price);
        creatureSpawner.setSpawnedType(EntityType.fromName(mobName));
        creatureSpawner.update();
        sender.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.GRAY + "Enjoy your brand spankin' new spawner!");
        sender.sendMessage(ChatColor.DARK_GRAY + "$" + price + " has been taken from your account.");

        return true;
    }

    private String createHelpText() {
        StringBuilder builder = new StringBuilder();
        builder.append("&bTo use this command&7, look at a &bspawner &7and type &b/spawner&7, followed by one of the following:");
        for (Map.Entry<String, Double> mobs : prices.entrySet()) {
            // &bmobName &7($price),
            builder.append("&b").append(mobs.getKey()).append(" &7(").append(c.format(mobs.getValue())).append("), ");
        }
        // Take out the last comma.
        String returnValue = builder.toString().substring(0, builder.toString().length() - 2);
        return DollarSpawner.color(returnValue);
    }

}
