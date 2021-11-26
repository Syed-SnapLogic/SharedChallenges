package com.workday.next.handler.issuer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.workday.next.handler.CredentialsHandler;
import com.workday.next.handler.issuer.model.RevocationRequest;
import com.workday.next.utils.CredentialUtils;
import com.workday.next.utils.exception.NotAuthorizedException;
import com.workday.next.utils.model.Config;

import java.io.IOException;
import java.io.OutputStream;

public class RevocationHandler  extends CredentialsHandler implements HttpHandler {

    public RevocationHandler(final Config config) {
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
                    revokeCredential(t, receiptId);
                }

            }

        }
    }


    private void revokeCredential (HttpExchange t, final String receiptId) throws IOException{

        RevocationRequest reqeust = new RevocationRequest();
        String[] receipts = new String[1];
        receipts[0] = receiptId;
        reqeust.setIds(receipts);


        String accessToken = null;
        try {
            accessToken = getAccessToken(false);
        } catch (IOException e) {
            System.out.println("Unable to get accessToken!");
            e.printStackTrace();
            return;
        }

        String result = null;
        int counter = 0;
        try {
            counter++;
            result = CredentialUtils.postPayloadToCredentialsPlatform(accessToken, reqeust, config.getRevocationEndpoint());
        } catch (NotAuthorizedException e) {
            if (e.getStatus() == 401 && counter == 1) {
                // force a new access token and retry as maybe the accessToken has expired.  Only do this 1 time.
                e.printStackTrace(); // log and retry
                try {
                    accessToken = getAccessToken(true);
                } catch (IOException ioe) {
                    System.out.println("Unable to get accessToken!");
                    ioe.printStackTrace();
                    return;
                }
                counter++;
                result = CredentialUtils.postPayloadToCredentialsPlatform(accessToken, reqeust, config.getOffersEndpoint());
            }
        }

        String response = result;

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();


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

