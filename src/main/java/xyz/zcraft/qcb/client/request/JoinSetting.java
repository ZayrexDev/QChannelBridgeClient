package xyz.zcraft.qcb.client.request;

import com.google.gson.JsonObject;

/**
 * Quiz JSON is kept verbatim because the CLI schema does not describe its structure.
 */
public record JoinSetting(
        QuestionSetting question,
        JsonObject quiz
) {
}

