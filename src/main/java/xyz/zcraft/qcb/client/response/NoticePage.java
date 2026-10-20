package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record NoticePage(
        List<Notice> notices,
        List<Notice> recentNotices,
        Boolean hasMore,
        String attachInfo,
        Integer count,
        String status,
        String message
) {
    public NoticePage {
        notices = notices == null ? List.of() : List.copyOf(notices);
        recentNotices = recentNotices == null ? List.of() : List.copyOf(recentNotices);
    }

}
