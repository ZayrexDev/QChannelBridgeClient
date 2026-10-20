package xyz.zcraft.qcb.client.request;

import java.util.List;

/**
 * Question-based membership configuration.
 */
public record QuestionSetting(
        List<JoinQuestion> items
) {
    public QuestionSetting {
        items = items == null ? null : List.copyOf(items);
    }

}

