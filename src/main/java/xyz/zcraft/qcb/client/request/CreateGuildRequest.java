package xyz.zcraft.qcb.client.request;

/**
 * Avatar path is on the bridge machine; provide theme or guildName.
 */
public record CreateGuildRequest(
        String imagePath,
        String theme,
        String guildName,
        String guildProfile,
        String communityType
) {
}

