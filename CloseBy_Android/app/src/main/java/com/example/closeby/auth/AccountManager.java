package com.example.closeby.auth;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.util.Scanner;

public class AccountManager {

    public static boolean createAccount(String username, String email, String password) {
        try {
            URL url = new URL("http://10.0.2.2:3000/api/register"); // Android Emulator uses 10.0.2.2 for localhost
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("email", email);
            jsonParam.put("password", password);

            OutputStream os = conn.getOutputStream();
            os.write(jsonParam.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
                return response.contains("User registered successfully");
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

