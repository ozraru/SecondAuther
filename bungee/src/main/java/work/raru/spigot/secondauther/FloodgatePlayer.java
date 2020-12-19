package work.raru.spigot.secondauther;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FloodgatePlayer {
    /**
     * Bedrock username with > identifier
     */
    private String javaUsername;
    /**
     * The Java UUID used to identify the bedrock client
     */
    private UUID javaUniqueId;

    FloodgatePlayer(UUID uuid, String name, String prefix, boolean replaceSpaces) {
        javaUsername = prefix + name.substring(0, Math.min(name.length(), 16 - prefix.length()));
        if (replaceSpaces) {
            javaUsername = javaUsername.replaceAll(" ", "_");
        }
        javaUniqueId = uuid;
    }
}
