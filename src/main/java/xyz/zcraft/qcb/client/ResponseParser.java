package xyz.zcraft.qcb.client;

import com.google.gson.*;
import xyz.zcraft.qcb.client.response.*;

import java.lang.reflect.Type;

/** Wire compatibility is kept here; business methods only choose their result type. */
final class ResponseParser {
    private static final Gson JSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setStrictness(Strictness.STRICT)
            .registerTypeAdapter(MediaImage.class, (JsonDeserializer<MediaImage>) (value, type, context) -> {
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    return new MediaImage(value.getAsString(), null, null, null);
                }
                if (!value.isJsonObject()) throw new JsonParseException("Expected image URL or image object");
                JsonObject image = value.getAsJsonObject();
                return new MediaImage(string(image, "url", "picUrl", "pic_url"), string(image, "picId", "pic_id"),
                        integer(image, "width"), integer(image, "height"));
            })
            .registerTypeAdapter(RichText.class, (JsonDeserializer<RichText>) (value, type, context) -> {
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    return new RichText(value.getAsString(), null, null, null);
                }
                // A separate Gson avoids recursing into this adapter for the object itself.
                return ResponseParser.RICH_TEXT_JSON.fromJson(value, RichText.class);
            }).create();

    private static final Gson RICH_TEXT_JSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(MediaImage.class, (JsonDeserializer<MediaImage>) (value, type, context) ->
                    JSON.fromJson(value, MediaImage.class)).create();

    static <T> T read(JsonElement payload, Type type) {
        if (payload == null || payload.isJsonNull()) return null;
        if (type == Feed.class && payload.isJsonObject() && payload.getAsJsonObject().has("feed")) {
            payload = payload.getAsJsonObject().get("feed");
        }
        if (payload.isJsonArray()) {
            String field = type == FeedPage.class ? "feeds" : type == CommentPage.class ? "comments"
                    : type == ReplyPage.class ? "replies" : type == ChannelList.class ? "channels"
                    : type == MemberPage.class ? "members" : type == NoticePage.class ? "notices" : null;
            if (field != null) {
                JsonObject wrapper = new JsonObject();
                wrapper.add(field, payload);
                payload = wrapper;
            }
        }
        if (type == OperationResult.class && !payload.isJsonObject()) {
            JsonObject wrapper = new JsonObject();
            if (payload.isJsonPrimitive() && payload.getAsJsonPrimitive().isString()) wrapper.add("message", payload);
            else wrapper.add("result", payload);
            payload = wrapper;
        }
        return JSON.fromJson(payload, type);
    }

    static Hint hint(JsonElement cli, JsonElement payload, String name) {
        JsonElement value = cli != null && cli.isJsonObject() ? cli.getAsJsonObject().get(name) : null;
        if (value == null && payload != null && payload.isJsonObject()) value = payload.getAsJsonObject().get(name);
        return value == null || value.isJsonNull() ? null : read(value, Hint.class);
    }

    private static String string(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object.get(key);
            if (value != null && !value.isJsonNull()) return value.getAsString();
        }
        return null;
    }

    private static Integer integer(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsInt();
    }

    private ResponseParser() {}
}
