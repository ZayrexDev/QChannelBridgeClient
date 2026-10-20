package xyz.zcraft.qcb.client.response;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CliFailure(
        Boolean success,
        CliError error,
        @SerializedName(value = "retCode", alternate = {"retcode", "ret_code"}) Long retCode,
        JsonElement data
) {
}
