package com.syed.s3.iam;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.google.common.net.MediaType;


public class Test {
    public static void main(String s[]) throws Exception {
        boolean flag = Boolean.parseBoolean(System.getProperty("useKms"));
        ClientConfiguration config = new ClientConfiguration();
        config
                .withUserAgentPrefix("APN/1.0 SnapLogic/1.0 jfs/4.26")
                .withSocketTimeout(180 * 1000);config.setRetryPolicy(
                PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(3));
        AmazonS3Client s3 = new AmazonS3Client(new InstanceProfileCredentialsProvider(), config);
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest("syed-snap6911-1", "hello.txt");
        if (flag) {
            System.out.println("using kms key");
            SSEAwsKeyManagementParams sseAwsKeyManagementParams =
                    new SSEAwsKeyManagementParams("28e3c2b6-74e2-4a3e-9890-6cd8e1c03661");
            initRequest.setSSEAwsKeyManagementParams(sseAwsKeyManagementParams);
        } else {
            System.out.println("no kms key used");
        }
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(MediaType.OCTET_STREAM.toString());
        initRequest.setObjectMetadata(objectMetadata);
        System.out.println(s3.initiateMultipartUpload(initRequest).getUploadId());
    }
}
