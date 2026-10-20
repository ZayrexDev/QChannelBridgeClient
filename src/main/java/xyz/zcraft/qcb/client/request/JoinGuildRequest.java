package xyz.zcraft.qcb.client.request;

import com.google.gson.JsonArray;

/**
 * Answers are the opaque answer array returned/required by CLI verification.
 */
public record JoinGuildRequest(
        String joinGuildComment,
        JsonArray joinGuildAnswers
) {
}

