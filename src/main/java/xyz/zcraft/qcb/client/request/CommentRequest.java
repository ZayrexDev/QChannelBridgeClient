package xyz.zcraft.qcb.client.request;

import java.util.List;

/**
 * Provide content or imagePath; feedCreateTime is the original Unix seconds.
 */
public record CommentRequest(
        String guildId,
        String channelId,
        String feedCreateTime,
        String content,
        String imagePath,
        List<AtUser> atUsers,
        List<TextLink> urls
) {
    public CommentRequest {
        atUsers = atUsers == null ? null : List.copyOf(atUsers);
        urls = urls == null ? null : List.copyOf(urls);
    }

    public CommentRequest(String guildId, String channelId, String feedCreateTime, String content) {
        this(guildId, channelId, feedCreateTime, content, null, null, null);
    }
}

