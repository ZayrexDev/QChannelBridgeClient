package xyz.zcraft.qcb.client.response;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record OperationResult(
        String status,
        String message,
        String guildId,
        String channelId,
        String feedId,
        String commentId,
        String replyId,
        String roleId,
        String shareUrl,
        Boolean needVerification,
        @SerializedName(value = "retCode", alternate = {"retcode", "ret_code"}) Long retCode,
        JsonElement questions,
        JsonElement result
) {
}
