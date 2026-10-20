package xyz.zcraft.qcb.client.response;

import java.util.List;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record GuildSearchResult(
        List<GuildSummary> channels,
        List<Feed> feeds,
        JsonElement authors,
        Boolean hasMore,
        @SerializedName(value = "isEnd", alternate = {"is_end"}) Integer isEnd,
        String nextPageToken
) {
    public GuildSearchResult {
        channels = channels == null ? List.of() : List.copyOf(channels);
        feeds = feeds == null ? List.of() : List.copyOf(feeds);
    }

}
