package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record ReplyPage(
        List<Reply> replies,
        Boolean hasMore,
        String attachInfo
) {
    public ReplyPage {
        replies = replies == null ? List.of() : List.copyOf(replies);
    }

}
