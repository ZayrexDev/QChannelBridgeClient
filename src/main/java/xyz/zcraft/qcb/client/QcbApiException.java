package xyz.zcraft.qcb.client;

import com.google.gson.JsonElement;

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
}
