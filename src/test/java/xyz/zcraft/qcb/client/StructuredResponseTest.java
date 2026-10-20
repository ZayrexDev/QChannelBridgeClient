package xyz.zcraft.qcb.client;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import xyz.zcraft.qcb.client.response.*;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class StructuredResponseTest {
    private HttpServer server;
    private QChannelBridgeClient client;
    private JsonObject response;
    private int status;

    @BeforeEach void start() throws Exception {
        status = 200;
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        client = new QChannelBridgeClient("http://localhost:" + server.getAddress().getPort(), "fixture-token");
    }

    @AfterEach void stop() { server.stop(0); }

    private JsonObject fixture(String name) throws Exception {
        try (var in = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/responses/" + name + ".json")), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(in).getAsJsonObject();
        }
    }

    private void reply(JsonElement cli) {
        response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "OK");
        response.add("data", cli);
    }

    private void payload(String json) { reply(JsonParser.parseString("{\"success\":true,\"data\":" + json + "}")); }

    @Test void realGuildAndShareResponsesHaveTypedFieldsAndLosslessIds() throws Exception {
        reply(fixture("guilds"));
        ApiResponse<GuildList> guilds = client.getGuilds();
        assertEquals("18446744073709551615", guilds.data().managedGuilds().getFirst().guildId());
        assertTrue(guilds.data().createdGuilds().isEmpty());
        assertEquals("fixture", guilds.result().joinedGuilds().getFirst().name());
        reply(fixture("guild-info"));
        GuildInfo guild = client.getGuildInfo("123").data();
        assertEquals("https://example.com/fixture", guild.avatarUrl());
        assertEquals("fixture", guild.profile());
        reply(fixture("channels"));
        assertEquals("fixture", client.getChannels("123").data().channels().getFirst().channelName());
        reply(fixture("guild-search"));
        assertEquals("fixture", client.searchGuilds("test").data().channels().getFirst().profile());
        reply(fixture("guild-share"));
        assertEquals("https://example.com/fixture", client.getGuildShareUrl("123").data().shareUrl());
        reply(fixture("share-info"));
        assertEquals("fixture", client.resolveShare("https://pd.qq.com/s/example").data().guildName());
    }

    @Test void memberModelsHandleChineseKeysCamelCaseAndSearchPagination() throws Exception {
        reply(fixture("members"));
        MemberPage members = client.getMembers("123").data();
        assertEquals("fixture", members.members().getFirst().nickname());
        assertEquals("fixture", members.owners().getFirst().gender());
        assertEquals("18446744073709551615", members.admins().getFirst().tinyId());
        assertTrue(members.aiMembers().isEmpty());
        reply(fixture("member-search"));
        Member member = client.searchMembers("123", "test").data().members().getFirst();
        assertEquals("fixture", member.joinTime());
        assertEquals("2026-10-20 12:00:00", member.joinTimeHuman());
        reply(fixture("member-info"));
        assertEquals("fixture", client.getMemberInfo("123", "456").data().globalNickname());
        reply(fixture("user-info"));
        assertEquals("fixture", client.getUserInfo("456").data().memberName());
    }

    @Test void feedListAndDetailSupportBothImageRepresentationsAndRichText() throws Exception {
        reply(fixture("feeds"));
        FeedPage feeds = client.getGuildFeeds("123").data();
        assertEquals("fixture", feeds.nextCursor());
        assertTrue(feeds.hasMore());
        assertEquals("fixture", feeds.feeds().getFirst().contentSnippet());
        assertEquals("fixture", feeds.feeds().getFirst().images().getFirst().url());
        assertThrows(UnsupportedOperationException.class, () -> feeds.feeds().clear());
        reply(fixture("feed-detail"));
        Feed detail = client.getFeedDetail("123").data();
        assertEquals("fixture", detail.content());
        assertEquals("fixture", detail.contentRichtext().text());
        assertEquals("https://example.com/fixture", detail.images().getFirst().url());
        assertEquals(1, detail.images().getFirst().width());
        assertEquals("18446744073709551615", detail.images().getFirst().picId());
        reply(fixture("feed-share"));
        assertEquals("https://example.com/fixture", client.getFeedShareUrl("123").data().shareUrl());
    }

    @Test void commentsAndNestedReplyPreviewsAreTyped() throws Exception {
        reply(fixture("comments"));
        CommentPage comments = client.getComments("123").data();
        assertTrue(comments.hasMore());
        assertEquals("fixture", comments.attachInfo());
        Comment withReply = comments.comments().stream().filter(c -> !c.repliesPreview().isEmpty()).findFirst().orElseThrow();
        assertEquals("fixture", withReply.content().text());
        Reply nested = withReply.repliesPreview().getFirst();
        assertEquals("18446744073709551615", nested.targetReplyId());
        assertEquals("fixture", nested.content().text());
        payload("{\"replies\":[{\"reply_id\":\"r\",\"content\":\"text\"}],\"has_more\":false,\"attach_info\":\"cursor\"}");
        ReplyPage page = client.getReplies("f", "c", new xyz.zcraft.qcb.client.request.RepliesQuery("123", "456", 20, null)).data();
        assertEquals("text", page.replies().getFirst().content().text());
        assertEquals("cursor", page.attachInfo());
    }

    @Test void noticesAndJoinPolicyHaveTypedStatusFields() throws Exception {
        reply(fixture("join-settings"));
        assertEquals("fixture", client.getJoinSettings("123").data().setting().joinType());
        reply(fixture("notice-status"));
        assertFalse(client.getNoticeStatus().data().active());
        payload("{\"notices\":[{\"ref\":7,\"feed_id\":\"f\",\"notice_action_list\":[{\"id\":\"agree\",\"text\":\"同意\"}]}]}");
        Notice notice = client.getInteractionNotices().data().notices().getFirst();
        assertEquals(7, notice.ref());
        assertEquals("agree", notice.noticeActionList().getFirst().id());
    }

    @Test void schemaAndOperationCatalogDoNotHaveAnExtraCliEnvelope() {
        reply(JsonParser.parseString("[{\"method\":\"DELETE\",\"path\":\"/api/feeds/{feed_id}\",\"command\":\"feed.del-feed\",\"fixedParameters\":{\"comment_type\":0},\"requiresConfirmation\":true}]"));
        ApiResponse<List<OperationDescription>> operations = client.getOperations();
        assertTrue(operations.data().getFirst().requiresConfirmation());
        assertEquals(0, operations.data().getFirst().fixedParameters().get("comment_type").getAsInt());
        reply(JsonParser.parseString("{\"command\":\"feed.publish-feed\",\"flags\":[{\"name\":\"feed-type\",\"type\":\"int\",\"default\":1,\"enum\":[\"1\",\"2\"]}]}"));
        CommandFlag flag = client.getSchema("feed", "publish-feed").data().flags().getFirst();
        assertEquals(1, flag.defaultValue().getAsInt());
        assertEquals(List.of("1", "2"), flag.enumValues());
        payload("{\"version\":\"1.0.10\"}");
        assertEquals("1.0.10", client.getCliVersion().data().version());
    }

    @Test void previewsAreSeparateFromBusinessResultsAndKeepExactParameters() {
        payload("{\"dry_run\":true,\"command\":\"tencent-channel-cli manage create-channel\",\"params\":{\"channel_name\":\"中文\"}}");
        ApiResponse<OperationResult> result = client.withOptions(RequestOptions.PREVIEW).createChannel("123", "中文");
        assertNull(result.data());
        assertEquals("中文", result.preview().params().get("channel_name").getAsString());
        assertTrue(result.raw().getAsJsonObject().getAsJsonObject("data").getAsJsonObject("data").get("dry_run").getAsBoolean());
    }

    @Test void missingNullAndArrayResultsAndFutureFieldsKeepTheirMeaning() {
        payload("[]");
        assertTrue(client.getGuildFeeds("123").data().feeds().isEmpty());
        payload("{\"has_more\":false,\"future_field\":{\"value\":123}}");
        ApiResponse<CommentPage> result = client.getComments("f");
        assertTrue(result.data().comments().isEmpty());
        assertEquals(123, result.raw().getAsJsonObject().getAsJsonObject("data").getAsJsonObject("data").getAsJsonObject("future_field").get("value").getAsInt());
        payload("null");
        assertNull(client.getGuildInfo("123").data());
        payload("{\"message\":\"done\",\"role_id\":\"18446744073709551615\"}");
        assertEquals("18446744073709551615", client.createRole("123", "test").data().roleId());
    }

    @Test void cliHintsAreStructuredWithoutDroppingRawMetadata() {
        reply(JsonParser.parseString("{\"success\":true,\"subscribe_hint\":{\"action\":\"subscribe_notices\",\"message\":\"hint\",\"command\":\"notices-on\"},\"data\":{\"version\":\"1.0.10\"}}"));
        ApiResponse<CliVersion> result = client.getCliVersion();
        assertEquals("hint", result.subscribeHint().message());
        assertEquals("1.0.10", result.data().version());
        assertNotNull(result.raw());
    }

    @Test void errorsHaveStructuredBridgeAndCliFieldsEvenAtHttp200() {
        for (int http : List.of(200, 400, 502)) {
            status = http;
            response = JsonParser.parseString("{\"success\":false,\"message\":\"failed\",\"data\":{\"code\":3002,\"httpStatus\":502,\"cli\":{\"success\":false,\"error\":{\"type\":\"validation\",\"message\":\"missing field\",\"field\":\"name\"}}}}").getAsJsonObject();
            QcbApiException error = assertThrows(QcbApiException.class, () -> client.getGuildInfo("123"));
            assertEquals(http, error.getHttpStatus());
            assertEquals(3002, error.getError().code());
            assertEquals(502, error.getError().httpStatus());
            assertEquals("missing field", error.getError().cli().error().message());
            assertEquals("name", error.getError().cli().error().field());
        }
    }

    @Test void invalidResultShapeReportsTheTypeAndPreservesTheResponse() {
        payload("\"not an object\"");
        QcbApiException error = assertThrows(QcbApiException.class, () -> client.getGuildInfo("123"));
        assertTrue(error.getMessage().contains("GuildInfo"));
        assertNotNull(error.getResponse());
    }

    @Test void genericInvokeRequiresAnExplicitResultTypeAndOnlyRawEscapeHatchReturnsJson() {
        record Query(String keyword) {}
        record CustomResult(String status) {}
        payload("{\"status\":\"done\"}");
        assertEquals("done", client.invoke("feed", "search-guild-feeds", new Query("test"), CustomResult.class).data().status());
        assertEquals("done", client.invokeRaw("feed", "search-guild-feeds", new JsonObject()).data().getAsJsonObject().get("status").getAsString());
        for (var method : QChannelBridgeClient.class.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers()) || method.getReturnType() != ApiResponse.class) continue;
            assertInstanceOf(ParameterizedType.class, method.getGenericReturnType(), method.toString());
            var resultType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            if (!method.getName().equals("invokeRaw")) assertNotEquals(JsonElement.class, resultType, method.toString());
        }
    }
}
