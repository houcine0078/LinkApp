package services;

import okhttp3.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FirebaseAuthService {
    private static final String CONFIG_FILE = "config.properties";
    private static String API_KEY;
    private static String DATABASE_URL;
    private static final OkHttpClient client = new OkHttpClient();

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            API_KEY = props.getProperty("FIREBASE_API_KEY");
            DATABASE_URL = props.getProperty("FIREBASE_DATABASE_URL");
            if (API_KEY == null || DATABASE_URL == null) {
                throw new RuntimeException("Missing FIREBASE_API_KEY or FIREBASE_DATABASE_URL in config.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties. Please create it and add FIREBASE_API_KEY and FIREBASE_DATABASE_URL.", e);
        }
    }

    public static String register(String email, String password) throws Exception {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String login(String email, String password) throws Exception {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String getUserByEmail(String email) throws Exception {
        String url = DATABASE_URL + "/users.json?orderBy=\"email\"&equalTo=\"" + email + "\"";
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String storeUserData(String userId, String name, String username, String email, String password) throws Exception {
        String url = DATABASE_URL + "/users/" + userId + ".json";
        String json = String.format("{\"name\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                name, username, email, password);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).put(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }
}