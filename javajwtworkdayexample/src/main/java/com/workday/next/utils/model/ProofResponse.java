package com.workday.next.utils.model;

import java.util.ArrayList;

public class ProofResponse {
    private String proofResponseId;
    private String proofRequestInstanceId;
    private String holderDid;
    private String callbackPayload;

    public ArrayList<VerifiedData> getVerifiedData() {
        return verifiedData;
    }

    public void setVerifiedData(ArrayList<VerifiedData> verifiedData) {
        this.verifiedData = verifiedData;
    }

    private ArrayList<VerifiedData> verifiedData;

    public String getProofResponseId() {
        return proofResponseId;
    }

    public void setProofResponseId(String proofResponseId) {
        this.proofResponseId = proofResponseId;
    }

    public String getProofRequestInstanceId() {
        return proofRequestInstanceId;
    }

    public void setProofRequestInstanceId(String proofRequestInstanceId) {
        this.proofRequestInstanceId = proofRequestInstanceId;
    }

    public String getHolderDid() {
        return holderDid;
    }

    public void setHolderDid(String holderDid) {
        this.holderDid = holderDid;
    }

    public String getCallbackPayload() {
        return callbackPayload;
    }

    public void setCallbackPayload(String callbackPayload) {
        this.callbackPayload = callbackPayload;
    }
}
