package xyz.zcraft.qcb.client;

import com.google.gson.*;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class StructuredBridgeContractTest {
    @Test void liveCatalogSchemasAndAllAtomicPreviewsHaveStructuredEnvelopes() throws Exception {
        String base = System.getProperty("qcb.test.baseUrl");
        assumeTrue(base != null, "Set qcb.test.baseUrl to enable the native CLI contract test");
        var client = new QChannelBridgeClient(base, System.getProperty("qcb.test.token"));
        var operations = client.getOperations().data();
        assertEquals(56, operations.size());
        assertFalse(client.getCliVersion().data().version().isBlank());
        assertFalse(client.getSchema("feed", "publish-feed").data().flags().isEmpty());
        JsonArray routes;
        try (var in = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/routes.json")), StandardCharsets.UTF_8)) {
            routes = JsonParser.parseReader(in).getAsJsonArray();
        }
        int previews = 0;
        for (var operation : operations) {
            JsonObject route = null;
            for (JsonElement item : routes) {
                JsonObject candidate = item.getAsJsonObject();
                if (candidate.get("method").getAsString().equals(operation.method())
                        && normalize("/api" + candidate.get("path").getAsString()).equals(normalize(operation.path()))) {
                    route = candidate;
                    break;
                }
            }
            assertNotNull(route, operation.path());
            if (operation.path().startsWith("/api/notices")) continue;
            JsonArray definitions = route.getAsJsonArray("parameters");
            Class<?>[] types = new Class<?>[definitions.size()];
            Object[] arguments = new Object[definitions.size()];
            for (int index = 0; index < definitions.size(); index++) {
                JsonObject definition = definitions.get(index).getAsJsonObject();
                String type = definition.get("type").getAsString();
                types[index] = parameterType(type);
                arguments[index] = argument(type, definition.get("name").getAsString());
            }
            var method = QChannelBridgeClient.class.getMethod(route.get("name").getAsString(), types);
            ApiResponse<?> response;
            try {
                response = (ApiResponse<?>) method.invoke(client.withOptions(RequestOptions.PREVIEW), arguments);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new AssertionError(operation.command(), e.getCause());
            }
            assertNull(response.data());
            assertNotNull(response.preview(), operation.command());
            assertEquals("tencent-channel-cli " + operation.command().replace('.', ' '), response.preview().command());
            assertNotNull(response.raw());
            previews++;
        }
        assertEquals(51, previews);
    }

    private static String normalize(String path) { return path.replaceAll("\\{[^}]+}", "{id}"); }

    private static Class<?> parameterType(String type) throws ClassNotFoundException {
        return switch (type) {
            case "String" -> String.class;
            case "Integer" -> Integer.class;
            case "long" -> long.class;
            case "boolean" -> boolean.class;
            case "List<String>" -> List.class;
            default -> Class.forName("xyz.zcraft.qcb.client.request." + type);
        };
    }

    private static Object argument(String type, String name) throws Exception {
        if (type.equals("String")) return switch (name) {
            case "url" -> "https://pd.qq.com/s/test";
            case "guildNumber" -> "pd1234567890";
            case "imagePath" -> "C:/test.png";
            case "scope" -> "channel";
            case "revokeMsgs" -> "3d";
            case "keyword", "query", "name", "channelName", "guildName", "guildProfile" -> "test";
            case "text" -> "结构化返回测试";
            default -> "123";
        };
        if (type.equals("Integer")) return name.equals("getType") ? 2 : 20;
        if (type.equals("long")) return 1700000000L;
        if (type.equals("boolean")) return true;
        if (type.equals("List<String>")) return List.of("123");
        JsonObject fixture = new JsonObject();
        for (String key : List.of("guild_id", "channel_id", "feed_author_id", "comment_author_id", "replier_id", "user_id")) fixture.addProperty(key, "123");
        for (String key : List.of("create_time", "feed_create_time", "comment_create_time")) fixture.addProperty(key, "1700000000");
        fixture.addProperty("content", "结构化返回预演");
        fixture.addProperty("image_path", "C:/test.png");
        fixture.addProperty("guild_name", "测试频道");
        fixture.addProperty("theme", "test");
        fixture.addProperty("join_type", "JOIN_GUILD_TYPE_DIRECT");
        fixture.addProperty("top_type", 1);
        fixture.addProperty("like_type", 3);
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
                .fromJson(fixture, parameterType(type));
    }
}
