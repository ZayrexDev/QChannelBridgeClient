package xyz.zcraft.qcb.client.response;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record GuildInfo(
        String guildId,
        String guildNumber,
        String name,
        String profile,
        String avatarUrl,
        String guildType,
        Long memberCount,
        String createTime,
        String createTimeHuman,
        String shareUrl
) {
}
