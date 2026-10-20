package xyz.zcraft.qcb.client.request;

/**
 * guildId/channelId are required by the CLI for reply paging.
 */
public record RepliesQuery(
        String guildId,
        String channelId,
        Integer count,
        String attachInfo
) {
}

