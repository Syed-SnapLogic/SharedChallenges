package com.workday.next.handler.verifier;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.workday.next.Server;
import com.workday.next.handler.CredentialsHandler;
import com.workday.next.handler.verifier.model.PollingResponse;
import com.workday.next.utils.model.Config;
import com.workday.next.utils.model.ProofRequestInstanceRequest;
import com.workday.next.utils.model.ProofRequestInstanceResponse;
import com.workday.next.utils.model.ProofResponses;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VerifierHandler extends CredentialsHandler implements HttpHandler  {
    private final static String VERIFIER_HTML_PRE_PR = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "    <head>\n" +
            "        <title>Dairy Queen (test)</title>\n" +
            "        <link rel=\"icon\" href=\"https://upload.wikimedia.org/wikipedia/commons/thumb/a/ae/Dairy_Queen_logo.svg/2000px-Dairy_Queen_logo.svg.png\">\n" +
            "         <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js\"></script>\n" +
            "    </head>\n" +
            "    <style>\n" +
            "    .content {\n" +
            "      max-width: 800px;\n" +
            "      margin: auto;\n" +
            "      text-align: center;\n" +
            "      font-family: IBM Plex Sans,Helvetica Neue,Helvetica,Arial,sans-serif;\n" +
            "    }\n" +
            "    </style>\n" +
            "  <body>\n" +
            "\n" +
            "    <script>\n" +
            "      (function() {\n" +
            "          poll = function() {\n" +
            "            $.ajax({\n" +
            "              url: '";
    private final static String VERIFIER_POLLING_URI_PRE = "/verifier/poll/";
    private final static String VERIFIER_HTML_POST_PR = "',\n" +
            "              dataType: 'json',\n" +
            "              type: 'get',\n" +
            "              success: function(resp) { \n" +
            "                console.log(resp)\n" +
            "                if ( !resp.found ) {\n" +
            "                    return\n" +
            "                }\n" +
            "\n" +
            "                clearInterval(pollInterval); \n" +
            "                document.getElementById(\"qrCode\").style.display = \"none\"\n" +
            "\n" +
            "                if ( resp.valid ) {\n" +
            "                    document.getElementById(\"coupon\").style.display = \"block\"\n" +
            "                } else {\n" +
            "                    document.getElementById(\"sorry\").style.display = \"block\"\n" +
            "                }\n" +
            "              },\n" +
            "              error: function(request, error) { \n" +
            "                console.error('err: '+error);\n" +
            "              },\n" +
            "            complete: function () {\n" +
            "              $('.qrCode').hide();\n" +
            "              console.log(\"Complete!\");\n" +
            "            }\n" +
            "            });\n" +
            "          },\n" +
            "          pollInterval = setInterval(function() { \n" +
            "            poll();\n" +
            "          }, 2000);\n" +
            "      })();\n" +
            "    </script>\n" +
            "\n" +
            "    <div class=\"content\">\n" +
            "      <div class=\"header\" >\n" +
            "        <img style=\"height: 160px\" src=\"resources/images/happyLogo.png\">\n" +
            "       </div>\n" +
            "\n" +
            "      <div id=\"qrCode\" class=\"content\">\n" +
            "        <p>Boulder neighbors, scan QR code to get free ice cream!</p>\n" +
            "        <img id=\"qrcode\" src=\"";
    private final static String VERIFIER_HTML_POST = "\">\n" +
            "      </div>\n" +
            "\n" +
            "      <img style=\"margin: 0 auto; display: none\" id=\"coupon\" src=\"resources/images/coupon.png\">\n" +
            "\n" +
            "      <div style=\"display: none\" id=\"sorry\" class=\"content\">\n" +
            "        <p>Sorry, this promotion is exclusive for our Boulder neighbors.</p>\n" +
            "      </div>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "  </body>\n" +
            "</html>";

    private String pollingURI;
    private String proofRequestInstanceId;
    public VerifierHandler(final Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return this.config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getProofRequestInstanceId() {
        return proofRequestInstanceId;
    }

    public void setProofRequestInstanceId(String proofRequestInstanceId) {
        this.proofRequestInstanceId = proofRequestInstanceId;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {

            if (t.getRequestURI().toString().equalsIgnoreCase(Server.VERIFIER_ENDPOINT)) {
                // This is the branch of logic that handles the initial navigation to the verifier uri.
                handlePresentationOfQRCode(t);

            } else {
                if (t.getRequestURI().toString().equalsIgnoreCase(this.getPollingURI())) {
                    // This branch of logic handles polling for the proof request response.
                    handlePollingForProofResponse(t);
                }
            }
        }
    }

    private void handlePresentationOfQRCode(HttpExchange t) {

        String accessToken = null;

        // try to get the accessToken and fail if you can't
        try {
            accessToken = getAccessToken(false);
        } catch (IOException e) {
            System.out.println("Unable to get accessToken!");
            e.printStackTrace();
            return;
        }

        ProofRequestInstanceRequest prir = new ProofRequestInstanceRequest();
        prir.setProofRequestId(this.getConfig().getProofRequests().get("verifier").get("id"));
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(prir);

        BufferedReader reader = null;
        String response = null;

        try {

            URL url = new URL(config.getCredPlatformEndpoint() + "/v1/proof-requests-instance");
            HttpURLConnection conn = postJsonPayload(url, jsonPayload, accessToken);

            int status = conn.getResponseCode();
            if (status == 401) { //Assuming the accessToken has expired.  Let's retry once!
                try {
                    accessToken = getAccessToken(false);
                } catch (IOException e) {
                    System.out.println("Unable to get accessToken!");
                    e.printStackTrace();
                    return;
                }
                conn = postJsonPayload(url, jsonPayload, accessToken);
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringWriter out = new StringWriter(conn.getContentLength() > 0 ? conn.getContentLength() : 2048);

            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            response = out.toString();
            System.out.println(response);

            ProofRequestInstanceResponse proofRequestInstanceResponse = gson.fromJson(response, ProofRequestInstanceResponse.class);
            this.setProofRequestInstanceId(proofRequestInstanceResponse.getProofRequestInstanceId());
            this.setPollingURI(VERIFIER_POLLING_URI_PRE + this.getProofRequestInstanceId());

            String htmlResponse = VERIFIER_HTML_PRE_PR + this.getPollingURI() + VERIFIER_HTML_POST_PR + proofRequestInstanceResponse.getQrCode() + VERIFIER_HTML_POST;
            t.sendResponseHeaders(200, htmlResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(htmlResponse.getBytes());
            os.close();

        } catch (IOException e) {}



    }

    private void handlePollingForProofResponse(HttpExchange t) throws IOException {

        String accessToken = null;

        // try to get the accessToken and fail if you can't
        try {
            accessToken = getAccessToken(false);
        } catch (IOException e) {
            System.out.println("Unable to get accessToken!");
            e.printStackTrace();
            return;
        }

        String proofResponsesEndpoint = config.getCredPlatformEndpoint() + "/v1/proof-requests-instance/" + this.getProofRequestInstanceId() + "/proof-responses";

        //   int postDataLength = postData.length();
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        String resp = null;
        try {

            URL url = new URL(proofResponsesEndpoint);

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
                    ProofResponses response = gson.fromJson(sb.toString(), ProofResponses.class);
                    PollingResponse pollingResponse = new PollingResponse();

                    if (!response.getData().isEmpty()) {
                        if (!response.getData().get(0).getVerifiedData().isEmpty()) {
                            pollingResponse.setFound(true);
                            if (response.getData().get(0).getVerifiedData().get(0).getAttributes().get(0).get("city").equalsIgnoreCase("boulder")) {
                                pollingResponse.setValid(true);
                            }
                        }
                    } else {
                        pollingResponse.setFound(false);
                    }

                    if (pollingResponse.isFound()) {
                        resp = gson.toJson(pollingResponse);
                    }
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

    private HttpURLConnection postJsonPayload(final URL url, final String jsonPayload, final String accessToken) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", Integer.toString(jsonPayload.length()));
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        PrintStream pos = new PrintStream(conn.getOutputStream());
        pos.print(jsonPayload);
        pos.close();

        return conn;
    }

    public String getPollingURI() {
        return pollingURI;
    }

    public void setPollingURI(String pollingURI) {
        this.pollingURI = pollingURI;
    }

    @Override
    protected String getKeyFile() {
        return config.getVerifier().get("keyFile");
    }

    @Override
    protected String getClientId() {
        return config.getVerifier().get("clientId");
    }
}