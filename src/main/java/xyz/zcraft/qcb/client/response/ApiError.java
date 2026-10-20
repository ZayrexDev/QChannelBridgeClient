package xyz.zcraft.qcb.client.response;

import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record ApiError(
        Integer code,
        @SerializedName(value = "httpStatus", alternate = {"http_status"}) Integer httpStatus,
        CliFailure cli
) {
}
