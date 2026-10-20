package xyz.zcraft.qcb.client.request;

/**
 * Comment paging and ranking; null fields preserve CLI defaults.
 */
public record CommentsQuery(
        String guildId,
        String channelId,
        Integer count,
        Integer rankType,
        Integer replyListNum,
        String attachInfo
) {
}

