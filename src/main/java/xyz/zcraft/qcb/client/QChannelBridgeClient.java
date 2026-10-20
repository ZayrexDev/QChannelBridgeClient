package xyz.zcraft.qcb.client;

import com.google.gson.*;
import xyz.zcraft.qcb.client.request.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Synchronous, reusable client for QChannelBridge. Requires Java 17+.
 * Simple operations take named arguments; complex requests use records.
 * No application-level retry is performed, including for write requests.
 */
public final class QChannelBridgeClient {
    private static final Gson JSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setStrictness(Strictness.STRICT).create();
    private final URI baseUri;
    private final String token;
    private final Duration timeout;
    private final HttpClient http;
    private final RequestOptions options;

    public QChannelBridgeClient(String baseUrl, String token) {
        this(baseUrl, token, Duration.ofSeconds(90));
    }

    public QChannelBridgeClient(String baseUrl, String token, Duration timeout) {
        this(normalizeBase(baseUrl), token, timeout,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NEVER).build(), RequestOptions.DEFAULT);
    }

    private QChannelBridgeClient(URI baseUri, String token, Duration timeout, HttpClient http, RequestOptions options) {
        this.baseUri = baseUri;
        this.token = token;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("timeout must be positive");
        this.http = http;
        this.options = Objects.requireNonNull(options, "options");
    }

    private static JsonObject empty() {
        return new JsonObject();
    }

    private static JsonObject json(Record value) {
        return JSON.toJsonTree(Objects.requireNonNull(value, "request")).getAsJsonObject();
    }

    private static JsonObject params(Object... pairs) {
        JsonObject result = empty();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null) result.add((String) pairs[i], JSON.toJsonTree(pairs[i + 1]));
        }
        return result;
    }

    private static JsonObject merge(JsonObject first, JsonObject second) {
        second.entrySet().forEach(entry -> first.add(entry.getKey(), entry.getValue()));
        return first;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }

    private static List<String> nonEmpty(List<String> values, String name) {
        if (values == null || values.isEmpty()) throw new IllegalArgumentException(name + " must not be empty");
        for (String value : values) required(value, name);
        return List.copyOf(values);
    }

    private static int positive(int ref) {
        if (ref < 1) throw new IllegalArgumentException("ref must be positive");
        return ref;
    }

    private static URI normalizeBase(String baseUrl) {
        URI uri = URI.create(Objects.requireNonNull(baseUrl, "baseUrl"));
        if (uri.getHost() == null || !Set.of("http", "https").contains(
                uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT))
                || uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("baseUrl must be an absolute HTTP(S) URL without credentials, query or fragment");
        }
        String base = uri.toASCIIString().replaceAll("/+$", "");
        return URI.create(base.endsWith("/api") ? base : base + "/api");
    }

    private static String segment(String value) {
        if (value == null || value.isBlank() || value.equals(".") || value.equals("..")) {
            throw new IllegalArgumentException("Resource IDs and command names must not be blank or dot segments");
        }
        return encode(value);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Returns a separate immutable view sharing the same HTTP connection pool.
     */
    public QChannelBridgeClient withOptions(RequestOptions options) {
        return new QChannelBridgeClient(baseUri, token, timeout, http, options);
    }

    /**
     * GET /api/guilds
     */
    public ApiResponse getGuilds() {
        return request("GET", "/guilds", empty());
    }

    /**
     * POST /api/guilds
     */
    public ApiResponse createGuild(CreateGuildRequest request) {
        return request("POST", "/guilds", json(request));
    }

    /**
     * GET /api/guilds/search
     */
    public ApiResponse searchGuilds(String keyword, String scope, String nextPageToken) {
        return request("GET", "/guilds/search", params("keyword", required(keyword, "keyword"), "scope", scope, "next_page_token", nextPageToken));
    }

    public ApiResponse searchGuilds(String keyword) {
        return searchGuilds(keyword, null, null);
    }

    /**
     * GET /api/shares/resolve
     */
    public ApiResponse resolveShare(String url) {
        return request("GET", "/shares/resolve", params("url", required(url, "url")));
    }

    /**
     * GET /api/users/{tinyId}
     */
    public ApiResponse getUserInfo(String tinyId) {
        return request("GET", "/users/" + segment(tinyId), empty());
    }

    /**
     * GET /api/guilds/{guildId}
     */
    public ApiResponse getGuildInfo(String guildId) {
        return request("GET", "/guilds/" + segment(guildId), empty());
    }

    /**
     * PATCH /api/guilds/{guildId}
     */
    public ApiResponse updateGuildInfo(String guildId, String guildName, String guildProfile) {
        return request("PATCH", "/guilds/" + segment(guildId), params("guild_name", guildName, "guild_profile", guildProfile));
    }

    /**
     * PUT /api/guilds/{guildId}/avatar
     */
    public ApiResponse updateGuildAvatar(String guildId, String imagePath) {
        return request("PUT", "/guilds/" + segment(guildId) + "/avatar", params("image_path", required(imagePath, "imagePath")));
    }

    /**
     * PUT /api/guilds/{guildId}/number
     */
    public ApiResponse updateGuildNumber(String guildId, String guildNumber) {
        return request("PUT", "/guilds/" + segment(guildId) + "/number", params("guild_number", required(guildNumber, "guildNumber")));
    }

    /**
     * GET /api/guilds/{guildId}/share-url
     */
    public ApiResponse getGuildShareUrl(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/share-url", empty());
    }

    /**
     * GET /api/guilds/{guildId}/join-settings
     */
    public ApiResponse getJoinSettings(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/join-settings", empty());
    }

    /**
     * PUT /api/guilds/{guildId}/join-settings
     */
    public ApiResponse updateJoinSettings(String guildId, JoinSettings request) {
        return request("PUT", "/guilds/" + segment(guildId) + "/join-settings", json(request));
    }

    /**
     * POST /api/guilds/{guildId}/membership
     */
    public ApiResponse joinGuild(String guildId, JoinGuildRequest request) {
        return request("POST", "/guilds/" + segment(guildId) + "/membership", json(request));
    }

    public ApiResponse joinGuild(String guildId) {
        return joinGuild(guildId, new JoinGuildRequest(null, null));
    }

    /**
     * DELETE /api/guilds/{guildId}/membership
     */
    public ApiResponse leaveGuild(String guildId) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/membership", empty());
    }

    /**
     * GET /api/guilds/{guildId}/members
     */
    public ApiResponse getMembers(String guildId, String nextPageToken) {
        return request("GET", "/guilds/" + segment(guildId) + "/members", params("next_page_token", nextPageToken));
    }

    public ApiResponse getMembers(String guildId) {
        return getMembers(guildId, null);
    }

    /**
     * GET /api/guilds/{guildId}/members/search
     */
    public ApiResponse searchMembers(String guildId, String keyword, Integer num, String nextPos) {
        return request("GET", "/guilds/" + segment(guildId) + "/members/search", params("keyword", required(keyword, "keyword"), "num", num, "next_pos", nextPos));
    }

    public ApiResponse searchMembers(String guildId, String keyword) {
        return searchMembers(guildId, keyword, null, null);
    }

    /**
     * GET /api/guilds/{guildId}/members/{tinyId}
     */
    public ApiResponse getMemberInfo(String guildId, String tinyId) {
        return request("GET", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId), empty());
    }

    /**
     * PUT /api/guilds/{guildId}/members/{tinyId}/mute
     */
    public ApiResponse muteMember(String guildId, String tinyId, long untilTimestamp) {
        return request("PUT", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId) + "/mute", params("time_stamp", Long.toString(untilTimestamp)));
    }

    /**
     * DELETE /api/guilds/{guildId}/members/{tinyId}
     */
    public ApiResponse kickMember(String guildId, String tinyId, boolean blacklist, String revokeMsgs) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId), params("blacklist", blacklist, "revoke_msgs", revokeMsgs));
    }

    public ApiResponse kickMember(String guildId, String tinyId) {
        return kickMember(guildId, tinyId, false, null);
    }

    /**
     * POST /api/guilds/{guildId}/admins
     */
    public ApiResponse addAdmins(String guildId, List<String> tinyIds) {
        return request("POST", "/guilds/" + segment(guildId) + "/admins", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")));
    }

    /**
     * DELETE /api/guilds/{guildId}/admins
     */
    public ApiResponse removeAdmins(String guildId, List<String> tinyIds) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/admins", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")));
    }

    /**
     * GET /api/guilds/{guildId}/channels
     */
    public ApiResponse getChannels(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/channels", empty());
    }

    /**
     * POST /api/guilds/{guildId}/channels
     */
    public ApiResponse createChannel(String guildId, String channelName) {
        return request("POST", "/guilds/" + segment(guildId) + "/channels", params("channel_name", required(channelName, "channelName")));
    }

    /**
     * PATCH /api/guilds/{guildId}/channels/{channelId}
     */
    public ApiResponse updateChannel(String guildId, String channelId, String channelName) {
        return request("PATCH", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId), params("channel_name", required(channelName, "channelName")));
    }

    /**
     * DELETE /api/guilds/{guildId}/channels/{channelId}
     */
    public ApiResponse deleteChannel(String guildId, String channelId) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId), empty());
    }

    /**
     * POST /api/guilds/{guildId}/roles
     */
    public ApiResponse createRole(String guildId, String name) {
        return request("POST", "/guilds/" + segment(guildId) + "/roles", params("name", required(name, "name")));
    }

    /**
     * PATCH /api/guilds/{guildId}/roles/{roleId}
     */
    public ApiResponse updateRole(String guildId, String roleId, String name) {
        return request("PATCH", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId), params("name", required(name, "name")));
    }

    /**
     * POST /api/guilds/{guildId}/roles/{roleId}/members
     */
    public ApiResponse addRoleMembers(String guildId, String roleId, List<String> tinyIds) {
        return request("POST", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId) + "/members", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")));
    }

    /**
     * DELETE /api/guilds/{guildId}/roles/{roleId}/members
     */
    public ApiResponse removeRoleMembers(String guildId, String roleId, List<String> tinyIds) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId) + "/members", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")));
    }

    /**
     * POST /api/direct-messages
     */
    public ApiResponse sendDirectMessage(String peerTinyId, String sourceGuildId, String text) {
        return request("POST", "/direct-messages", params("peer_tiny_id", required(peerTinyId, "peerTinyId"), "source_guild_id", required(sourceGuildId, "sourceGuildId"), "text", required(text, "text")));
    }

    public ApiResponse sendDirectMessage(int ref, String text) {
        return request("POST", "/direct-messages", params("ref", positive(ref), "text", required(text, "text")));
    }

    /**
     * GET /api/guilds/{guildId}/feeds
     */
    public ApiResponse getGuildFeeds(String guildId, Integer getType, Integer count, String feedAttachInfo) {
        return request("GET", "/guilds/" + segment(guildId) + "/feeds", params("get_type", getType, "count", count, "feed_attach_info", feedAttachInfo));
    }

    public ApiResponse getGuildFeeds(String guildId) {
        return getGuildFeeds(guildId, 2, null, null);
    }

    /**
     * GET /api/guilds/{guildId}/feeds/search
     */
    public ApiResponse searchFeeds(String guildId, String query, String cookie) {
        return request("GET", "/guilds/" + segment(guildId) + "/feeds/search", params("query", required(query, "query"), "cookie", cookie));
    }

    public ApiResponse searchFeeds(String guildId, String query) {
        return searchFeeds(guildId, query, null);
    }

    /**
     * GET /api/guilds/{guildId}/channels/{channelId}/feeds
     */
    public ApiResponse getChannelFeeds(String guildId, String channelId, Integer count, String feedAttchInfo) {
        return request("GET", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId) + "/feeds", params("count", count, "feed_attch_info", feedAttchInfo));
    }

    public ApiResponse getChannelFeeds(String guildId, String channelId) {
        return getChannelFeeds(guildId, channelId, null, null);
    }

    /**
     * POST /api/guilds/{guildId}/channels/{channelId}/feeds
     */
    public ApiResponse publishFeed(String guildId, String channelId, FeedContent content) {
        return request("POST", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId) + "/feeds", json(content));
    }

    public ApiResponse publishFeed(String guildId, String channelId, String content) {
        return publishFeed(guildId, channelId, new FeedContent(required(content, "content")));
    }

    /**
     * POST /api/feeds
     */
    public ApiResponse publishFeed(FeedContent content) {
        return request("POST", "/feeds", json(content));
    }

    /**
     * GET /api/feeds/{feedId}
     */
    public ApiResponse getFeedDetail(String feedId, String guildId, String channelId) {
        return request("GET", "/feeds/" + segment(feedId), params("guild_id", guildId, "channel_id", channelId));
    }

    public ApiResponse getFeedDetail(String feedId) {
        return getFeedDetail(feedId, null, null);
    }

    /**
     * PATCH /api/feeds/{feedId}
     */
    public ApiResponse editFeed(String feedId, String guildId, String channelId, long createTime, FeedContent content) {
        return request("PATCH", "/feeds/" + segment(feedId), merge(json(content), params("guild_id", required(guildId, "guildId"), "channel_id", required(channelId, "channelId"), "create_time", Long.toString(createTime))));
    }

    /**
     * DELETE /api/feeds/{feedId}
     */
    public ApiResponse deleteFeed(String feedId, String guildId, String channelId, long createTime) {
        return request("DELETE", "/feeds/" + segment(feedId), params("guild_id", required(guildId, "guildId"), "channel_id", required(channelId, "channelId"), "create_time", Long.toString(createTime)));
    }

    /**
     * GET /api/feeds/{feedId}/share-url
     */
    public ApiResponse getFeedShareUrl(String feedId, String guildId, String channelId) {
        return request("GET", "/feeds/" + segment(feedId) + "/share-url", params("guild_id", guildId, "channel_id", channelId));
    }

    public ApiResponse getFeedShareUrl(String feedId) {
        return getFeedShareUrl(feedId, null, null);
    }

    /**
     * POST /api/feeds/{feedId}/move
     */
    public ApiResponse moveFeed(String feedId, String guildId, String originalChannelId, String channelId) {
        return request("POST", "/feeds/" + segment(feedId) + "/move", params("guild_id", required(guildId, "guildId"), "original_channel_id", required(originalChannelId, "originalChannelId"), "channel_id", required(channelId, "channelId")));
    }

    /**
     * GET /api/feeds/{feedId}/comments
     */
    public ApiResponse getComments(String feedId, CommentsQuery query) {
        return request("GET", "/feeds/" + segment(feedId) + "/comments", json(query));
    }

    public ApiResponse getComments(String feedId) {
        return getComments(feedId, new CommentsQuery(null, null, null, null, null, null));
    }

    /**
     * POST /api/feeds/{feedId}/comments
     */
    public ApiResponse comment(String feedId, CommentRequest request) {
        return request("POST", "/feeds/" + segment(feedId) + "/comments", json(request));
    }

    /**
     * DELETE /api/feeds/{feedId}/comments/{commentId}
     */
    public ApiResponse deleteComment(String feedId, String commentId, CommentDeletion request) {
        return request("DELETE", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId), json(request));
    }

    /**
     * GET /api/feeds/{feedId}/comments/{commentId}/replies
     */
    public ApiResponse getReplies(String feedId, String commentId, RepliesQuery query) {
        return request("GET", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies", json(query));
    }

    /**
     * POST /api/feeds/{feedId}/comments/{commentId}/replies
     */
    public ApiResponse reply(String feedId, String commentId, ReplyRequest request) {
        return request("POST", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies", json(request));
    }

    /**
     * DELETE /api/feeds/{feedId}/comments/{commentId}/replies/{replyId}
     */
    public ApiResponse deleteReply(String feedId, String commentId, String replyId, ReplyDeletion request) {
        return request("DELETE", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies/" + segment(replyId), json(request));
    }

    public ApiResponse comment(int ref, String content) {
        return invokeRaw("feed", "do-comment", params("ref", positive(ref), "content", required(content, "content")));
    }

    public ApiResponse reply(int ref, String content) {
        return invokeRaw("feed", "do-reply", params("ref", positive(ref), "content", required(content, "content")));
    }

    /**
     * PUT /api/feeds/{feedId}/like
     */
    public ApiResponse likeFeed(String feedId, String guildId, String channelId, boolean like) {
        return request("PUT", "/feeds/" + segment(feedId) + "/like", params("guild_id", guildId, "channel_id", channelId, "action", like ? 1 : 3));
    }

    public ApiResponse likeFeed(String feedId, boolean like) {
        return likeFeed(feedId, null, null, like);
    }

    /**
     * PUT /api/feeds/{feedId}/comments/{commentId}/like
     */
    public ApiResponse likeCommentOrReply(String feedId, String commentId, LikeRequest request) {
        return request("PUT", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/like", json(request));
    }

    /**
     * PUT /api/feeds/{feedId}/essence
     */
    public ApiResponse setFeedEssence(String feedId, boolean enabled) {
        return request("PUT", "/feeds/" + segment(feedId) + "/essence", params("action", enabled ? 1 : 2));
    }

    /**
     * PUT /api/feeds/{feedId}/pin
     */
    public ApiResponse pinFeed(String feedId, PinRequest request, boolean pinned) {
        return request("PUT", "/feeds/" + segment(feedId) + "/pin", merge(json(request), params("action", pinned ? 1 : 2)));
    }

    /**
     * POST /api/feeds/{feedId}/essence-notification
     */
    public ApiResponse pushEssenceNotification(String feedId) {
        return request("POST", "/feeds/" + segment(feedId) + "/essence-notification", empty());
    }

    /**
     * GET /api/notices/interactions
     */
    public ApiResponse getInteractionNotices(Integer pageNum, String guildId, String attachInfo) {
        return request("GET", "/notices/interactions", params("page_num", pageNum, "guild_id", guildId, "attach_info", attachInfo));
    }

    public ApiResponse getInteractionNotices() {
        return getInteractionNotices(null, null, null);
    }

    /**
     * GET /api/notices/status
     */
    public ApiResponse getNoticeStatus() {
        return request("GET", "/notices/status", empty());
    }

    /**
     * GET /api/notices/recent
     */
    public ApiResponse getRecentNotices() {
        return request("GET", "/notices/recent", empty());
    }

    /**
     * POST /api/notices/check
     */
    public ApiResponse checkNotices() {
        return request("POST", "/notices/check", empty());
    }

    /**
     * POST /api/notices/decisions
     */
    public ApiResponse dealNotice(String noticeId, boolean agree) {
        return request("POST", "/notices/decisions", params("notice_id", required(noticeId, "noticeId"), "action_id", agree ? "agree" : "refuse"));
    }

    public ApiResponse dealNotice(int ref, boolean agree) {
        return request("POST", "/notices/decisions", params("ref", positive(ref), "action_id", agree ? "agree" : "refuse"));
    }

    /**
     * GET /api/operations
     */
    public ApiResponse getOperations() {
        return request("GET", "/operations", empty());
    }

    /**
     * GET /api/cli/version
     */
    public ApiResponse getCliVersion() {
        return request("GET", "/cli/version", empty());
    }

    /**
     * Pass a typed request record for an advanced atomic operation.
     */
    public ApiResponse invoke(String domain, String action, Record parameters) {
        return invokeRaw(domain, action, json(parameters));
    }

    /**
     * Escape hatch for new/undocumented CLI fields; normal operations use typed methods.
     */
    public ApiResponse invokeRaw(String domain, String action, JsonObject parameters) {
        return request("POST", "/commands/" + segment(domain) + "/" + segment(action), Objects.requireNonNull(parameters, "parameters").deepCopy());
    }

    public ApiResponse getSchema(String domain, String action) {
        return request("GET", "/commands/" + segment(domain) + "/" + segment(action) + "/schema", empty());
    }

    private ApiResponse request(String method, String path, JsonObject input) {
        for (String key : input.keySet()) {
            if (!key.matches("[a-z][a-z0-9_]*") || key.equals("confirm")) {
                throw new IllegalArgumentException("Business parameters must use snake_case; use withOptions for confirm/dryRun");
            }
        }
        StringJoiner query = new StringJoiner("&");
        if (options.dryRun()) query.add("dryRun=true");
        if (options.confirm()) query.add("confirm=true");
        if (method.equals("GET")) {
            input.entrySet().forEach(entry -> {
                JsonElement value = entry.getValue();
                if (!value.isJsonNull()) query.add(encode(entry.getKey()) + "="
                        + encode(value.isJsonPrimitive() ? value.getAsString() : value.toString()));
            });
        }
        URI uri = URI.create(baseUri.toASCIIString() + path + (query.length() == 0 ? "" : "?" + query));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(timeout).header("Accept", "application/json");
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);
        if (method.equals("GET")) {
            builder.GET();
        } else {
            builder.header("Content-Type", "application/json; charset=utf-8")
                    .method(method, HttpRequest.BodyPublishers.ofString(JSON.toJson(input), StandardCharsets.UTF_8));
        }
        HttpResponse<String> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QcbApiException("QChannelBridge request interrupted; verify writes before retrying", 0, null, null, e);
        } catch (IOException e) {
            throw new QcbApiException("Cannot complete QChannelBridge request; verify writes before retrying", 0, null, null, e);
        }
        JsonElement decoded;
        try {
            decoded = JSON.fromJson(response.body(), JsonElement.class);
        } catch (JsonParseException e) {
            throw new QcbApiException("QChannelBridge returned invalid JSON (HTTP " + response.statusCode() + ")",
                    response.statusCode(), null, null, e);
        }
        if (decoded == null || !decoded.isJsonObject()) {
            throw new QcbApiException("Expected a QChannelBridge response object", response.statusCode(), null, decoded, null);
        }
        JsonObject body = decoded.getAsJsonObject();
        JsonElement success = body.get("success");
        if (success == null || !success.isJsonPrimitive() || !success.getAsJsonPrimitive().isBoolean()) {
            throw new QcbApiException("Missing boolean success in QChannelBridge response", response.statusCode(), null, decoded, null);
        }
        String message = body.has("message") && body.get("message").isJsonPrimitive()
                ? body.get("message").getAsString() : "QChannelBridge request failed";
        JsonElement data = body.get("data");
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !success.getAsBoolean()) {
            Integer code = null;
            if (data != null && data.isJsonObject()) {
                JsonElement value = data.getAsJsonObject().get("code");
                if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                    try {
                        code = value.getAsInt();
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            throw new QcbApiException(message, response.statusCode(), code, decoded, null);
        }
        return new ApiResponse(true, message, data == null ? JsonNull.INSTANCE : data);
    }
}

