package sd.p03.day26;

/** What travels down the pipeline. */
public record Request(String apiKey, String clientIp, String path) {
}
