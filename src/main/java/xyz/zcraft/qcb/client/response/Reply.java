package xyz.zcraft.qcb.client.response;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Reply(
        String replyId,
        String author,
        String authorId,
        RichText content,
        String contentText,
        String createTime,
        Long createTimeRaw,
        String targetReplyId,
        String targetUserId,
        String targetUser,
        Long likeCount
) {
}
