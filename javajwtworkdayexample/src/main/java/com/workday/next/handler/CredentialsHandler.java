package com.workday.next.handler;

import com.workday.next.utils.CredentialUtils;
import com.workday.next.utils.model.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class CredentialsHandler {

    protected Config config;
    protected String base64EncodedPrivateKey;
    private String accessToken;

    protected String getAccessToken(final boolean forceReset) throws IOException {

        if (this.accessToken != null && !forceReset) {
            return this.accessToken;
        } else {
            this.accessToken =  CredentialUtils.getAccessToken( config.getAccount(), getBase64EncodedPrivateKey(), getClientId(), getClientId(),60*60*1000, config.getTokenEndpoint());
            return this.accessToken;
        }
    }

    protected abstract String getKeyFile();
    protected String getBase64EncodedPrivateKey() throws IOException {
        if (base64EncodedPrivateKey == null) {
            InputStream keyFileStream = this.getClass()
                    .getClassLoader().getResourceAsStream (getKeyFile());

            String str = "";
            StringBuffer buf = new StringBuffer();
            boolean start = false;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(keyFileStream));
                if (keyFileStream != null) {
                    while ((str = reader.readLine()) != null) {
                        if (str.equalsIgnoreCase("-----BEGIN PRIVATE KEY-----")) {
                            start = true;
                        }
                        if (start) {
                            buf.append(str + "\n");
                        }
                        if (str.equalsIgnoreCase("-----END PRIVATE KEY-----")) {
                            start = false;
                        }
                    }
                }
            } finally {
                try { keyFileStream.close(); } catch (Throwable ignore) {}
            }
            base64EncodedPrivateKey = buf.toString();

        }
        return base64EncodedPrivateKey;
    }
    protected abstract String getClientId();

}
