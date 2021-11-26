package com.workday.next.handler.issuer;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.workday.next.handler.CredentialsHandler;
import com.workday.next.handler.issuer.model.ReceiptResponse;
import com.workday.next.utils.model.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class StatusHandler extends CredentialsHandler implements HttpHandler {

    public StatusHandler(final Config config) {
        this.config = config;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            String queryString = t.getRequestURI().getQuery();
            //there should only be on query String parameter.  So let's split on = and get the value.
            if (queryString != null) {
                // we're expecting a single query string parameter that's named receiptId.  We'll assume everything after it is the value for the receipt.
                // check to makes sure that the query string starts with receiptId and is longer than just receiptId=
                if (queryString.startsWith("receiptId=") && queryString.length() > 11 ){
                    // Split the string along '=' and assume the 2nd array index is the value
                    String receiptId = queryString.split("=")[1];
                    getCredentialStatusFromReceiptId(t,receiptId);
                }

            }

        }
    }

    private void getCredentialStatusFromReceiptId(HttpExchange t, final String receiptId) throws IOException {

        String accessToken = null;

        // try to get the accessToken and fail if you can't
        try {
            accessToken = getAccessToken(false);
        } catch (IOException e) {
            System.out.println("Unable to get accessToken!");
            e.printStackTrace();
            return;
        }

        String receiptApiEndppint = config.getReceiptEndpoint() + "/" + receiptId;

        //   int postDataLength = postData.length();
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        String resp = null;
        try {

            URL url = new URL(receiptApiEndppint);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.connect();
            int status = conn.getResponseCode();

            if (status == 401) { // Access Token has likely expired, so retry with a new access token.
                try {
                    accessToken = getAccessToken(true);
                } catch (IOException e) {
                    System.out.println("Unable to get accessToken!");
                    e.printStackTrace();
                    return;
                }

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-length", "0");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.connect();
                status = conn.getResponseCode();

            }

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    Gson gson = new Gson();
                    ReceiptResponse response = gson.fromJson(sb.toString(), ReceiptResponse.class);

                    StringBuffer responseBuffer = new StringBuffer();
                    responseBuffer.append("cred status: " + response.getState() + "\n");
                    responseBuffer.append("tenant: " + response.getTenant() + "\n");
                    responseBuffer.append("recipient: " + response.getRecipient());
                    resp = responseBuffer.toString();
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }


        if (resp != null) {
            t.sendResponseHeaders(200, resp.length());
            OutputStream os = t.getResponseBody();
            os.write(resp.getBytes());
            os.close();
        }


    }
    @Override
    protected String getKeyFile() {
        return config.getIssuer().get("keyFile");
    }


    @Override
    protected String getClientId() {
        return config.getIssuer().get("clientId");
    }

}

