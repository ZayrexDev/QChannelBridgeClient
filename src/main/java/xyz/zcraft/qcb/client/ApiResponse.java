package xyz.zcraft.qcb.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record ApiResponse(boolean success, String message, JsonElement data) {
    public JsonElement result() {
        if (data != null && data.isJsonObject()) {
            JsonObject object = data.getAsJsonObject();
            if (object.has("success") && object.has("data")) return object.get("data");
        }
        return data;
    }
}
