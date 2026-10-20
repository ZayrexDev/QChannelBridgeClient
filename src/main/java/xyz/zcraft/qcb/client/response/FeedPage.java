package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record FeedPage(
        List<Feed> feeds,
        Boolean hasMore,
        String feedAttachInfo,
        String feedAttchInfo,
        String cookie,
        String nextPageCookie,
        Long totalCount
) {
    public FeedPage {
        feeds = feeds == null ? List.of() : List.copyOf(feeds);
    }

    public String nextCursor() {
        if (feedAttachInfo != null) return feedAttachInfo;
        if (feedAttchInfo != null) return feedAttchInfo;
        return cookie != null ? cookie : nextPageCookie;
    }
}
