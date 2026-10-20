package xyz.zcraft.qcb.client.response;

import java.util.List;

/** Structured CLI fields; the full response, including unknown fields, is available through ApiResponse.raw(). */
public record NoticeStatus(
        Boolean active,
        String message,
        String status,
        String platform,
        String pushMode,
        List<NoticeRoute> registeredRoutes
) {
    public NoticeStatus {
        registeredRoutes = registeredRoutes == null ? List.of() : List.copyOf(registeredRoutes);
    }

}
