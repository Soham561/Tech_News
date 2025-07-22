public class Main {
    public static void main(String[] args) {
        // Fetch secrets from environment variables
        String apiKey = System.getenv("API_KEY");
        String appPassword = System.getenv("APP_PASSWD");
        String senderEmail = System.getenv("SENDER_EMAIL");
        String receiverEmail = System.getenv("RECEIVER_EMAIL");

        API_GET.performAction(apiKey,senderEmail,receiverEmail,appPassword);
    }
}
