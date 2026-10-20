package xyz.zcraft.qcb.client.request;

/**
 * joinType is the CLI JOIN_GUILD_TYPE_* value.
 */
public record JoinSettings(
        String joinType,
        JoinSetting setting
) {
}

