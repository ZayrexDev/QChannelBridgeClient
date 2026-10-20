package xyz.zcraft.qcb.client;

import com.google.gson.JsonElement;
import xyz.zcraft.qcb.client.response.ApiError;

/**
 * HTTP/API failures retain the complete JSON response. Transport failures use HTTP status 0.
 */
public class QcbApiException extends RuntimeException {
    private final int httpStatus;
    private final Integer errorCode;
    private final JsonElement response;

    public QcbApiException(String message, int httpStatus, Integer errorCode, JsonElement response, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.response = response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public JsonElement getResponse() {
        return response;
    }

    /** Structured bridge/CLI error, or null when the server did not return a bridge error body. */
    public ApiError getError() {
        if (response == null || !response.isJsonObject()) return null;
        JsonElement data = response.getAsJsonObject().get("data");
        if (data == null || !data.isJsonObject()) return null;
        try {
            return ResponseParser.read(data, ApiError.class);
        } catch (com.google.gson.JsonParseException | IllegalStateException e) {
            return null;
        }
    }
}
