package xyz.zcraft.qcb.client.request;

/**
 * Metadata required to delete a comment.
 */
public record CommentDeletion(
        String guildId,
        String channelId,
        String feedCreateTime,
        String commentAuthorId
) {
}

