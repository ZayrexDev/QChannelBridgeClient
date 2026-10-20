package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Comment(
        String commentId,
        Long commentIndex,
        String author,
        String authorId,
        RichText content,
        String contentText,
        String createTime,
        Long createTimeRaw,
        Long likeCount,
        Long replyCount,
        Boolean hasMoreReplies,
        String attachInfo,
        List<Reply> repliesPreview
) {
    public Comment {
        repliesPreview = repliesPreview == null ? List.of() : List.copyOf(repliesPreview);
    }

}
