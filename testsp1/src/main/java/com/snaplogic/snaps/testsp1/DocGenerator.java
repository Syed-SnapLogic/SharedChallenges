/*
 * SnapLogic - Data Integration
 *
 * Copyright (C) 2016, SnapLogic, Inc.  All rights reserved.
 *
 * This program is licensed under the terms of
 * the SnapLogic Commercial Subscription agreement.
 *
 * "SnapLogic" is a trademark of SnapLogic, Inc.
 */
package com.snaplogic.snaps.testsp1;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.Suggestions;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.common.properties.builders.SuggestionBuilder;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.ErrorViews;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Snap that generates the configured number of documents.
 */
@General(title = "Doc Generator", purpose = "Generates documents based on the configuration",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class DocGenerator implements Snap {
    private static final String COUNT = "count";

    // Document utility is the only way to create a document
    // or manipulate the document header
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;
    @Inject
    private ErrorViews errorViews;
    private int count;
    Set<String> set = ImmutableSet.of("Hello There", "How Are you?");
    String policy;

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(COUNT, "Number of documents to create")
                .type(SnapType.INTEGER).required().add();
        propertyBuilder.describe("policy", "Policy", "some dsc")
                .withSuggestions((suggestionBuilder, propertyValues) -> suggestionBuilder.node("policy").suggestions(set.toArray(new String[0])))
                .defaultValue("Hello There")
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        Number countValue = propertyValues.get(COUNT);
        count = countValue.intValue();
        policy = propertyValues.get("policy");
    }

    @Override
    public void execute() throws ExecutionException {
        if (count < 0) {
            while (true) {
                Map<String, String> data = new LinkedHashMap<>();
                data.put("key", "value" + ((Math.random() * 100) + 1));
                outputViews.write(documentUtility.newDocument(data));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            for (int i = 0; i < count; i++) {
                Map<String, String> data = new LinkedHashMap<>();
                data.put("key", "value" + (i + 1));
                data.put("policy", policy);
                outputViews.write(documentUtility.newDocument(data));
            }
        }
    }

    @Override
    public void cleanup() throws ExecutionException {
        // NOOP
    }
}