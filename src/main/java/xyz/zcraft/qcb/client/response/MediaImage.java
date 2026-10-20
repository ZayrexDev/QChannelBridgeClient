package xyz.zcraft.qcb.client.response;

import com.google.gson.annotations.SerializedName;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record MediaImage(
        @SerializedName(value = "url", alternate = {"picUrl", "pic_url"}) String url,
        @SerializedName(value = "picId", alternate = {"pic_id"}) String picId,
        Integer width,
        Integer height
) {
}
