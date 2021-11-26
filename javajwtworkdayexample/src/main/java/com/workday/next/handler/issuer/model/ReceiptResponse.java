package com.workday.next.handler.issuer.model;

public class ReceiptResponse {
    private String id;
    private String batchID;
    private String state;
    private String tenant;
    private String clienId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClienId() {
        return clienId;
    }

    public void setClienId(String clienId) {
        this.clienId = clienId;
    }

    public String getGlobalUserId() {
        return globalUserId;
    }

    public void setGlobalUserId(String globalUserId) {
        this.globalUserId = globalUserId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getIssuerDID() {
        return issuerDID;
    }

    public void setIssuerDID(String issuerDID) {
        this.issuerDID = issuerDID;
    }

    public String getSigningDID() {
        return signingDID;
    }

    public void setSigningDID(String signingDID) {
        this.signingDID = signingDID;
    }

    public String getSchemaDID() {
        return schemaDID;
    }

    public void setSchemaDID(String schemaDID) {
        this.schemaDID = schemaDID;
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public ReceiptState getOffered() {
        return offered;
    }

    public void setOffered(ReceiptState offered) {
        this.offered = offered;
    }

    public ReceiptState getAccepted() {
        return accepted;
    }

    public void setAccepted(ReceiptState accepted) {
        this.accepted = accepted;
    }

    public ReceiptState getRevoked() {
        return revoked;
    }

    public void setRevoked(ReceiptState revoked) {
        this.revoked = revoked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastSTateTransition() {
        return lastSTateTransition;
    }

    public void setLastSTateTransition(String lastSTateTransition) {
        this.lastSTateTransition = lastSTateTransition;
    }

    private String globalUserId;
    private String recipient;
    private String issuerDID;
    private String signingDID;
    private String schemaDID;
    private String templateID;
    private ReceiptState offered;
    private ReceiptState accepted;
    private ReceiptState revoked;
    private String createdAt;
    private String lastSTateTransition;

}
