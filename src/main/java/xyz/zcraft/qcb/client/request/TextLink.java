package xyz.zcraft.qcb.client.request;

import com.google.gson.annotations.SerializedName;

/**
 * Clickable link; displayText deliberately uses the CLI camelCase spelling.
 */
public record TextLink(
        String url,
        @SerializedName("displayText") String displayText
) {
}

