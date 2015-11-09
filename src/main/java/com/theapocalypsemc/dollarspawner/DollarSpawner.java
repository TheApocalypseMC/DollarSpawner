/*
 * Copyright (c) 2015 SirFaizdat. All rights reserved.
 * The Apocalypse MC internal use only.
 */

package com.theapocalypsemc.dollarspawner;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class for DollarSpawner.
 *
 * @author SirFaizdat
 */
public class DollarSpawner extends JavaPlugin {

    // == Variables

    public static final String LOG_PREFIX = color("&7[&aDollar&cSpawner&7]&r");
    public Economy economy;

    public void onEnable() {
        this.saveDefaultConfig();

        if (!setupEconomy()) {
            log("&cFailed to set up economy. &7Make sure an economy plugin is installed.");
            suicide();
            return;
        }



        getCommand("spawner").setExecutor(new SpawnerCommand(this));


        // A quick little reload command, nothing special

        // This is so that the dsreload executor can access an instance of the plugin class.
        // I know it's a dirty fix, but it's what I got!
        final DollarSpawner instance = this;
        getCommand("dsreload").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                reloadConfig();
                // Reset the
                getCommand("spawner").setExecutor(new SpawnerCommand(instance));
                sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                return true;
            }
        });

        log("&7Enabled &b" + getDescription().getFullName() + "&7. Crafted with <3 by &bSirFaizdat&7.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    // == Public utility methods

    /**
     * Disable all loaded modules, and then the plugin. Use this wisely.
     */
    public void suicide() {
        log("&cI go die now.");
        getServer().getPluginManager().disablePlugin(this);
    }

    // == Logger methods

    public static void log(String message, Throwable t) {
        CommandSender consoleSender = Bukkit.getConsoleSender();
        String coloredMessage = LOG_PREFIX + color(" " + message);
        // Some consoles don't support colorful messages
        if (consoleSender == null) {
            Bukkit.getLogger().info(ChatColor.stripColor(coloredMessage));
            return;
        }
        consoleSender.sendMessage(coloredMessage);
        // Print the stack trace, if a throwable was provided
        if (t != null) {
            log("&cThe stack trace is as follows; please send this to the developer.");
            t.printStackTrace();
        }
    }

    public static void log(String message) {
        log(message, null);
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
