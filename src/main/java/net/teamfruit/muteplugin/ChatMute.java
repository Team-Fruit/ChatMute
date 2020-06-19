package net.teamfruit.muteplugin;

import com.google.gson.Gson;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class ChatMute extends JavaPlugin {
    Gson config;

    @Override
    public void onEnable() {
        Log.log = getLogger();

        // Plugin startup logic
        config = new YamlConfiguration();
        try {
            File configPath = new File(getFile(), "mutes.yml");
            if (configPath.exists())
                config.load(configPath);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Couldn't load mute list", e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("引数が足らんぞ");
            return false;
        }
        if ("mute".equalsIgnoreCase(label)) {
            Player player = Bukkit.getServer().getPlayer(args[0]);
            if (player != null) {
                config.se
            }
        }
        return false;
    }
}
