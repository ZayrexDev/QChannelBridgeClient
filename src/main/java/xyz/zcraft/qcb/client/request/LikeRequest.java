package xyz.zcraft.qcb.client.request;

/**
 * likeType: 3/4 like/unlike comment; 5/6 like/unlike reply.
 */
public record LikeRequest(
        int likeType,
        String guildId,
        String channelId,
        String feedAuthorId,
        String feedCreateTime,
        String commentAuthorId,
        String replyId,
        String replyAuthorId,
        Integer commentLikeCount,
        Integer replyLikeCount
) {
}

