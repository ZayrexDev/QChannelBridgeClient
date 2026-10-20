package xyz.zcraft.qcb.client.request;

import java.util.List;

/**
 * Original post/comment metadata plus content and optional reply target.
 */
public record ReplyRequest(
        String guildId,
        String channelId,
        String feedAuthorId,
        String feedCreateTime,
        String commentAuthorId,
        String commentCreateTime,
        String replierId,
        String content,
        String imagePath,
        List<AtUser> atUsers,
        List<TextLink> urls,
        String targetReplyId,
        String targetUserId,
        String targetUserNick
) {
    public ReplyRequest {
        atUsers = atUsers == null ? null : List.copyOf(atUsers);
        urls = urls == null ? null : List.copyOf(urls);
    }

    public ReplyRequest(String guildId, String channelId, String feedAuthorId, String feedCreateTime,
                        String commentAuthorId, String commentCreateTime, String replierId, String content) {
        this(guildId, channelId, feedAuthorId, feedCreateTime, commentAuthorId, commentCreateTime,
                replierId, content, null, null, null, null, null, null);
    }
}

