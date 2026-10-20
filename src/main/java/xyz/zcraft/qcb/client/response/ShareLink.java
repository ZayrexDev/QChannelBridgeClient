package xyz.zcraft.qcb.client.response;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record ShareLink(
        String guildId,
        String feedId,
        String shareUrl
) {
}
