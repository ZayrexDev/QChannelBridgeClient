package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Feed(
        String feedId,
        String guildId,
        String guildName,
        String channelId,
        String channelName,
        String author,
        String authorId,
        String title,
        String content,
        String contentSnippet,
        RichText contentRichtext,
        RichText titleRichtext,
        String createTime,
        Long createTimeRaw,
        Integer feedType,
        Long commentCount,
        Long preferCount,
        String shareUrl,
        List<MediaImage> images
) {
    public Feed {
        images = images == null ? List.of() : List.copyOf(images);
    }

}
