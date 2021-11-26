package com.workday.next.utils.model;

import com.google.gson.annotations.SerializedName;

public class JWTTokenResponse {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType;
    @SerializedName("expires_in") private int expiresIn;
    JWTTokenResponse() {
        // no-args constructor
    }
    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}


