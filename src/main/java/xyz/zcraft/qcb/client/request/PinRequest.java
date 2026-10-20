package xyz.zcraft.qcb.client.request;

/**
 * Original feed author/time; channelId is needed for board pinning.
 */
public record PinRequest(
        String guildId,
        String channelId,
        String userId,
        String createTime,
        int topType
) {
}

