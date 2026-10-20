package xyz.zcraft.qcb.client.response;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record JoinPolicy(
        @SerializedName(value = "joinType", alternate = {"join_type"}) String joinType,
        JsonElement question,
        JsonElement quiz
) {
}
