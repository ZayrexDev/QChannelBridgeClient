package xyz.zcraft.qcb.client.response;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record OperationDescription(
        String method,
        String path,
        String command,
        @SerializedName(value = "fixedParameters", alternate = {"fixed_parameters"}) JsonObject fixedParameters,
        @SerializedName(value = "requiresConfirmation", alternate = {"requires_confirmation"}) Boolean requiresConfirmation
) {
}
