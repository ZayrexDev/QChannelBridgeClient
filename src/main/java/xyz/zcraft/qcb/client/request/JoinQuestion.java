package xyz.zcraft.qcb.client.request;

/**
 * answer may be null when administrator review is required.
 */
public record JoinQuestion(
        String title,
        String answer
) {
}

