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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.snaplogic.account.api.AccountType;
import com.snaplogic.account.api.ValidatableAccount;
import com.snaplogic.account.api.capabilities.AccountCategory;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.account.oauth2.OAuth2Account;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Account for Google Drive OAuth2 Authentication
 */
@General(title = "Google Drive OAuth2 Account")
@Version(snap = 1)
@AccountCategory(type = AccountType.OAUTH2)
public class GoogleDriveAccount extends OAuth2Account<String>
        implements ValidatableAccount<String> {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveAccount.class);
    private static final Set<String> APPROVAL_PROMPT_TYPES = ImmutableSet.of("auto", "force");
    protected static final String ACCESS_TYPE = "access_type";
    protected static final String APPROVAL_PROMPT = "approval_prompt";
    protected static final String OFFLINE = "offline";

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        super.defineProperties(propertyBuilder);
        SnapProperty tokenParam = propertyBuilder.describe("tokenParam", "Token endpoint parameter",
                        "Defines an optional token endpoint parameter.")
                .build();
        SnapProperty tokenParamValue = propertyBuilder.describe("tokenParamValue", "Token endpoint parameter value",
                        "Defines an optional token endpoint parameter value.")
                .build();
        propertyBuilder.describe("token_endpoint_config", "Token endpoint config",
                        "Provides custom properties for the OAuth2 token endpoint.")
                .type(SnapType.TABLE)
                .withEntry(tokenParam)
                .withEntry(tokenParamValue)
                .add();
    }
    /**
     * Ensures that the OAuth 2.0 authorization request URL, sent by the UI using the saved account
     * properties, will include {@code "access_type=offline"} so that a {@code refresh_token} is
     * received in Google's token response.
     *
     * @param propertyBuilder as the builder
     * @see <a href="https://developers.google.com/identity/protocols/OAuth2WebServer">Redirecting
     * to Google's OAuth 2.0 server</a>
     */
    @Override
    public List<SnapProperty> defineAuthEndpointProperties(final PropertyBuilder propertyBuilder) {
        return new ImmutableList.Builder<SnapProperty>()
                /*
                see "Offline Access"
                https://developers.google.com/identity/protocols/OAuth2WebServer?hl=en
                and "When should I use force",
                http://www.riskcompletefailure.com/2013/12/are-you-using-approvalpromptforce.html
                 */
                .add(propertyBuilder.describe(ACCESS_TYPE, "Access Type", "Indicates whether the Snap needs to access a Google " +
                                "API when the user is not present at the browser. The property value \"offline\" " +
                                "results in obtaining a refresh token the first time authorization has been " +
                                "received.")
                        .makeReadOnly()
                        .defaultValue(OFFLINE)
                        .build())
                .add(propertyBuilder.describe(APPROVAL_PROMPT, "Approval Prompt",
                                "Indicates whether the user should be re-prompted " +
                                        "for offline consent to receive a new refresh token. The property value \"auto\" " +
                                        "will only display the consent prompt on the first time through the OAuth 2.0 " +
                                        "authorization sequence. To manually acquire a new refresh token, set this property " +
                                        "value to \"force\" and re-authorize.")
                        .withAllowedValues(APPROVAL_PROMPT_TYPES)
                        .defaultValue("auto")
                        .build())
                .build();
    }

    @Override
    public String connect() {
        return getAccessToken();
    }

    @Override
    public void disconnect() throws ExecutionException {
        // NO OP
    }

    @Override
    public String setDefaultScope() {
        return "https://www.googleapis.com/auth/drive";
    }

    @Override
    protected String setDefaultClientId() {
        return "641557182446-rgnujh4f1nm1pvdd1rsqum7ehep574lf.apps.googleusercontent.com";
    }

    @Override
    protected String setDefaultClientSecret() {
        return "QtNzjPUrnQfJjKgW3l3JdkQN";
    }

    @Override
    public String setAuthorizationEndpoint() {
        return "https://accounts.google.com/o/oauth2/auth";
    }

    @Override
    public String setTokenEndpoint() {
        return "https://oauth2.googleapis.com/token";
    }
}