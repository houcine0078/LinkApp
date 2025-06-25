package services;

import okhttp3.*;


public class FirebaseAuthService {
    private static final String API_KEY = "AIzaSyCPcJ0ujBDdDT8-zNZcmnZnc3iTYARqH5c";
    private static final OkHttpClient client = new OkHttpClient();
    // Add your Firebase Realtime Database URL here
    public static final String DATABASE_URL = "https://linkapp-db-a988c-default-rtdb.firebaseio.com/";

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