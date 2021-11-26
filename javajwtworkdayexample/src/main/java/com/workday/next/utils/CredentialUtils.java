package com.workday.next.utils;

import com.google.gson.Gson;
import com.workday.next.handler.issuer.model.CredentialOffer;
import com.workday.next.handler.issuer.model.PayloadModel;
import com.workday.next.utils.exception.NotAuthorizedException;
import com.workday.next.utils.model.JWTTokenResponse;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

public class CredentialUtils {

    private static RSAPrivateKey getPrivateKeyFromString(String key) throws GeneralSecurityException {
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decodeBase64(privateKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
        return privKey;
    }

    public static String createSignedJWT(final String account, final String privateKey, final String issuer, final String subject, final long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS512;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        Key signingKey = null;
        try {
            signingKey = getPrivateKeyFromString(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().claim("tenant", account)
                .claim("scope", "Regular")
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signingKey, signatureAlgorithm);


        //if it has been specified, let's add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }


        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    public static String getAccessToken(final String jwt, final String tokenEndpoint) throws Exception {


        String grantType = null;
        try {
            grantType = URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String postData = "grant_type=" + grantType + "&assertion=" + jwt;

        int postDataLength = postData.length();
        BufferedReader reader = null;
        URL url = new URL(tokenEndpoint);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        PrintStream os = new PrintStream(conn.getOutputStream());
        os.print(postData);
        os.close();

        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        StringWriter out = new StringWriter(conn.getContentLength() > 0 ? conn.getContentLength() : 2048);

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        String response = out.toString();
        System.out.println(response);

        Gson gson = new Gson();
        JWTTokenResponse jwtTokenResponse = gson.fromJson(response, JWTTokenResponse.class);

        return jwtTokenResponse.getAccessToken();

    }

    public static String getAccessToken(final String account, final String privateKey, final String issuer, final String subject, final long ttlMillis, final String tokenEndpoint) throws IOException {
        String jwt = createSignedJWT(account, privateKey, issuer, subject, ttlMillis);
        System.out.println("jwt = " + jwt);
        String accessToken = null;
        try {
            accessToken = getAccessToken(jwt, tokenEndpoint);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return accessToken;
    }


    public static String postPayloadToCredentialsPlatform(final String accessToken, final PayloadModel payload, final String endpoint) {
        String receipt = null;

        Gson gson = new Gson();
        String offerData = gson.toJson(payload);


        int postDataLength = offerData.length();
        BufferedReader reader = null;
        try {
            URL url = new URL(endpoint);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            PrintStream os = new PrintStream(conn.getOutputStream());
            System.out.println(offerData);
            os.print(offerData);
            os.close();

            int status = conn.getResponseCode();
            if (status == 401) {
                throw new NotAuthorizedException(status, conn.getResponseMessage());
            }


            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            StringWriter out = new StringWriter(conn.getContentLength() > 0 ? conn.getContentLength() : 2048);

            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            receipt = out.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return receipt;
    }
}
