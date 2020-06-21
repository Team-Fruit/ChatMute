package net.teamfruit.muteplugin;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MuteModel {
    public Map<String, MuteProfile> mutes = new HashMap<>();

    public static class MuteProfile {
        public String id;
        public String name;
        public EnumSet<MuteMode> modes;

        public transient long lastChat;

        public MuteProfile(String id, String name, EnumSet<MuteMode> modes) {
            this.id = id;
            this.name = name;
            this.modes = modes;
        }
    }

    public static enum MuteMode {
        MUTE("ミュート"),
        REDUCE("連投禁止"),
        ASTERISK("伏せ字"),
        BLANK("削除"),
        GARBLED("文字化け"),
        ;

        public final String title;

        private MuteMode(String title) {
            this.title = title;
        }

        public static MuteMode from(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
            return null;
        }
    }
}
