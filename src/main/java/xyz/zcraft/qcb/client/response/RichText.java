package xyz.zcraft.qcb.client.response;

import java.util.List;
import com.google.gson.*;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record RichText(
        String text,
        List<Mention> atUsers,
        List<MediaImage> images,
        JsonElement sticker
) {
    public RichText {
        atUsers = atUsers == null ? List.of() : List.copyOf(atUsers);
        images = images == null ? List.of() : List.copyOf(images);
    }

}
