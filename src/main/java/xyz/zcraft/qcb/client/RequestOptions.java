package xyz.zcraft.qcb.client;

/**
 * Controls the bridge query options, never CLI business parameters.
 */
public record RequestOptions(boolean dryRun, boolean confirm) {
    public static final RequestOptions DEFAULT = new RequestOptions(false, false);
    public static final RequestOptions PREVIEW = new RequestOptions(true, false);
    public static final RequestOptions CONFIRMED = new RequestOptions(false, true);
}
