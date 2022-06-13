/*
 * SnapLogic - Data Integration
 *
 * Copyright (C) 2022, SnapLogic, Inc.  All rights reserved.
 *
 * This program is licensed under the terms of
 * the SnapLogic Commercial Subscription agreement.
 *
 * "SnapLogic" is a trademark of SnapLogic, Inc.
 */
package com.snaplogic.poc.slackbots.gdrivebot;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.MessageFileShareEvent;
import com.snaplogic.account.api.capabilities.Accounts;
import com.snaplogic.api.*;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.account.oauth2.GenericOauth2Account;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.PlatformFeature;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This snap accepts two inputs and outputs to a single output. To use it, feed
 * it two JSON documents. It will output a single stream which consists
 * of the combination of the two inputs, plus an additional "Processed" field in each.
 *
 * <p>This Snap extends {@link SimpleSnap} (instead of implementing {@link Snap}).</p>
 * <p>This means that instead of having a method called 'execute' which is called once,
 * it has a method called 'process' which is called for every document the snap receives.
 * This means you do not have to iterate over incoming documents as 'process' does that
 * for you.
 */
@General(title = "Google Drive Bot", purpose = "Upload files from slack to google drive",
        author = "SnapLogic", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 0, accepts = {ViewType.DOCUMENT})
@Outputs(min = 0, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 0, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.WRITE)
@PlatformFeature(coerceAndValidateExpressions = true)
@Accounts (provides = GoogleDriveAccount.class)
public class GoogleDriveBot extends SimpleSnap implements LifecycleCallback {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveBot.class);
    private static NetHttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static boolean startupError;
    private static Exception startupException;

    private int executionTimeout;
    private boolean runForever;
    private String botToken;
    private String appToken;
    private String channel;
    private String fileType;
    private String fileNameToBeUsed;
    private String fileUrlDownloadPrivate;
    private int gdriveState;
    private boolean gdriveSession;
    private CloseableHttpClient httpClient;
    @Inject
    private GoogleDriveAccount account;
    private App app;
    private ExecutorService service;
    private SocketModeApp socketModeApp;

    static {
        GenericOauth2Account a;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException | GeneralSecurityException e) {
            startupException = e;
            startupError = true;
        }
    }

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe("keyBotToken", "Slack Bot Token", "Bot token of the app to be used")
                .required()
                .expression()
                .obfuscate()
                .add();
        propertyBuilder.describe("keyAppToken", "Slack App Token", "App token of the slack app enabled in socket mode")
                .required()
                .expression()
                .obfuscate()
                .add();
        propertyBuilder.describe("keyExecTimeout", "Execution Timeout (seconds)", "No of seconds this snap is supposed to execute for (An empty or zero or negative value makes it run infinitely). Min positive value has to be 60 seconds.")
                .type(SnapType.INTEGER)
                .defaultValue(0)
                .expression()
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues)
            throws ConfigurationException {
        if (startupError) {
            throw new SnapDataException(startupException, "Failed to start the snap")
                    .withReason(startupException.getMessage())
                    .withResolutionAsDefect();
        }
        botToken = propertyValues.get("keyBotToken");
        appToken = propertyValues.get("keyAppToken");
        if (StringUtils.isBlank(botToken)) {
            throw new ConfigurationException("Input error")
                    .withReason("Bot token must to execute the snap")
                    .withResolution("Try again with correct value");
        }
        if (StringUtils.isBlank(appToken)) {
            throw new ConfigurationException("Input error")
                    .withReason("App token must to execute the snap")
                    .withResolution("Try again with correct value");
        }
        BigInteger timeout = propertyValues.get("keyExecTimeout");
        if (timeout != null) {
            executionTimeout = timeout.intValue();
        }
        if (executionTimeout > 0 && executionTimeout < 60) {
            throw new ConfigurationException("Input error")
                    .withReason("Positive value of the Execution Timeout property has to be min 60")
                    .withResolution("Try again with correct value");
        }
        if (executionTimeout <= 0) {
            runForever = true;
        }
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setExpectContinueEnabled(true)
                .build();
        HttpClientBuilder clientBuilder = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig);
        httpClient = clientBuilder.build();
        service = Executors.newSingleThreadExecutor();
    }

    @Override
    public void process(Document document, String inputViewName) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runBot();
                } catch (Throwable t) {
                    throw new SnapDataException(t, "Execution error")
                            .withReason(t.getMessage())
                            .withResolutionAsDefect();
                }
            }
        });
        if (!runForever) {
            try {
                Thread.sleep(executionTimeout * 1000);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        } else {
            while (runForever) {
                // NO OP
            }
        }
    }

    @Override
    public void handle(LifecycleEvent e) {
        if (runForever) {
            switch (e) {
                case CLOSE:
                case FAILURE:
                case STOP:
                case SUCCESS:
                    runForever = false;
                    break;
            }
        }
    }
    private void runBot() throws Exception {
        app = new App(AppConfig
                .builder()
                .singleTeamBotToken(botToken)
                .ignoringSelfEventsEnabled(true)
                .build());
        app.event(MessageFileShareEvent.class, (req, ctx) -> {
            if (gdriveSession && gdriveState == 1) {
                System.out.println("in file received mode");
                gdriveState = 2;
                MessageFileShareEvent event = req.getEvent();
                fileType = event.getFiles().get(0).getFiletype();
                fileNameToBeUsed = event.getFiles().get(0).getName();
                fileUrlDownloadPrivate = event.getFiles().get(0).getUrlPrivateDownload();
                ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                        .channel(channel)
                        .text("Thanks for uploading the file.  Please provide the absolute gdrive path"));
                if (!message.isOk()) {
                    ctx.logger.error("chat.postMessage failed: {}", message.getError());
                }
            }
            return ctx.ack();
        });
        app.event(MessageEvent.class, (req, ctx) -> {
            MessageEvent event = req.getEvent();
            String msg = event.getText();
            if (gdriveSession) {
                System.out.println("session on");
                if (msg.equalsIgnoreCase("cancel") ||
                        msg.equalsIgnoreCase("abort") ||
                        msg.equalsIgnoreCase("exit")) {
                    gdriveState = 0;
                    gdriveSession = false;
                    ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                            .channel(event.getChannel())
                            .text("upload session terminated"));
                    if (!message.isOk()) {
                        ctx.logger.error("chat.postMessage failed: {}", message.getError());
                    }
                } else if (gdriveState == 2) {
                    System.out.println("session on; upload next");
                    String result = uploadToGoogleDrive(msg);
                    if (!msg.startsWith("/")) {
                        msg = "/" + msg;
                    }
                    if (!msg.endsWith("/")) {
                        msg = msg + "/";
                    }
                    if (!result.equals("")) {
                        gdriveState = 0;
                        gdriveSession = false;
                        String msg1 = result.toLowerCase().startsWith("failed") ?
                                "Failed to upload file due to the error: " + (result.substring(8)) :
                                "Successfully uploaded the file with ID: " + result;
                        ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                                .channel(event.getChannel())
                                .text(msg1));
                        if (!message.isOk()) {
                            ctx.logger.error("chat.postMessage failed: {}", message.getError());
                        }
                        if (msg1.startsWith("S")) {
                            Map<String, String> map = new LinkedHashMap<>();
                            map.put("File ID", msg1.substring(40));
                            map.put("File Name", fileNameToBeUsed);
                            map.put("File Path", StringUtils.isBlank(msg) ? "/" : msg);
                            outputViews.write(documentUtility.newDocument(map));
                        }
                    } else {
                        ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                                .channel(event.getChannel())
                                .text("Given path looks invalid, try giving valid path now."));
                        if (!message.isOk()) {
                            ctx.logger.error("chat.postMessage failed: {}", message.getError());
                        }
                    }
                } else {
                    ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                            .channel(event.getChannel())
                            .text("Upload a file, or type cancel/exit/abort"));
                    if (!message.isOk()) {
                        ctx.logger.error("chat.postMessage failed: {}", message.getError());
                    }
                }
            } else {
                if (msg.toLowerCase().startsWith("upload file to google drive")) {
                    System.out.println("upload starts now");
                    gdriveSession = true;
                    gdriveState = 1;
                    channel = event.getChannel();
                    ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                            .channel(event.getChannel())
                            .text("Session to upload a file to google drive started.  Please type cancel/quit/abort anytime to cancel this session.\nPlease upload a file first."));
                    if (!message.isOk()) {
                        ctx.logger.error("chat.postMessage failed: {}", message.getError());
                    }
                }
            }

            return ctx.ack();
        });
        socketModeApp = new SocketModeApp(appToken, app);
        socketModeApp.startAsync();
        System.out.println("socketmodeapp started finally");
    }

    @Override
    public void cleanup() throws ExecutionException {
        System.out.println("cleanup invoked");
        service.shutdown();
        while (!service.isShutdown()) {
            service.shutdownNow();
        }
        System.out.println("service shutdown");
        try {
            if (httpClient != null) {
                httpClient.close();
                System.out.println("http client closed");
            }
        } catch (Throwable t) {
            // no op
        }
        if (socketModeApp != null) {
            try {
                socketModeApp.stop();
                socketModeApp.close();
                System.out.println("socket app stopped/closed");
            } catch (Exception e) {
                LOG.debug("error closing socket app", e);
                e.printStackTrace();
            }
        }
        if (app != null) {
            app.stop();
            System.out.println("app stopped");
        }
    }

    private String uploadToGoogleDrive(String gdrivePath) {
        HttpGet request = new HttpGet(fileUrlDownloadPrivate);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + botToken);
        CloseableHttpResponse response = null;
        String accessTokenDrive = account.connect();
        try {
            response = httpClient.execute(request);
            InputStream is = response.getEntity().getContent();
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessTokenDrive);
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Syed Slack Bot for G-Drive")
                    .build();
            File fileMetadata = new File();
            fileMetadata.setName(fileNameToBeUsed);
            String mediaType;
            switch (fileType.toLowerCase()) {
                case "html":
                    mediaType = "text/html";
                    break;
                case "txt":
                    mediaType = "text/plain";
                    break;
                case "pdf":
                    mediaType = "application/pdf";
                    break;
                case "csv":
                    mediaType = "text/csv";
                    break;
                case "jpeg":
                    mediaType = "image/jpeg";
                    break;
                case "png":
                    mediaType = "image/png";
                    break;
                case "mp4":
                    mediaType = "video/mp4";
                    break;
                default:
                    mediaType = "text/plain";
                    break;
            }
            if (StringUtils.isNotBlank(gdrivePath)) {
                List<String> parents = getParents(gdrivePath, service);
                if (parents == null) {
                    return "";
                }
                fileMetadata.setParents(parents);
            }
            File op = service
                    .files()
                    .create(fileMetadata, new InputStreamContent(mediaType, is))
                    .execute();
            return op.getId();
        } catch (Throwable t) {
            t.printStackTrace();
            return "failed: " + t.getMessage();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Throwable t) {
                // no op
            }
        }
    }

    List<String> getParents(String gdrivePath, Drive service) throws IOException {
        List<String> parents = new ArrayList<>();
        String parts[] = gdrivePath.split("/");
        String parentFolderId = null;
        for (String part : parts) {
            if (StringUtils.isBlank(part)) {
                continue;
            }
            Drive.Files.List list = service.files().list();
            if (parentFolderId == null) {
                list.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + part + "'");
            } else {
                list.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + part +
                        "' and parents in '" + parentFolderId + "'");
            }
            list.setFields("files(id, name)");
            try {
                String folderId = list.execute().getFiles().get(0).getId();
                parents.add(folderId);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
        }
        return parents;
    }
}