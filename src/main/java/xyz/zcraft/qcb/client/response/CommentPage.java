package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CommentPage(
        String guildId,
        List<Comment> comments,
        Boolean hasMore,
        String attachInfo
) {
    public CommentPage {
        comments = comments == null ? List.of() : List.copyOf(comments);
    }

}
