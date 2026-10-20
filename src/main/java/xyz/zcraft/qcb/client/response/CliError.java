package xyz.zcraft.qcb.client.response;

import com.google.gson.*;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CliError(
        String type,
        String message,
        String hint,
        String field,
        JsonElement detail
) {
}
