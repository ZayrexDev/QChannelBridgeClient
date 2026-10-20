package xyz.zcraft.qcb.client.request;

/**
 * Image/video path on the bridge machine; dimensions may be omitted.
 */
public record MediaFile(
        String filePath,
        Integer width,
        Integer height
) {
    public MediaFile(String filePath) {
        this(filePath, null, null);
    }
}

