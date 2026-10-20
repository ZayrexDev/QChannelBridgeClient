package xyz.zcraft.qcb.client.response;

import java.util.List;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CommandFlag(
        String name,
        String type,
        Boolean required,
        String description,
        String pattern,
        @SerializedName("default") JsonElement defaultValue,
        @SerializedName("enum") List<String> enumValues
) {
    public CommandFlag {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    }

}
