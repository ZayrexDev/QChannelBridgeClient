package xyz.zcraft.qcb.client.response;

import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Member(
        @SerializedName(value = "tinyid", alternate = {"tiny_id", "tinyId"}) String tinyId,
        @SerializedName(value = "昵称", alternate = {"nickname", "nick"}) String nickname,
        String role,
        @SerializedName(value = "性别", alternate = {"gender"}) String gender,
        @SerializedName(value = "加入时间", alternate = {"joinTime", "join_time"}) String joinTime,
        @SerializedName(value = "joinTime_human", alternate = {"join_time_human"}) String joinTimeHuman
) {
}
