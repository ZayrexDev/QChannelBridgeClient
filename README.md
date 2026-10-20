# QChannelBridgeClient

QChannelBridge 的独立 Java 25+ 客户端。简单操作直接传具名形参，复杂操作使用 `xyz.zcraft.qcb.client.request` 包中的记录类；业务方法不接收 Map。

## 引入依赖

在此项目执行 `mvn install` 安装到本机 Maven 仓库，其它项目添加：

```xml
<dependency>
    <groupId>com.github.ZayrexDev</groupId>
    <artifactId>QChannelBridgeClient</artifactId>
    <version>v1.0.1</version>
</dependency>
```

Gradle 使用 `mavenLocal()` 和 `implementation 'com.github.ZayrexDev:QChannelBridgeClient:v1.0.1'`。
尚未发布公共仓库；跨机器使用请发布到自己的 Maven 仓库，或在目标机器执行安装。
客户端只依赖 JDK HttpClient 和 Gson，Maven/Gradle 会传递引入 Gson。CLI 和登录授权由服务端机器负责。

## 简单操作直接传参

```java
import xyz.zcraft.qcb.client.*;
import xyz.zcraft.qcb.client.request.*;
import xyz.zcraft.qcb.client.response.*;
import java.util.List;

var client = new QChannelBridgeClient("http://localhost:8728", "bridge-api-token");

client.getGuilds();
client.getGuildInfo("123");
client.getMembers("123", "next-page-token");
client.searchMembers("123", "昵称");
client.getGuildFeeds("123", 2, 20, null); // 最新帖子，最多 20 条

client.withOptions(RequestOptions.PREVIEW).createChannel("123", "讨论区");
client.withOptions(RequestOptions.PREVIEW).publishFeed("123", "456", "你好，频道！");
client.withOptions(RequestOptions.CONFIRMED).muteMember("123", "789", 0); // 0 表示解禁
client.withOptions(RequestOptions.PREVIEW).addAdmins("123", List.of("789", "790"));
client.sendDirectMessage("789", "123", "你好");
```

资源 ID 使用字符串，时间戳使用 Unix 秒。方法形参为 `long` 的时间戳会转换成 CLI 所需的字符串；记录类中的原始创建时间使用字符串。
可选参数传 `null` 时不发送，保留服务端/CLI 默认值。
方法不会自动确认高风险操作，实际执行仍须 `RequestOptions.CONFIRMED`。

## 复杂参数使用记录类

### 发帖与编辑

```java
var imagePost = new FeedContent("看看图片", List.of(new MediaFile("C:/images/photo.png")));
client.withOptions(RequestOptions.PREVIEW).publishFeed("123", "456", imagePost);

var markdown = FeedContent.markdown("文章标题", "# 标题\n正文");
client.withOptions(RequestOptions.PREVIEW).publishFeed("123", "456", markdown);

client.withOptions(RequestOptions.PREVIEW)
        .editFeed("B_example", "123", "456", 1700000000L, new FeedContent("新正文"));

client.withOptions(RequestOptions.CONFIRMED)
        .deleteFeed("B_example", "123", "456", 1700000000L);
```

`FeedContent` 的完整构造器还支持正文文件、图片/视频、本地媒体尺寸、已有图片 URL、@成员、链接、话题及编辑时清除媒体。
这些字段都是具名记录组件；无需自行拼接 JSON 字段。
图片/视频/正文文件路径指向服务端机器。视频使用 `videoPaths`，图片使用 `filePaths`。
`clearImages` / `clearVideos` 仅用于编辑；`content` 与 `markdownContent` 互斥。
`publishFeed(FeedContent)` 表示作者身份全局发帖，需要服务端账号具有对应身份并显式确认。

### 评论与回复

```java
client.withOptions(RequestOptions.PREVIEW).comment("B_example",
        new CommentRequest("123", "456", "1700000000", "评论正文"));

client.withOptions(RequestOptions.PREVIEW).reply("B_example", "C_example",
        new ReplyRequest("123", "456", "帖子作者tiny_id", "1700000000",
                "评论作者tiny_id", "1700000001", "回复人tiny_id", "回复正文"));

client.withOptions(RequestOptions.CONFIRMED).deleteComment("B_example", "C_example",
        new CommentDeletion("123", "456", "1700000000", "评论作者tiny_id"));

client.withOptions(RequestOptions.CONFIRMED).deleteReply("B_example", "C_example", "R_example",
        new ReplyDeletion("123", "456", "帖子作者tiny_id", "1700000000",
                "评论作者tiny_id", "1700000001", "回复人tiny_id"));

// 也支持直接引用通知编号。
client.comment(7, "对通知对应帖子发表评论");
client.reply(7, "回复通知对应评论");
client.sendDirectMessage(7, "回复私信通知");
client.dealNotice(7, true); // 同意，false 表示拒绝
```

`ReplyRequest` 完整构造器还支持图片、@成员、链接，以及楼中楼目标回复和目标用户。
删除评论/回复 REST 方法固定为自删；以帖主身份删除他人内容，可使用下面的原子命令入口并指定相应类型。

### 加入设置

```java
var settings = new JoinSettings("JOIN_GUILD_TYPE_MULTI_QUESTION",
        new JoinSetting(
                new QuestionSetting(List.of(new JoinQuestion("问题", "答案"))),
                null));
client.withOptions(RequestOptions.PREVIEW).updateJoinSettings("123", settings);
client.joinGuild("123");
client.joinGuild("123", new JoinGuildRequest("用户填写的附言或答案", null));
```

官方 CLI schema 没有描述测试题配置和多题答案的完整内部结构，因此 `JoinSetting.quiz` 保留为 `JsonObject`，`JoinGuildRequest.joinGuildAnswers` 保留为 `JsonArray`，按实际 CLI 返回结构传递。其余常见参数均有明确类型，不猜测未公开的字段。

## 分页、点赞和置顶

```java
client.getChannelFeeds("123", "456", 20, "上次返回的版块游标");
client.searchFeeds("123", "关键词", "上次返回的搜索cookie");
client.getComments("B_example", new CommentsQuery("123", "456", 20, 1, 3, null));
client.getReplies("B_example", "C_example", new RepliesQuery("123", "456", 20, null));

client.likeFeed("B_example", true);       // false 取消点赞
client.setFeedEssence("B_example", true); // false 取消精华
client.pinFeed("B_example", new PinRequest("123", null, "作者tiny_id", "1700000000", 1), true);
```

评论/回复点赞使用 `LikeRequest`，必须提供帖子作者、创建时间和评论作者等元数据。
`likeType=3/4` 为评论点赞/取消，`5/6` 为回复点赞/取消，回复场景还需 `replyId` 和 `replyAuthorId`。
频道首页游标、版块游标、搜索 cookie 会分别转换为 `feed_attach_info`、`feed_attch_info` 和 `cookie`，调用方无需处理拼写差异。

## 请求选项、响应和错误

`withOptions` 返回共享 HTTP 连接池的新客户端视图，不改变原客户端：
`DEFAULT` 无额外选项，`PREVIEW` 对应 `dryRun=true`，`CONFIRMED` 对应 `confirm=true`。
不要让长期保存的已确认视图承担无关操作；建议在具体调用处使用链式写法。

```java
ApiResponse<GuildInfo> response = client.getGuildInfo("123");
response.data().name();     // 业务记录类，可直接访问字段
response.result().guildId(); // result() 是 data() 的别名
response.raw();              // 完整桥接响应，保留 CLI 包装和所有未知字段

var page = client.getGuildFeeds("123").data();
page.feeds().forEach(feed -> System.out.println(feed.title()));
page.nextCursor();          // 对应当前接口的分页游标
page.hasMore();

var preview = client.withOptions(RequestOptions.PREVIEW).publishFeed("123", "456", "预演");
preview.preview().command();
preview.preview().params(); // 预演参数 JSON
// 预演没有真实业务结果，因此 preview.data() == null。

try {
    client.getGuildInfo("123");
} catch (QcbApiException e) {
    e.getHttpStatus(); // 网络失败/超时/中断为 0
    e.getErrorCode();  // 桥接错误码，可能为 null
    e.getError();      // ApiError，包含桥接错误码、HTTP 状态和 CliFailure
    e.getError().cli().error().message(); // 有 CLI error 时可直接访问；各可选层可能为 null
    e.getResponse();   // 完整 JSON 错误，含 data.cli；无 JSON 时为 null
    e.getCause();      // 原始网络异常
}
```

非 2xx 和 HTTP 200 但 success=false 都会抛出异常。客户端不做应用层重试，不跟随重定向。
写操作超时或中断后先核实实际结果，再决定是否重试；线程中断标记会保留。
默认连接超时 10 秒、请求超时 90 秒，第三个构造参数可设置请求超时 `Duration`。
baseUrl 支持根地址、`/api/` 或反向代理前缀；Token 是桥接 Token，可在服务未启用鉴权时传 null。

## 结构化返回类型

所有常规业务方法返回 `ApiResponse<T>`，T 按接口确定，不需要再手动处理 JSON：

| 接口 | 业务返回类型 T |
| --- | --- |
| `getGuilds` | `GuildList`，含 createdGuilds / managedGuilds / joinedGuilds |
| `getGuildInfo` | `GuildInfo` |
| `searchGuilds` | `GuildSearchResult` |
| `getMembers` / `searchMembers` | `MemberPage`，含 owners / admins / robots / aiMembers / members |
| `getUserInfo` / `getMemberInfo` | `UserInfo` |
| `getChannels` | `ChannelList`，成员为 `Channel` |
| `getGuildFeeds` / `getChannelFeeds` / `searchFeeds` | `FeedPage`，成员为 `Feed` |
| `getFeedDetail` | `Feed`，自动解开上游 feed 包装 |
| `getComments` | `CommentPage`，含 `Comment` 和 `Reply` 回复预览 |
| `getReplies` | `ReplyPage` |
| `getGuildShareUrl` / `getFeedShareUrl` | `ShareLink` |
| `resolveShare` | `ShareInfo` |
| `getJoinSettings` | `GuildJoinSettings` / `JoinPolicy` |
| 通知查询 / 检查 | `NoticePage` / `NoticeStatus` |
| 写入、加入、删除、互动操作 | `OperationResult`，包含状态、消息、相关 ID、分享链接和验证标志等可选字段 |
| `getCliVersion` | `CliVersion` |
| `getOperations` | `List<OperationDescription>` |
| `getSchema` | `CommandSchema`，含 `CommandFlag` 列表 |

结果记录类位于 `xyz.zcraft.qcb.client.response`，需要时导入 `import xyz.zcraft.qcb.client.response.*;`。
ID 始终为字符串，避免大整数精度丢失。缺失的数字/布尔字段使用包装类型，保留 null；列表字段缺失或为 null 时返回不可修改的空列表。
成员列表的中文键、“tinyid”、搜索的 camelCase 字段会统一映射到英文属性。
图片 URL 字符串和详情图片对象统一为 `MediaImage`；评论/回复正文统一为 `RichText`。

`ApiResponse.setupHint()` 和 `subscribeHint()` 返回具名 `Hint`。客户端不会自动执行这些提示中的命令。
未知/新增字段完整保留在 `raw()`；预演使用独立的 `Preview`。
官方没有完整响应 schema：测试题/验证问题、搜索作者的未确认结构、富文本贴纸、schema 默认值等保留为 JsonElement；这些字段不被擅自丢弃或猜测。
写入返回只建模公共已知字段，未实际执行写入来获取样本。具体上游操作未返回的 ID 等字段保持 null，不从请求参数伪造。
服务端协议没有改变；本次是客户端返回类型升级，旧代码中对 data()/result() 的 JsonElement 访问需要改为记录类属性。

## 参数查询和扩展

```java
client.getSchema("feed", "publish-feed");

// 为新操作定义一个小记录类即可使用原子命令入口。
record DeleteAsOwner(String feedId, String commentId, String commentAuthorId,
                     String feedCreateTime, String guildId, String channelId, int commentType) {}

client.withOptions(RequestOptions.CONFIRMED).invoke("feed", "do-comment",
        new DeleteAsOwner("B_example", "C_example", "作者tiny_id",
                "1700000000", "123", "456", 2), OperationResult.class);
```

自定义记录组件按 camelCase → snake_case 序列化，调用时必须通过第四个参数指定返回类型 Class<T>。
例如自定义 `record MyResult(String status) {}` 后，传 `MyResult.class`，返回 `ApiResponse<MyResult>`。
`invokeRaw` 返回 `ApiResponse<JsonElement>`，是唯一明确保留原始业务 JSON 的公开扩展入口。
`invokeRaw(domain, action, JsonObject)` 仅作为未公开/新增字段的低层扩展入口。
所有命令仍受服务端白名单及权限约束。

## 完整方法

以下为 56 条业务路由和目录/版本接口的主签名，常用操作另有简化重载。全部路径以 `/api` 为前缀。

| 方法签名 | REST 接口 |
| --- | --- |
| `getGuilds()` | `GET /guilds` |
| `createGuild(CreateGuildRequest request)` | `POST /guilds` |
| `searchGuilds(String keyword, String scope, String nextPageToken)` | `GET /guilds/search` |
| `resolveShare(String url)` | `GET /shares/resolve` |
| `getUserInfo(String tinyId)` | `GET /users/{tinyId}` |
| `getGuildInfo(String guildId)` | `GET /guilds/{guildId}` |
| `updateGuildInfo(String guildId, String guildName, String guildProfile)` | `PATCH /guilds/{guildId}` |
| `updateGuildAvatar(String guildId, String imagePath)` | `PUT /guilds/{guildId}/avatar` |
| `updateGuildNumber(String guildId, String guildNumber)` | `PUT /guilds/{guildId}/number` |
| `getGuildShareUrl(String guildId)` | `GET /guilds/{guildId}/share-url` |
| `getJoinSettings(String guildId)` | `GET /guilds/{guildId}/join-settings` |
| `updateJoinSettings(String guildId, JoinSettings request)` | `PUT /guilds/{guildId}/join-settings` |
| `joinGuild(String guildId, JoinGuildRequest request)` | `POST /guilds/{guildId}/membership` |
| `leaveGuild(String guildId)` | `DELETE /guilds/{guildId}/membership` |
| `getMembers(String guildId, String nextPageToken)` | `GET /guilds/{guildId}/members` |
| `searchMembers(String guildId, String keyword, Integer num, String nextPos)` | `GET /guilds/{guildId}/members/search` |
| `getMemberInfo(String guildId, String tinyId)` | `GET /guilds/{guildId}/members/{tinyId}` |
| `muteMember(String guildId, String tinyId, long untilTimestamp)` | `PUT /guilds/{guildId}/members/{tinyId}/mute` |
| `kickMember(String guildId, String tinyId, boolean blacklist, String revokeMsgs)` | `DELETE /guilds/{guildId}/members/{tinyId}` |
| `addAdmins(String guildId, List<String> tinyIds)` | `POST /guilds/{guildId}/admins` |
| `removeAdmins(String guildId, List<String> tinyIds)` | `DELETE /guilds/{guildId}/admins` |
| `getChannels(String guildId)` | `GET /guilds/{guildId}/channels` |
| `createChannel(String guildId, String channelName)` | `POST /guilds/{guildId}/channels` |
| `updateChannel(String guildId, String channelId, String channelName)` | `PATCH /guilds/{guildId}/channels/{channelId}` |
| `deleteChannel(String guildId, String channelId)` | `DELETE /guilds/{guildId}/channels/{channelId}` |
| `createRole(String guildId, String name)` | `POST /guilds/{guildId}/roles` |
| `updateRole(String guildId, String roleId, String name)` | `PATCH /guilds/{guildId}/roles/{roleId}` |
| `addRoleMembers(String guildId, String roleId, List<String> tinyIds)` | `POST /guilds/{guildId}/roles/{roleId}/members` |
| `removeRoleMembers(String guildId, String roleId, List<String> tinyIds)` | `DELETE /guilds/{guildId}/roles/{roleId}/members` |
| `sendDirectMessage(String peerTinyId, String sourceGuildId, String text)` | `POST /direct-messages` |
| `getGuildFeeds(String guildId, Integer getType, Integer count, String feedAttachInfo)` | `GET /guilds/{guildId}/feeds` |
| `searchFeeds(String guildId, String query, String cookie)` | `GET /guilds/{guildId}/feeds/search` |
| `getChannelFeeds(String guildId, String channelId, Integer count, String feedAttchInfo)` | `GET /guilds/{guildId}/channels/{channelId}/feeds` |
| `publishFeed(String guildId, String channelId, FeedContent content)` | `POST /guilds/{guildId}/channels/{channelId}/feeds` |
| `publishFeed(FeedContent content)` | `POST /feeds` |
| `getFeedDetail(String feedId, String guildId, String channelId)` | `GET /feeds/{feedId}` |
| `editFeed(String feedId, String guildId, String channelId, long createTime, FeedContent content)` | `PATCH /feeds/{feedId}` |
| `deleteFeed(String feedId, String guildId, String channelId, long createTime)` | `DELETE /feeds/{feedId}` |
| `getFeedShareUrl(String feedId, String guildId, String channelId)` | `GET /feeds/{feedId}/share-url` |
| `moveFeed(String feedId, String guildId, String originalChannelId, String channelId)` | `POST /feeds/{feedId}/move` |
| `getComments(String feedId, CommentsQuery query)` | `GET /feeds/{feedId}/comments` |
| `comment(String feedId, CommentRequest request)` | `POST /feeds/{feedId}/comments` |
| `deleteComment(String feedId, String commentId, CommentDeletion request)` | `DELETE /feeds/{feedId}/comments/{commentId}` |
| `getReplies(String feedId, String commentId, RepliesQuery query)` | `GET /feeds/{feedId}/comments/{commentId}/replies` |
| `reply(String feedId, String commentId, ReplyRequest request)` | `POST /feeds/{feedId}/comments/{commentId}/replies` |
| `deleteReply(String feedId, String commentId, String replyId, ReplyDeletion request)` | `DELETE /feeds/{feedId}/comments/{commentId}/replies/{replyId}` |
| `likeFeed(String feedId, String guildId, String channelId, boolean like)` | `PUT /feeds/{feedId}/like` |
| `likeCommentOrReply(String feedId, String commentId, LikeRequest request)` | `PUT /feeds/{feedId}/comments/{commentId}/like` |
| `setFeedEssence(String feedId, boolean enabled)` | `PUT /feeds/{feedId}/essence` |
| `pinFeed(String feedId, PinRequest request, boolean pinned)` | `PUT /feeds/{feedId}/pin` |
| `pushEssenceNotification(String feedId)` | `POST /feeds/{feedId}/essence-notification` |
| `getInteractionNotices(Integer pageNum, String guildId, String attachInfo)` | `GET /notices/interactions` |
| `getNoticeStatus()` | `GET /notices/status` |
| `getRecentNotices()` | `GET /notices/recent` |
| `checkNotices()` | `POST /notices/check` |
| `dealNotice(String noticeId, boolean agree)` | `POST /notices/decisions` |
| `getOperations()` | `GET /operations` |
| `getCliVersion()` | `GET /cli/version` |

## 构建和验证

```powershell
mvn test
mvn install

# 可选：对已启动、配置了真实 CLI 的服务执行预演契约测试
mvn test '-Dqcb.test.baseUrl=http://localhost:8728' '-Dqcb.test.token=your-token'
```

结构化返回测试使用 16 份脱敏后的真实 CLI 1.0.10 只读响应样本，并覆盖空结果、图片两种结构、中文键、分页、嵌套评论/回复、提示、错误、预演以及未知字段保留。
联调检查全部 56 条业务路由，并通过具名参数/记录类预演 51 条，跳过依赖账号本地状态的 5 条通知接口。
`examples/ClientExample.java` 是可编译运行的示例，通过 `QCB_BASE_URL` 和 `QCB_API_TOKEN` 配置，仅查询版本、目录并预演发帖。

本次参数接口替换了原先的 Map 签名，旧调用需要按以上形参/记录类迁移。
