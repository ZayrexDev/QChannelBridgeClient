package xyz.zcraft.qcb.client;

import com.google.gson.JsonElement;
import xyz.zcraft.qcb.client.response.Hint;
import xyz.zcraft.qcb.client.response.Preview;

/** data is the unwrapped business result; raw retains the complete bridge response. */
public record ApiResponse<T>(boolean success, String message, T data, Preview preview,
                             Hint setupHint, Hint subscribeHint, JsonElement raw) {
    public T result() {
        return data;
    }
}
