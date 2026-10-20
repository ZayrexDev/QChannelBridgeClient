package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record CommandSchema(
        String command,
        String description,
        String group,
        String risk,
        List<CommandFlag> flags,
        List<String> examples
) {
    public CommandSchema {
        flags = flags == null ? List.of() : List.copyOf(flags);
        examples = examples == null ? List.of() : List.copyOf(examples);
    }

}
