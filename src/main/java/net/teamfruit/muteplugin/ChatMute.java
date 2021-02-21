package net.teamfruit.muteplugin;

import com.google.common.base.Predicates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public final class ChatMute extends JavaPlugin implements Listener {
    File mutedPath;
    MuteModel muted;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Log.log = getLogger();
        this.saveDefaultConfig();

        mutedPath = new File(getDataFolder(), "mutes.json");
        muted = DataUtils.loadFileIfExists(mutedPath, MuteModel.class, "Muted Player List");
        if (muted == null)
            muted = new MuteModel();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        MuteModel.MuteProfile profile = muted.mutes.computeIfAbsent(uuid, id -> new MuteModel.MuteProfile(uuid, player.getName(), EnumSet.of(MuteModel.MuteMode.REDUCE)));
        EnumSet<MuteModel.MuteMode> set = profile.modes;
        if (set == null)
            set = EnumSet.noneOf(MuteModel.MuteMode.class);
        if (set.contains(MuteModel.MuteMode.MUTE)) {
            event.setCancelled(true);
            player.sendMessage("発言権がありません");
            return;
        }
        if (set.contains(MuteModel.MuteMode.REDUCE)) {
            long time = System.currentTimeMillis();
            long duration = time - profile.lastChat;
            long cooldown = getConfig().getInt("cooldown", 10000);
            if (duration < cooldown) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[かめすたプラグイン] " + ChatColor.RED + "わお！とっても香ばしいメッセージですね！あと" + ((cooldown - duration) / 1000) + "秒待ってください！");
                return;
            } else {
                profile.lastChat = time;
            }
        }
        if (set.contains(MuteModel.MuteMode.BLANK)) {
            event.setMessage("");
        }
        if (set.contains(MuteModel.MuteMode.ASTERISK)) {
            event.setMessage(event.getMessage().replaceAll(".", "*"));
        }
        if (set.contains(MuteModel.MuteMode.GARBLED)) {
            String text = event.getMessage();
            text = new String(text.getBytes(), Charset.forName("Shift_JIS"));
            event.setMessage(text);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("引数が足らんぞ");
            return false;
        }
        if ("mute".equalsIgnoreCase(label)) {
            String playerName = args[0];
            List<Player> players = Bukkit.selectEntities(sender, playerName).stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .collect(Collectors.toList());
            if (players.isEmpty()) {
                sender.sendMessage("プレイヤーが見つかりません");
                return true;
            }
            EnumSet<MuteModel.MuteMode> set;
            if (args.length > 1) {
                set = Arrays.stream(args)
                        .skip(1)
                        .map(MuteModel.MuteMode::from)
                        .filter(Predicates.notNull())
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(MuteModel.MuteMode.class)));
            } else {
                set = EnumSet.of(MuteModel.MuteMode.MUTE);
            }
            players.forEach(player -> {
                String uuid = player.getUniqueId().toString();
                muted.mutes.put(uuid, new MuteModel.MuteProfile(uuid, player.getName(), set));
            });
            DataUtils.saveFile(mutedPath, MuteModel.class, muted, "Muted Player List");
            sender.sendMessage(playerName + "を" + set.stream().map(e -> e.title).collect(Collectors.joining("と")) + "にしました");
            return true;
        }
        if ("unmute".equalsIgnoreCase(label)) {
            String playerName = args[0];
            List<Player> players = Bukkit.selectEntities(sender, playerName).stream()
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .collect(Collectors.toList());
            muted.mutes.values().removeIf(e -> players.stream().map(p -> p.getPlayerProfile().getId()).anyMatch(x -> e.id.equals(x.toString())));
            DataUtils.saveFile(mutedPath, MuteModel.class, muted, "Muted Player List");
            sender.sendMessage(players.stream().map(Player::getName).collect(Collectors.joining(", ")) + "のミュートを解除しました");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> pp = new ArrayList<>();
        if (args.length == 1) {
            List<String> listbase = getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            String str = args[0];
            if (str.length() < 1) {
                return listbase;
            } else {
                for (String s : listbase) {
                    if (s.startsWith(str))
                        pp.add(s);
                }
            }
        } else if (args.length >= 2) {
            List<String> listbase = Arrays.stream(MuteModel.MuteMode.values()).map(e -> e.name().toLowerCase()).collect(Collectors.toList());
            String str = args[args.length - 1];
            if (str.length() < 1) {
                return listbase;
            } else {
                for (String s : listbase) {
                    if (s.startsWith(str))
                        pp.add(s);
                }
            }
        }
        return pp;
    }
}
