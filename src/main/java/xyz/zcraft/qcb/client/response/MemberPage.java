package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record MemberPage(
        List<Member> owners,
        List<Member> admins,
        List<Member> robots,
        List<Member> aiMembers,
        List<Member> members,
        Boolean hasMore,
        String nextPageToken,
        String nextPos,
        Long matchCount,
        Long totalFetched,
        String totalFetchedNote
) {
    public MemberPage {
        owners = owners == null ? List.of() : List.copyOf(owners);
        admins = admins == null ? List.of() : List.copyOf(admins);
        robots = robots == null ? List.of() : List.copyOf(robots);
        aiMembers = aiMembers == null ? List.of() : List.copyOf(aiMembers);
        members = members == null ? List.of() : List.copyOf(members);
    }

}
