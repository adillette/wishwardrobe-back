package today.wishwordrobe.webpush;

public class WebPushSendException extends RuntimeException {

    private final String endpoint;
    private final int statusCode;
    private final String responseBody;

    public WebPushSendException(String endpoint, int statusCode, String responseBody) {
        super("WebPush failed: status=" + statusCode + ", endpoint=" + endpoint);
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
