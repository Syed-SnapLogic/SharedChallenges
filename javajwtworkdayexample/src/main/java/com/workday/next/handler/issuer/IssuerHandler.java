package com.workday.next.handler.issuer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.workday.next.handler.CredentialsHandler;
import com.workday.next.handler.issuer.model.AddressData;
import com.workday.next.handler.issuer.model.CredentialOffer;
import com.workday.next.handler.issuer.model.Recipient;
import com.workday.next.utils.CredentialUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.workday.next.utils.exception.NotAuthorizedException;
import com.workday.next.utils.model.Config;
import org.apache.commons.io.IOUtils;

public class IssuerHandler extends CredentialsHandler implements HttpHandler {

    private final static String ISSUER_HTML_PRE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "\n" +
            "<head>\n" +
            "    <title>Post Office Pilot</title>\n" +
            "</head>\n" +
            "\n" +
            "<style>\n" +
            "    .content {\n" +
            "      max-width: 800px;\n" +
            "      margin: auto;\n" +
            "      text-align: center;\n" +
            "      font-family: IBM Plex Sans,Helvetica Neue,Helvetica,Arial,sans-serif;\n" +
            "    }\n" +
            "    .submissionField {\n" +
            "        width: 240px; border: 1px solid #999999; padding: 5px; margin: 5px;\n" +
            "    }\n" +
            "    </style>\n" +
            "<body>\n" +
            "\n" +
            "<div class=\"content\">\n" +
            "    <div class=\"header\" >\n" +
            "        <img style=\"height: 100%\" src=\"resources/images/postal_logo.png\">\n" +
            "    </div>\n" +
            "\n" +
            "    <div class=\"content\">\n" +
            "\n" +
            "\n" +
            "        <h1>Contact</h1>\n" ;
    private final static String ISSUER_HTML_FORM = "<form method=\"POST\" style=\"display: inline-block\">\n" +
            "            <label>Email*:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"email\"><br />\n" +
            "            <label>Street 1:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"street1\"><br />\n" +
            "            <label>Street 2:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"street2\"><br />\n" +
            "            <label>City:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"city\"><br />\n" +
            "            <label>State:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"state\"><br />\n" +
            "            <label>Postal Code:</label>\n" +
            "            <input class=\"submissionField\" type=\"text\" name=\"postalCode\"><br />\n" +
            "            <input type=\"submit\">\n" +
            "        </form>";
    private final static String ISSUER_HTML_POST= "\n" +
            "    </div>\n" +
            "\n" +
            "</div>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

    public IssuerHandler(final Config config) {
        this.config = config;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        if (t.getRequestMethod().equalsIgnoreCase("GET")) {

            String response = ISSUER_HTML_PRE + ISSUER_HTML_FORM + ISSUER_HTML_POST;
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } else if (t.getRequestMethod().equalsIgnoreCase("POST")) {

            InputStream payload = t.getRequestBody();
            CredentialOffer offer = buildOfferFromPayload(payload);

            String accessToken = null;
            try {
                accessToken = getAccessToken(false);
            } catch (IOException e) {
                System.out.println("Unable to get accessToken!");
                e.printStackTrace();
                return;
            }

            String receipt = null;
            int counter = 0;
            try {
                counter++;
                receipt = CredentialUtils.postPayloadToCredentialsPlatform(accessToken, offer, config.getOffersEndpoint());
            } catch (NotAuthorizedException e) {
                if (e.getStatus() == 401 && counter == 1) {
                    // force a new access token and retry as maybe the accessToken has expired.  Only do this 1 time.
                    e.printStackTrace(); // log and retry
                    try {
                        accessToken = getAccessToken(true);
                    } catch (IOException ioe) {
                        System.out.println("Unable to get accessToken!");
                        ioe.printStackTrace();
                        return;
                    }
                    counter++;
                    receipt = CredentialUtils.postPayloadToCredentialsPlatform(accessToken, offer, config.getOffersEndpoint());
                }
            }

            String response = ISSUER_HTML_PRE + receipt + ISSUER_HTML_POST;

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }

    }
    private CredentialOffer buildOfferFromPayload(final InputStream payload ) throws IOException {

        StringWriter writer = new StringWriter();

        IOUtils.copy(payload, writer, "UTF-8");
        String theString = writer.toString();
        String[] pairs = theString.split("&");
        Map<String, String> formFields = new HashMap<String, String>();
        for (String pair : pairs) {
            String[] values = pair.split("=");
            formFields.put(values[0], values.length == 2 ? values[1] : null);
        }

        String issuee = URLDecoder.decode(formFields.get("email"), "UTF-8");

        Recipient sub = new Recipient();
        sub.setEmail(issuee);

        AddressData data = new AddressData();

        if (formFields.get("street1") != null) {
            data.setStreet1(URLDecoder.decode(formFields.get("street1"), "UTF-8"));
        }
        if (formFields.get("street2") != null) {
            data.setStreet2(URLDecoder.decode(formFields.get("street2"), "UTF-8"));
        }
        if (formFields.get("city") != null) {
            data.setCity(URLDecoder.decode(formFields.get("city"), "UTF-8"));
        }
        if (formFields.get("state") != null) {
            data.setState(URLDecoder.decode(formFields.get("state"), "UTF-8"));
        }
        if (formFields.get("postalCode") != null) {
            data.setPostalCode(URLDecoder.decode(formFields.get("postalCode"), "UTF-8"));
        }
        data.setCountry("United States");


        CredentialOffer offer = new CredentialOffer();

        offer.setRecipient(sub);
        offer.setTemplateId(config.getCredentialDefinitions().get("address"));
        offer.setData(data);

        return offer;
    }

    @Override
    protected String getKeyFile() {
        return config.getIssuer().get("keyFile");
    }


    @Override
    protected String getClientId() {
        return config.getIssuer().get("clientId");
    }
}

