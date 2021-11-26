package com.workday.next;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.workday.next.handler.issuer.IssuerHandler;
import com.workday.next.handler.issuer.RevocationHandler;
import com.workday.next.handler.verifier.VerifierHandler;
import com.workday.next.handler.issuer.StatusHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.workday.next.utils.model.Config;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class Server {

    private static final int HTTP_PORT = 8000;
    private static final String ISSUER_ENDPOINT = "/issuer";
    public static final String VERIFIER_ENDPOINT = "/verifier";
    private final static String IMAGE_SOURCE_LOCATION = "images/";
    private final static String STATUS_ENDPOINT = "/checkStatus";
    private final static String REVOCATION_ENDPOINT = "/revokeCredential";



    public static void main(String[] args) throws Exception {

        Server me = new Server();
        InputStream configFileStream = me.getClass()
                .getClassLoader().getResourceAsStream ("config.yaml");


        Yaml yaml = new Yaml(new Constructor(Config.class));

        Config config = yaml.load(configFileStream);

        me.validateSetup(config);

        HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
        server.createContext(ISSUER_ENDPOINT, new IssuerHandler(config));
        server.createContext(VERIFIER_ENDPOINT, new VerifierHandler(config));
        server.createContext(STATUS_ENDPOINT, new StatusHandler(config));
        server.createContext(REVOCATION_ENDPOINT, new RevocationHandler(config));
        URL imagesFolder = me.getClass().getClassLoader().getResource("images");
        List<String> imgFiles = new ArrayList<String>();
        Files.walk(Paths.get(imagesFolder.getPath())).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                imgFiles.add(filePath.getFileName().toString());
            }
        });
        for (String imgFile : imgFiles) {
            server.createContext("/resources/" + IMAGE_SOURCE_LOCATION + imgFile, new ImageHandler(imgFile));
        }

        server.setExecutor(null);
        server.start();
        System.out.println("Java Simple Reference Application Server started up successfully!  Please navigate to http://localhost:" + HTTP_PORT + "/[issuer|verifier] to test out the application.");

    }
    static class ImageHandler implements HttpHandler {
        private String img;

        public ImageHandler(String img) {
            this.img = img;
        }

        @Override
        public void handle(HttpExchange http) throws IOException {

            Server me = new Server();
            if (http.getRequestMethod().equals("GET")) {
                InputStream image = me.getClass()
                        .getClassLoader().getResourceAsStream (IMAGE_SOURCE_LOCATION + img);

                byte[] result = IOUtils.toByteArray(image);

                http.sendResponseHeaders(200,result.length);
                OutputStream os = http.getResponseBody();
                os.write(result);
                os.flush();
                os.close();
            }
        }
    }

    private void validateSetup(final Config config) {

        // validate the keyfiles exist
        InputStream issuerKeyFileInputStream = this.getClass()
                .getClassLoader().getResourceAsStream (config.getIssuer().get("keyFile"));

        if (issuerKeyFileInputStream == null) {
            System.out.println("Setup Validation failed.  Did not find the issuer key file (" + config.getIssuer().get("keyFile") + ") specified config.yaml");
        }

        InputStream verifierKeyFileInputStream = this.getClass()
                .getClassLoader().getResourceAsStream (config.getVerifier().get("keyFile"));

        if (verifierKeyFileInputStream == null) {
            System.out.println("Setup Validation failed.  Did not find the verifier key file (" + config.getVerifier().get("keyFile") + ") specified config.yaml");
        }

        if (verifierKeyFileInputStream == null || issuerKeyFileInputStream == null) {
            System.exit(1);
        }



    }
}
