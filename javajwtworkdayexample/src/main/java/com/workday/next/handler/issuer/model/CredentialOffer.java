package com.workday.next.handler.issuer.model;

public class CredentialOffer extends PayloadModel {
    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public AddressData getData() {
        return data;
    }

    public void setData(AddressData data) {
        this.data = data;
    }


    private Recipient recipient;
    private String templateId;
    private AddressData data;

    public CredentialOffer() {}
}