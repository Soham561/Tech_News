import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class API_GET {

    public static void performAction(final String API_KEY, final String senderEmail,
                                     final String receiverEmail, final String appPasswd) {

        try {
            // Initial request to get totalResults
            final String baseUrl = "https://newsapi.org/v2/top-headlines?category=technology&language=en&pageSize=1&page=1&apiKey=" + API_KEY;

            URL url1 = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                int totalResults = json.getInt("totalResults");
                int safeRandomPage = getSafeRandomPage(totalResults);
                // Fetch article from random page
                final String finalUrl = "https://newsapi.org/v2/top-headlines?category=technology&language=en&pageSize=1&page="
                        + safeRandomPage + "&apiKey=" + API_KEY;

                URL url2 = new URL(finalUrl);
                HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                conn2.setRequestMethod("GET");
                conn2.setConnectTimeout(10000);
                conn2.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn2.getResponseCode() == 200) {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    StringBuilder sb2 = new StringBuilder();
                    String ln;

                    while ((ln = reader2.readLine()) != null) {
                        sb2.append(ln);
                    }
                    reader2.close();

                    JSONObject resultJson = new JSONObject(sb2.toString());
                    JSONArray articles = resultJson.getJSONArray("articles");

                    if (articles.length() > 0) {
                        JSONObject article = articles.getJSONObject(0);
                        String title = article.optString("title", "No title");
                        String description = article.optString("description", "No description available");
                        String urlLink = article.optString("url", "#");
                        String imageUrl = article.optString("urlToImage", "");
                        String publishedAt = formatDate(article.optString("publishedAt", "Date not available"));

                        // Email configuration
                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "587");
                        props.put("mail.smtp.starttls.enable", "true");

                        Session session = Session.getInstance(props,
                                new Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(senderEmail, appPasswd);
                                    }
                                });

                        // Create HTML email
                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(senderEmail));
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
                        message.setSubject("🚀 Tech News Digest: " + shortenTitle(title));
                        message.setContent(buildEmailHtml(title, description, urlLink, imageUrl, publishedAt), "text/html; charset=utf-8");

                        // Send email
                        Transport.send(message);
                    }
                } else {
                    System.out.println("❌ Error fetching article: Response code " + conn2.getResponseCode());
                }
            } else {
                System.out.println("❌ Failed to get total results. Response code: " + connection.getResponseCode());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Exception occurred:");
            e.printStackTrace();
        }
    }

    // Helper to get safe random page number
    public static int getSafeRandomPage(int totalResults) {
        int maxPage = Math.min(totalResults, 100); // Free plan limit
        if (maxPage < 1) return 1;
        return new Random().nextInt(maxPage) + 1;
    }

    // Format date string (YYYY-MM-DDTHH:MM:SSZ → YYYY-MM-DD HH:MM)
    private static String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty() || rawDate.equals("Date not available")) {
            return "Date not available";
        }
        try {
            return rawDate.substring(0, 10) + " " + rawDate.substring(11, 16);
        } catch (Exception e) {
            return rawDate;
        }
    }

    // Shorten long titles for email subject
    private static String shortenTitle(String title) {
        if (title.length() > 50) {
            return title.substring(0, 47) + "...";
        }
        return title;
    }

    // Build professional HTML email template
    private static String buildEmailHtml(String title, String description,
                                         String urlLink, String imageUrl, String publishedAt) {

        // Handle missing image
        String imageHtml = "";
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
            imageHtml = "<img src='" + imageUrl + "' alt='Article Image' style='max-width:100%; border-radius:8px;'>";
        } else {
            imageHtml = "<div style='background:#f0f5ff; height:200px; border-radius:8px; display:flex; align-items:center; justify-content:center;'>"
                    + "<p style='color:#4361ee; font-size:20px;'>Technology News Image</p></div>";
        }

        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "   <meta charset='UTF-8'>" +
                "   <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "   <title>Tech News Update</title>" +
                "   <style>" +
                "       * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "       body { font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; background-color: #f5f8fa; color: #333; line-height: 1.6; }" +
                "       .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 5px 15px rgba(0,0,0,0.08); }" +
                "       .header { background: linear-gradient(135deg, #4361ee, #3a0ca3); color: white; padding: 40px 20px; text-align: center; }" +
                "       .header h1 { font-size: 28px; font-weight: 700; margin-bottom: 10px; }" +
                "       .header p { opacity: 0.9; font-size: 16px; max-width: 80%; margin: 0 auto; }" +
                "       .content { padding: 30px; }" +
                "       .article-title { font-size: 24px; color: #2b2d42; margin-bottom: 15px; line-height: 1.4; }" +
                "       .date-badge { background: #edf2ff; color: #4361ee; padding: 6px 15px; border-radius: 20px; font-size: 14px; display: inline-block; margin-bottom: 20px; }" +
                "       .image-container { margin: 25px 0; border-radius: 8px; overflow: hidden; }" +
                "       .description { font-size: 17px; color: #4a5568; margin-bottom: 25px; line-height: 1.7; }" +
                "       .btn { display: inline-block; background: #4361ee; color: white !important; text-decoration: none; " +
                "           padding: 14px 30px; border-radius: 8px; font-weight: 600; font-size: 16px; " +
                "           transition: all 0.3s ease; box-shadow: 0 4px 6px rgba(67, 97, 238, 0.3); }" +
                "       .btn:hover { background: #3a0ca3; transform: translateY(-2px); box-shadow: 0 6px 12px rgba(67, 97, 238, 0.4); }" +
                "       .footer { text-align: center; padding: 25px; color: #718096; font-size: 14px; background-color: #f8fafc; }" +
                "       .disclaimer { max-width: 500px; margin: 10px auto; font-size: 13px; opacity: 0.8; }" +
                "       .divider { height: 1px; background: #e2e8f0; margin: 30px 0; }" +
                "       @media (max-width: 600px) { " +
                "           .container { margin: 10px; }" +
                "           .header { padding: 30px 15px; }" +
                "           .content { padding: 20px; }" +
                "           .article-title { font-size: 22px; }" +
                "       }" +
                "   </style>" +
                "</head>" +
                "<body>" +
                "   <div class='container'>" +
                "       <div class='header'>" +
                "           <h1>Technology News Digest</h1>" +
                "           <p>Your daily update from the world of technology</p>" +
                "       </div>" +
                "       <div class='content'>" +
                "           <h2 class='article-title'>" + title + "</h2>" +
                "           <div class='date-badge'>Published: " + publishedAt + "</div>" +
                "           <div class='image-container'>" + imageHtml + "</div>" +
                "           <p class='description'>" + description + "</p>" +
                "           <div class='divider'></div>" +
                "           <a href='" + urlLink + "' class='btn'>Read Full Article</a>" +
                "       </div>" +
                "       <div class='footer'>" +
                "           <p class='disclaimer'>This is an automated email. You're receiving this because you subscribed to Tech News Digest.</p>" +
                "           <p>© " + java.time.Year.now().getValue() + " Tech News Digest | All rights reserved</p>" +
                "       </div>" +
                "   </div>" +
                "</body>" +
                "</html>";
    }
}