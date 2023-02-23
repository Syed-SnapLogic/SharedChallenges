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
package com.snaplogic.snaps.test;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.api.Snap;
import com.snaplogic.common.jsonpath.JsonPath;
import com.snaplogic.common.properties.Suggestions;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.common.properties.builders.SuggestionBuilder;
import com.snaplogic.jsonpath.JsonPaths;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.OutputViews;
import com.snaplogic.snap.api.PropertyCategory;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This Snap has one output and writes one document that contains the suggested
 * value.
 *
 * <p>This snap demonstrates the suggest value functionality that uses the
 * partial configuration information to suggest a property value.</p>
 */
@Version(snap = 1)
@General(title = "Suggest", purpose = "Demo suggest functionality.",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Errors(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Category(snap = SnapCategory.READ)
public class Suggest implements Snap {

    public static final String PROP_NAME = "name";
    public static final String PROP_ECHO = "echo";
    static final JsonPath PATH_LABEL = JsonPaths.compileStatic("$label.value");
    static final JsonPath PATH_INTERNAL_ID = JsonPaths.compileStatic("$internalId.value");
    static final JsonPath PATH_FIELD_TYPE = JsonPaths.compileStatic("$fieldType.value");
    private String valueToWrite;
    @Inject
    private DocumentUtility documentUtility;
    @Inject
    private OutputViews outputViews;

    @Override
    public void defineProperties(final PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(PROP_NAME, PROP_NAME)
                .add();
        propertyBuilder.describe(PROP_ECHO, PROP_ECHO)
                .withSuggestions(new Suggestions() {
                    @Override
                    public void suggest(SuggestionBuilder suggestionBuilder,
                            PropertyValues propertyValues) {
                        String name = propertyValues.get(PropertyCategory.SETTINGS, PROP_NAME);
                        suggestionBuilder.node(PROP_ECHO).suggestions("hello", "bye", "hi");
                    }
                }).add();
        propertyBuilder.describe("objsugg", "obj sugg", "obj suggestions")
                .withSuggestions(new Suggestions() {
                    @Override
                    public void suggest(SuggestionBuilder suggestionBuilder, PropertyValues propertyValues) {
                        List<Map<String, Object>> suggestions = new ArrayList<>();
                        Map<String, Object> suggestion1 = Maps.newHashMap();
                        PATH_LABEL.writeStatic(suggestion1, "1st suggestion");
                        PATH_INTERNAL_ID.writeStatic(suggestion1, "id1");
                        PATH_FIELD_TYPE.writeStatic(suggestion1, "String");
                        suggestions.add(suggestion1);
                        Map<String, Object> suggestion2 = Maps.newHashMap();
                        PATH_LABEL.writeStatic(suggestion2, "2nd suggestion");
                        PATH_INTERNAL_ID.writeStatic(suggestion2, "id2");
                        PATH_FIELD_TYPE.writeStatic(suggestion2, "Integer");
                        suggestions.add(suggestion2);
                        Map<String, Object> suggestion3 = Maps.newHashMap();
                        PATH_LABEL.writeStatic(suggestion3, "3rd suggestion");
                        PATH_INTERNAL_ID.writeStatic(suggestion3, "id3");
                        PATH_FIELD_TYPE.writeStatic(suggestion3, "Boolean");
                        suggestions.add(suggestion3);
                        suggestionBuilder.node("objsugg").objectSuggestions(suggestions);
                    }
                }).add();
    }

    @Override
    public void configure(final PropertyValues propertyValues) throws ConfigurationException {
        valueToWrite = propertyValues.get(PROP_ECHO);
        valueToWrite = valueToWrite + ":sugg:" + propertyValues.get("syed1");
    }

    @Override
    public void execute() throws ExecutionException {
        Map<String, String> data = new LinkedHashMap<String, String>() {{
            put("key", valueToWrite);
        }};
        outputViews.write(documentUtility.newDocument(data));
    }

    @Override
    public void cleanup() throws ExecutionException {
        // NOOP
    }
}