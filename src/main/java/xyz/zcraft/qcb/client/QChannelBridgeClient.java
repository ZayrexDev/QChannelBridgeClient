package xyz.zcraft.qcb.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import xyz.zcraft.qcb.client.response.*;
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
    public ApiResponse<GuildList> getGuilds() {
        return request("GET", "/guilds", empty(), GuildList.class);
    }

    /**
     * POST /api/guilds
     */
    public ApiResponse<OperationResult> createGuild(CreateGuildRequest request) {
        return request("POST", "/guilds", json(request), OperationResult.class);
    }

    /**
     * GET /api/guilds/search
     */
    public ApiResponse<GuildSearchResult> searchGuilds(String keyword, String scope, String nextPageToken) {
        return request("GET", "/guilds/search", params("keyword", required(keyword, "keyword"), "scope", scope, "next_page_token", nextPageToken), GuildSearchResult.class);
    }

    public ApiResponse<GuildSearchResult> searchGuilds(String keyword) {
        return searchGuilds(keyword, null, null);
    }

    /**
     * GET /api/shares/resolve
     */
    public ApiResponse<ShareInfo> resolveShare(String url) {
        return request("GET", "/shares/resolve", params("url", required(url, "url")), ShareInfo.class);
    }

    /**
     * GET /api/users/{tinyId}
     */
    public ApiResponse<UserInfo> getUserInfo(String tinyId) {
        return request("GET", "/users/" + segment(tinyId), empty(), UserInfo.class);
    }

    /**
     * GET /api/guilds/{guildId}
     */
    public ApiResponse<GuildInfo> getGuildInfo(String guildId) {
        return request("GET", "/guilds/" + segment(guildId), empty(), GuildInfo.class);
    }

    /**
     * PATCH /api/guilds/{guildId}
     */
    public ApiResponse<OperationResult> updateGuildInfo(String guildId, String guildName, String guildProfile) {
        return request("PATCH", "/guilds/" + segment(guildId), params("guild_name", guildName, "guild_profile", guildProfile), OperationResult.class);
    }

    /**
     * PUT /api/guilds/{guildId}/avatar
     */
    public ApiResponse<OperationResult> updateGuildAvatar(String guildId, String imagePath) {
        return request("PUT", "/guilds/" + segment(guildId) + "/avatar", params("image_path", required(imagePath, "imagePath")), OperationResult.class);
    }

    /**
     * PUT /api/guilds/{guildId}/number
     */
    public ApiResponse<OperationResult> updateGuildNumber(String guildId, String guildNumber) {
        return request("PUT", "/guilds/" + segment(guildId) + "/number", params("guild_number", required(guildNumber, "guildNumber")), OperationResult.class);
    }

    /**
     * GET /api/guilds/{guildId}/share-url
     */
    public ApiResponse<ShareLink> getGuildShareUrl(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/share-url", empty(), ShareLink.class);
    }

    /**
     * GET /api/guilds/{guildId}/join-settings
     */
    public ApiResponse<GuildJoinSettings> getJoinSettings(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/join-settings", empty(), GuildJoinSettings.class);
    }

    /**
     * PUT /api/guilds/{guildId}/join-settings
     */
    public ApiResponse<OperationResult> updateJoinSettings(String guildId, JoinSettings request) {
        return request("PUT", "/guilds/" + segment(guildId) + "/join-settings", json(request), OperationResult.class);
    }

    /**
     * POST /api/guilds/{guildId}/membership
     */
    public ApiResponse<OperationResult> joinGuild(String guildId, JoinGuildRequest request) {
        return request("POST", "/guilds/" + segment(guildId) + "/membership", json(request), OperationResult.class);
    }

    public ApiResponse<OperationResult> joinGuild(String guildId) {
        return joinGuild(guildId, new JoinGuildRequest(null, null));
    }

    /**
     * DELETE /api/guilds/{guildId}/membership
     */
    public ApiResponse<OperationResult> leaveGuild(String guildId) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/membership", empty(), OperationResult.class);
    }

    /**
     * GET /api/guilds/{guildId}/members
     */
    public ApiResponse<MemberPage> getMembers(String guildId, String nextPageToken) {
        return request("GET", "/guilds/" + segment(guildId) + "/members", params("next_page_token", nextPageToken), MemberPage.class);
    }

    public ApiResponse<MemberPage> getMembers(String guildId) {
        return getMembers(guildId, null);
    }

    /**
     * GET /api/guilds/{guildId}/members/search
     */
    public ApiResponse<MemberPage> searchMembers(String guildId, String keyword, Integer num, String nextPos) {
        return request("GET", "/guilds/" + segment(guildId) + "/members/search", params("keyword", required(keyword, "keyword"), "num", num, "next_pos", nextPos), MemberPage.class);
    }

    public ApiResponse<MemberPage> searchMembers(String guildId, String keyword) {
        return searchMembers(guildId, keyword, null, null);
    }

    /**
     * GET /api/guilds/{guildId}/members/{tinyId}
     */
    public ApiResponse<UserInfo> getMemberInfo(String guildId, String tinyId) {
        return request("GET", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId), empty(), UserInfo.class);
    }

    /**
     * PUT /api/guilds/{guildId}/members/{tinyId}/mute
     */
    public ApiResponse<OperationResult> muteMember(String guildId, String tinyId, long untilTimestamp) {
        return request("PUT", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId) + "/mute", params("time_stamp", Long.toString(untilTimestamp)), OperationResult.class);
    }

    /**
     * DELETE /api/guilds/{guildId}/members/{tinyId}
     */
    public ApiResponse<OperationResult> kickMember(String guildId, String tinyId, boolean blacklist, String revokeMsgs) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/members/" + segment(tinyId), params("blacklist", blacklist, "revoke_msgs", revokeMsgs), OperationResult.class);
    }

    public ApiResponse<OperationResult> kickMember(String guildId, String tinyId) {
        return kickMember(guildId, tinyId, false, null);
    }

    /**
     * POST /api/guilds/{guildId}/admins
     */
    public ApiResponse<OperationResult> addAdmins(String guildId, List<String> tinyIds) {
        return request("POST", "/guilds/" + segment(guildId) + "/admins", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")), OperationResult.class);
    }

    /**
     * DELETE /api/guilds/{guildId}/admins
     */
    public ApiResponse<OperationResult> removeAdmins(String guildId, List<String> tinyIds) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/admins", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")), OperationResult.class);
    }

    /**
     * GET /api/guilds/{guildId}/channels
     */
    public ApiResponse<ChannelList> getChannels(String guildId) {
        return request("GET", "/guilds/" + segment(guildId) + "/channels", empty(), ChannelList.class);
    }

    /**
     * POST /api/guilds/{guildId}/channels
     */
    public ApiResponse<OperationResult> createChannel(String guildId, String channelName) {
        return request("POST", "/guilds/" + segment(guildId) + "/channels", params("channel_name", required(channelName, "channelName")), OperationResult.class);
    }

    /**
     * PATCH /api/guilds/{guildId}/channels/{channelId}
     */
    public ApiResponse<OperationResult> updateChannel(String guildId, String channelId, String channelName) {
        return request("PATCH", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId), params("channel_name", required(channelName, "channelName")), OperationResult.class);
    }

    /**
     * DELETE /api/guilds/{guildId}/channels/{channelId}
     */
    public ApiResponse<OperationResult> deleteChannel(String guildId, String channelId) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId), empty(), OperationResult.class);
    }

    /**
     * POST /api/guilds/{guildId}/roles
     */
    public ApiResponse<OperationResult> createRole(String guildId, String name) {
        return request("POST", "/guilds/" + segment(guildId) + "/roles", params("name", required(name, "name")), OperationResult.class);
    }

    /**
     * PATCH /api/guilds/{guildId}/roles/{roleId}
     */
    public ApiResponse<OperationResult> updateRole(String guildId, String roleId, String name) {
        return request("PATCH", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId), params("name", required(name, "name")), OperationResult.class);
    }

    /**
     * POST /api/guilds/{guildId}/roles/{roleId}/members
     */
    public ApiResponse<OperationResult> addRoleMembers(String guildId, String roleId, List<String> tinyIds) {
        return request("POST", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId) + "/members", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")), OperationResult.class);
    }

    /**
     * DELETE /api/guilds/{guildId}/roles/{roleId}/members
     */
    public ApiResponse<OperationResult> removeRoleMembers(String guildId, String roleId, List<String> tinyIds) {
        return request("DELETE", "/guilds/" + segment(guildId) + "/roles/" + segment(roleId) + "/members", params("tiny_ids", nonEmpty(tinyIds, "tinyIds")), OperationResult.class);
    }

    /**
     * POST /api/direct-messages
     */
    public ApiResponse<OperationResult> sendDirectMessage(String peerTinyId, String sourceGuildId, String text) {
        return request("POST", "/direct-messages", params("peer_tiny_id", required(peerTinyId, "peerTinyId"), "source_guild_id", required(sourceGuildId, "sourceGuildId"), "text", required(text, "text")), OperationResult.class);
    }

    public ApiResponse<OperationResult> sendDirectMessage(int ref, String text) {
        return request("POST", "/direct-messages", params("ref", positive(ref), "text", required(text, "text")), OperationResult.class);
    }

    /**
     * GET /api/guilds/{guildId}/feeds
     */
    public ApiResponse<FeedPage> getGuildFeeds(String guildId, Integer getType, Integer count, String feedAttachInfo) {
        return request("GET", "/guilds/" + segment(guildId) + "/feeds", params("get_type", getType, "count", count, "feed_attach_info", feedAttachInfo), FeedPage.class);
    }

    public ApiResponse<FeedPage> getGuildFeeds(String guildId) {
        return getGuildFeeds(guildId, 2, null, null);
    }

    /**
     * GET /api/guilds/{guildId}/feeds/search
     */
    public ApiResponse<FeedPage> searchFeeds(String guildId, String query, String cookie) {
        return request("GET", "/guilds/" + segment(guildId) + "/feeds/search", params("query", required(query, "query"), "cookie", cookie), FeedPage.class);
    }

    public ApiResponse<FeedPage> searchFeeds(String guildId, String query) {
        return searchFeeds(guildId, query, null);
    }

    /**
     * GET /api/guilds/{guildId}/channels/{channelId}/feeds
     */
    public ApiResponse<FeedPage> getChannelFeeds(String guildId, String channelId, Integer count, String feedAttchInfo) {
        return request("GET", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId) + "/feeds", params("count", count, "feed_attch_info", feedAttchInfo), FeedPage.class);
    }

    public ApiResponse<FeedPage> getChannelFeeds(String guildId, String channelId) {
        return getChannelFeeds(guildId, channelId, null, null);
    }

    /**
     * POST /api/guilds/{guildId}/channels/{channelId}/feeds
     */
    public ApiResponse<OperationResult> publishFeed(String guildId, String channelId, FeedContent content) {
        return request("POST", "/guilds/" + segment(guildId) + "/channels/" + segment(channelId) + "/feeds", json(content), OperationResult.class);
    }

    public ApiResponse<OperationResult> publishFeed(String guildId, String channelId, String content) {
        return publishFeed(guildId, channelId, new FeedContent(required(content, "content")));
    }

    /**
     * POST /api/feeds
     */
    public ApiResponse<OperationResult> publishFeed(FeedContent content) {
        return request("POST", "/feeds", json(content), OperationResult.class);
    }

    /**
     * GET /api/feeds/{feedId}
     */
    public ApiResponse<Feed> getFeedDetail(String feedId, String guildId, String channelId) {
        return request("GET", "/feeds/" + segment(feedId), params("guild_id", guildId, "channel_id", channelId), Feed.class);
    }

    public ApiResponse<Feed> getFeedDetail(String feedId) {
        return getFeedDetail(feedId, null, null);
    }

    /**
     * PATCH /api/feeds/{feedId}
     */
    public ApiResponse<OperationResult> editFeed(String feedId, String guildId, String channelId, long createTime, FeedContent content) {
        return request("PATCH", "/feeds/" + segment(feedId), merge(json(content), params("guild_id", required(guildId, "guildId"), "channel_id", required(channelId, "channelId"), "create_time", Long.toString(createTime))), OperationResult.class);
    }

    /**
     * DELETE /api/feeds/{feedId}
     */
    public ApiResponse<OperationResult> deleteFeed(String feedId, String guildId, String channelId, long createTime) {
        return request("DELETE", "/feeds/" + segment(feedId), params("guild_id", required(guildId, "guildId"), "channel_id", required(channelId, "channelId"), "create_time", Long.toString(createTime)), OperationResult.class);
    }

    /**
     * GET /api/feeds/{feedId}/share-url
     */
    public ApiResponse<ShareLink> getFeedShareUrl(String feedId, String guildId, String channelId) {
        return request("GET", "/feeds/" + segment(feedId) + "/share-url", params("guild_id", guildId, "channel_id", channelId), ShareLink.class);
    }

    public ApiResponse<ShareLink> getFeedShareUrl(String feedId) {
        return getFeedShareUrl(feedId, null, null);
    }

    /**
     * POST /api/feeds/{feedId}/move
     */
    public ApiResponse<OperationResult> moveFeed(String feedId, String guildId, String originalChannelId, String channelId) {
        return request("POST", "/feeds/" + segment(feedId) + "/move", params("guild_id", required(guildId, "guildId"), "original_channel_id", required(originalChannelId, "originalChannelId"), "channel_id", required(channelId, "channelId")), OperationResult.class);
    }

    /**
     * GET /api/feeds/{feedId}/comments
     */
    public ApiResponse<CommentPage> getComments(String feedId, CommentsQuery query) {
        return request("GET", "/feeds/" + segment(feedId) + "/comments", json(query), CommentPage.class);
    }

    public ApiResponse<CommentPage> getComments(String feedId) {
        return getComments(feedId, new CommentsQuery(null, null, null, null, null, null));
    }

    /**
     * POST /api/feeds/{feedId}/comments
     */
    public ApiResponse<OperationResult> comment(String feedId, CommentRequest request) {
        return request("POST", "/feeds/" + segment(feedId) + "/comments", json(request), OperationResult.class);
    }

    /**
     * DELETE /api/feeds/{feedId}/comments/{commentId}
     */
    public ApiResponse<OperationResult> deleteComment(String feedId, String commentId, CommentDeletion request) {
        return request("DELETE", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId), json(request), OperationResult.class);
    }

    /**
     * GET /api/feeds/{feedId}/comments/{commentId}/replies
     */
    public ApiResponse<ReplyPage> getReplies(String feedId, String commentId, RepliesQuery query) {
        return request("GET", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies", json(query), ReplyPage.class);
    }

    /**
     * POST /api/feeds/{feedId}/comments/{commentId}/replies
     */
    public ApiResponse<OperationResult> reply(String feedId, String commentId, ReplyRequest request) {
        return request("POST", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies", json(request), OperationResult.class);
    }

    /**
     * DELETE /api/feeds/{feedId}/comments/{commentId}/replies/{replyId}
     */
    public ApiResponse<OperationResult> deleteReply(String feedId, String commentId, String replyId, ReplyDeletion request) {
        return request("DELETE", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/replies/" + segment(replyId), json(request), OperationResult.class);
    }

    public ApiResponse<OperationResult> comment(int ref, String content) {
        return request("POST", "/commands/feed/do-comment", params("ref", positive(ref), "content", required(content, "content")), OperationResult.class);
    }

    public ApiResponse<OperationResult> reply(int ref, String content) {
        return request("POST", "/commands/feed/do-reply", params("ref", positive(ref), "content", required(content, "content")), OperationResult.class);
    }

    /**
     * PUT /api/feeds/{feedId}/like
     */
    public ApiResponse<OperationResult> likeFeed(String feedId, String guildId, String channelId, boolean like) {
        return request("PUT", "/feeds/" + segment(feedId) + "/like", params("guild_id", guildId, "channel_id", channelId, "action", like ? 1 : 3), OperationResult.class);
    }

    public ApiResponse<OperationResult> likeFeed(String feedId, boolean like) {
        return likeFeed(feedId, null, null, like);
    }

    /**
     * PUT /api/feeds/{feedId}/comments/{commentId}/like
     */
    public ApiResponse<OperationResult> likeCommentOrReply(String feedId, String commentId, LikeRequest request) {
        return request("PUT", "/feeds/" + segment(feedId) + "/comments/" + segment(commentId) + "/like", json(request), OperationResult.class);
    }

    /**
     * PUT /api/feeds/{feedId}/essence
     */
    public ApiResponse<OperationResult> setFeedEssence(String feedId, boolean enabled) {
        return request("PUT", "/feeds/" + segment(feedId) + "/essence", params("action", enabled ? 1 : 2), OperationResult.class);
    }

    /**
     * PUT /api/feeds/{feedId}/pin
     */
    public ApiResponse<OperationResult> pinFeed(String feedId, PinRequest request, boolean pinned) {
        return request("PUT", "/feeds/" + segment(feedId) + "/pin", merge(json(request), params("action", pinned ? 1 : 2)), OperationResult.class);
    }

    /**
     * POST /api/feeds/{feedId}/essence-notification
     */
    public ApiResponse<OperationResult> pushEssenceNotification(String feedId) {
        return request("POST", "/feeds/" + segment(feedId) + "/essence-notification", empty(), OperationResult.class);
    }

    /**
     * GET /api/notices/interactions
     */
    public ApiResponse<NoticePage> getInteractionNotices(Integer pageNum, String guildId, String attachInfo) {
        return request("GET", "/notices/interactions", params("page_num", pageNum, "guild_id", guildId, "attach_info", attachInfo), NoticePage.class);
    }

    public ApiResponse<NoticePage> getInteractionNotices() {
        return getInteractionNotices(null, null, null);
    }

    /**
     * GET /api/notices/status
     */
    public ApiResponse<NoticeStatus> getNoticeStatus() {
        return request("GET", "/notices/status", empty(), NoticeStatus.class);
    }

    /**
     * GET /api/notices/recent
     */
    public ApiResponse<NoticePage> getRecentNotices() {
        return request("GET", "/notices/recent", empty(), NoticePage.class);
    }

    /**
     * POST /api/notices/check
     */
    public ApiResponse<NoticePage> checkNotices() {
        return request("POST", "/notices/check", empty(), NoticePage.class);
    }

    /**
     * POST /api/notices/decisions
     */
    public ApiResponse<OperationResult> dealNotice(String noticeId, boolean agree) {
        return request("POST", "/notices/decisions", params("notice_id", required(noticeId, "noticeId"), "action_id", agree ? "agree" : "refuse"), OperationResult.class);
    }

    public ApiResponse<OperationResult> dealNotice(int ref, boolean agree) {
        return request("POST", "/notices/decisions", params("ref", positive(ref), "action_id", agree ? "agree" : "refuse"), OperationResult.class);
    }

    /**
     * GET /api/operations
     */
    public ApiResponse<List<OperationDescription>> getOperations() {
        return request("GET", "/operations", empty(), new TypeToken<List<OperationDescription>>() {}.getType());
    }

    /**
     * GET /api/cli/version
     */
    public ApiResponse<CliVersion> getCliVersion() {
        return request("GET", "/cli/version", empty(), CliVersion.class);
    }

    /**
     * Pass a typed request record for an advanced atomic operation.
     */
    public <T> ApiResponse<T> invoke(String domain, String action, Record parameters, Class<T> resultType) {
        return request("POST", "/commands/" + segment(domain) + "/" + segment(action), json(parameters),
                Objects.requireNonNull(resultType, "resultType"));
    }

    /**
     * Escape hatch for new/undocumented CLI fields; normal operations use typed methods.
     */
    public ApiResponse<JsonElement> invokeRaw(String domain, String action, JsonObject parameters) {
        return request("POST", "/commands/" + segment(domain) + "/" + segment(action), Objects.requireNonNull(parameters, "parameters").deepCopy(), JsonElement.class);
    }

    public ApiResponse<CommandSchema> getSchema(String domain, String action) {
        return request("GET", "/commands/" + segment(domain) + "/" + segment(action) + "/schema", empty(), CommandSchema.class);
    }

    private <T> ApiResponse<T> request(String method, String path, JsonObject input, Type resultType) {
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
        JsonElement payload = data;
        if (data != null && data.isJsonObject()) {
            JsonObject cli = data.getAsJsonObject();
            if (cli.has("success") && cli.get("success").isJsonPrimitive()
                    && cli.get("success").getAsJsonPrimitive().isBoolean()) {
                payload = cli.has("data") ? cli.get("data") : JsonNull.INSTANCE;
            }
        }
        try {
            Preview preview = null;
            if (payload != null && payload.isJsonObject()
                    && new JsonPrimitive(true).equals(payload.getAsJsonObject().get("dry_run"))) {
                preview = ResponseParser.read(payload, Preview.class);
            }
            T result = preview == null ? ResponseParser.read(payload, resultType) : null;
            return new ApiResponse<>(true, message, result, preview,
                    ResponseParser.hint(data, payload, "setup_hint"),
                    ResponseParser.hint(data, payload, "subscribe_hint"), decoded);
        } catch (JsonParseException | IllegalStateException e) {
            throw new QcbApiException("Cannot decode QChannelBridge result as " + resultType.getTypeName(),
                    response.statusCode(), null, decoded, e);
        }
    }
}

