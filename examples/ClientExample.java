import xyz.zcraft.qcb.client.*;

public class ClientExample {
    public static void main(String[] args) {
        String baseUrl = System.getenv().getOrDefault("QCB_BASE_URL", "http://localhost:8728");
        String token = System.getenv("QCB_API_TOKEN");
        QChannelBridgeClient client = new QChannelBridgeClient(baseUrl, token);

        System.out.println(client.getCliVersion().result());
        System.out.println(client.getOperations().result());
        System.out.println(client.withOptions(RequestOptions.PREVIEW)
                .publishFeed("123", "456", "客户端调用示例（预演）").result());
    }
}
