package com.test.syed.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.File;

public class RunFullFledgedIntegration {
    private DockerClient dockerClient;
    private DefaultDockerClientConfig defaultDockerClientConfig;
    private String imageId;
    private BuildImageCmd buildImageCmd;
    private String containerId;
    private CreateContainerCmd createContainerCmd;
    private CreateContainerResponse createContainerResponse;
    private String ipAddress;

    public static void main(String arguments[]) throws Exception {
        RunFullFledgedIntegration instance = new RunFullFledgedIntegration();
        instance.createDockerClient();
        instance.buildImageFromDockerFile();
        instance.createDockerContainer();
        instance.startContainer();
        instance.stopContainer();
        instance.waitTillContainerStops();
    }

    private void createDockerClient() throws Exception {
        defaultDockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        System.out.println("\n==== Config ====");
        System.out.println(defaultDockerClientConfig);
        System.out.println("\n");

        dockerClient = DockerClientBuilder
                .getInstance(defaultDockerClientConfig)
                .build();
        System.out.println("\nDocker client instantiated.....\n");
    }

    private void buildImageFromDockerFile() throws Exception {
        File baseDir = new File("./src/main/resources");

        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                //System.out.println("" + item);
                super.onNext(item);
            }
        };

        buildImageCmd = dockerClient
                .buildImageCmd(baseDir);
        imageId = buildImageCmd
                .exec(callback)
                .awaitImageId();
        System.out.println("\nImage ID: " + imageId);
    }

    private void createDockerContainer() throws Exception {
        containerId = "SoapBox" + java.util.UUID.randomUUID().toString();
        createContainerCmd = dockerClient
                .createContainerCmd(containerId);
        createContainerResponse = createContainerCmd
                .withImage(imageId)
                .withName(containerId)
                .exec();
        containerId = createContainerResponse
                .getId();
        System.out.println("Container created with ID: " + containerId);
        System.out.println(createContainerResponse.toString());
    }

    private void stopContainer() throws Exception {
        dockerClient
                .stopContainerCmd(containerId)
                .exec();
        System.out.println("Container being stopped....");
    }

    private void startContainer() throws Exception {
        dockerClient
                .startContainerCmd(containerId)
                .exec();
        ipAddress = getContainerIpAddress();
        System.out.println("Container started with IP Address: " + ipAddress);
        Thread.sleep(120000);
    }

    private void waitTillContainerStops() throws Exception {
        dockerClient
                .waitContainerCmd(containerId)
                .exec(null);
        System.out.println("Container successfully stopped....");
    }

    private String getContainerIpAddress() throws Exception {
        InspectContainerResponse inspectContainerResponse =  dockerClient
                .inspectContainerCmd(containerId)
                .exec();
        return inspectContainerResponse
                .getNetworkSettings()
                .getIpAddress();
    }
}