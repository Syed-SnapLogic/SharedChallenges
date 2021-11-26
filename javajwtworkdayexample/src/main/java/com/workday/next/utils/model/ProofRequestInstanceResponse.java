package com.workday.next.utils.model;

public class ProofRequestInstanceResponse {
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getProofRequestInstanceId() {
        return proofRequestInstanceId;
    }

    public void setProofRequestInstanceId(String proofRequestInstanceId) {
        this.proofRequestInstanceId = proofRequestInstanceId;
    }

    private String qrCode;
    private String proofRequestInstanceId;

    ProofRequestInstanceResponse () {}
}
