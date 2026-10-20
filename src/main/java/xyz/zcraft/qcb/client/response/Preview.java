package xyz.zcraft.qcb.client.response;

import com.google.gson.*;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record Preview(
        String command,
        JsonObject params
) {
}
