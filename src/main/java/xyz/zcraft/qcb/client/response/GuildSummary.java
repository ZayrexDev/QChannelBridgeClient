package xyz.zcraft.qcb.client.response;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record GuildSummary(
        String guildId,
        String guildNumber,
        String name,
        String profile,
        Long memberCount,
        String role,
        String shareUrl
) {
}
