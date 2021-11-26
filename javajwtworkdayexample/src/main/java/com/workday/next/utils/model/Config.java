package com.workday.next.utils.model;

import java.util.Map;

public class Config {
    private String account;
    private String tokenEndpoint;
    private String offersEndpoint;

    public String getReceiptEndpoint() {
        return receiptEndpoint;
    }

    public void setReceiptEndpoint(String receiptEndpoint) {
        this.receiptEndpoint = receiptEndpoint;
    }

    public String getRevocationEndpoint() {
        return revocationEndpoint;
    }

    public void setRevocationEndpoint(String revocationEndpoint) {
        this.revocationEndpoint = revocationEndpoint;
    }

    private String receiptEndpoint;
    private String revocationEndpoint;
    private String credPlatformEndpoint;
    private Map<String,String> issuer;
    private Map<String,String> verifier;
    private Map<String,String> credentialDefinitions;
    private Map<String,Map<String,String>> proofRequests;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getOffersEndpoint() {
        return offersEndpoint;
    }

    public void setOffersEndpoint(String offersEndpoint) {
        this.offersEndpoint = offersEndpoint;
    }

    public String getCredPlatformEndpoint() {
        return credPlatformEndpoint;
    }

    public void setCredPlatformEndpoint(String credPlatformEndpoint) {
        this.credPlatformEndpoint = credPlatformEndpoint;
    }

    public Map<String, String> getIssuer() {
        return issuer;
    }

    public void setIssuer(Map<String, String> issuer) {
        this.issuer = issuer;
    }

    public Map<String, String> getVerifier() {
        return verifier;
    }

    public void setVerifier(Map<String, String> verifier) {
        this.verifier = verifier;
    }

    public Map<String, String> getCredentialDefinitions() {
        return credentialDefinitions;
    }

    public void setCredentialDefinitions(Map<String, String> credentialDefinitions) {
        this.credentialDefinitions = credentialDefinitions;
    }

    public Map<String, Map<String,String>> getProofRequests() {
        return proofRequests;
    }

    public void setProofRequests(Map<String, Map<String,String>> proofRequests) {
        this.proofRequests = proofRequests;
    }

    public Config() {}
}
