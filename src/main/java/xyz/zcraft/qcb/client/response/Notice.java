package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Notice(
        Integer ref,
        String source,
        String type,
        String guildId,
        String guildName,
        String feedId,
        String commentId,
        String replyId,
        String noticeId,
        String posterId,
        String posterNick,
        String text,
        String message,
        String createdAt,
        Long ts,
        Boolean pushed,
        List<NoticeAction> noticeActionList
) {
    public Notice {
        noticeActionList = noticeActionList == null ? List.of() : List.copyOf(noticeActionList);
    }

}
