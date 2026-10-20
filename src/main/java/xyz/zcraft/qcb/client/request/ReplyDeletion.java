package xyz.zcraft.qcb.client.request;

/**
 * Metadata required to delete a reply.
 */
public record ReplyDeletion(
        String guildId,
        String channelId,
        String feedAuthorId,
        String feedCreateTime,
        String commentAuthorId,
        String commentCreateTime,
        String replierId
) {
}

