package xyz.zcraft.qcb.client.response;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CliVersion(
        String version
) {
}
