package xyz.zcraft.qcb.client.response;

import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record UserInfo(
        @SerializedName(value = "tiny_id", alternate = {"tinyid", "tinyId"}) String tinyId,
        String nickname,
        String globalNickname,
        String memberName,
        String gender,
        String country,
        String province
) {
}
