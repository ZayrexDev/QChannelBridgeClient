package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record GuildList(
        List<GuildSummary> createdGuilds,
        List<GuildSummary> managedGuilds,
        List<GuildSummary> joinedGuilds,
        Long totalCount
) {
    public GuildList {
        createdGuilds = createdGuilds == null ? List.of() : List.copyOf(createdGuilds);
        managedGuilds = managedGuilds == null ? List.of() : List.copyOf(managedGuilds);
        joinedGuilds = joinedGuilds == null ? List.of() : List.copyOf(joinedGuilds);
    }

}
