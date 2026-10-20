package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record ChannelList(
        String guildId,
        Long count,
        List<Channel> channels
) {
    public ChannelList {
        channels = channels == null ? List.of() : List.copyOf(channels);
    }

}
