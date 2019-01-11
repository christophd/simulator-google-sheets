/*
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.syndesis.simulator.sheets.scenario.v4.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.simulator.util.VariableHelper;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
@Scenario(TodoSheetScenario.SPREADSHEET_ID)
public class TodoSheetScenario extends AbstractSimulatorScenario {

    public static final String SPREADSHEET_ID = "TodoList";

    private final List<List<String>> todoEntries = Arrays.asList(
                                        Collections.singletonList("Walk the dog"),
                                        Collections.singletonList("Feed the dog"),
                                        Collections.singletonList("Wash the dog"),
                                        Collections.singletonList("Play with the dog")
                                    );

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario
            .receive()
            .validationCallback((message, context) -> {
                VariableHelper.createVariablesFromRequest(message, context);

                String requestUri = Optional.ofNullable(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                        .map(Object::toString)
                        .orElse("");

                context.setVariable("todo_entries", getTodoEntries(context.getVariable(VariableHelper.Variables.MAJOR_DIMENSION.value())));

                context.setVariable("operation", getOperation(requestUri));
                context.setVariable("method", message.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD));
            });

        scenario.variable("responsePayload", "{}");

        scenario.conditional()
                .when("${method}-${operation}", Matchers.is("GET-spreadsheet"))
                .actions(scenario.createVariable("responsePayload", "{" +
                                        "\"spreadsheetId\": \"${spreadsheetId}\"," +
                                        "\"properties\": {" +
                                            "\"title\": \"TodoSheet\"" +
                                        "}" +
                                    "}"));

        scenario.conditional()
                .when("${method}-${operation}", Matchers.is("GET-values"))
                .actions(scenario.createVariable("responsePayload","{" +
                                        "\"range\": \"${sheet}!${range}\"," +
                                        "\"majorDimension\": \"${majorDimension}\"," +
                                        "\"values\": ${todo_entries}" +
                                    "}"));

        scenario.conditional()
                .when("${method}-${operation}", Matchers.is("GET-valuesBatch"))
                .actions(scenario.createVariable("responsePayload","{" +
                                    "\"spreadsheetId\": \"${spreadsheetId}\"," +
                                    "\"valueRanges\": [" +
                                        "{" +
                                            "\"range\": \"${sheet}!${range}\"," +
                                            "\"majorDimension\": \"${majorDimension}\"," +
                                            "\"values\": ${todo_entries}" +
                                        "}" +
                                    "]}"));

        scenario.conditional()
                .when("${method}-${operation}", Matchers.is("PUT-values"))
                .actions(scenario.createVariable("responsePayload", "{" +
                                    "\"spreadsheetId\": \"${spreadsheetId}\"," +
                                    "\"updatedRange\": \"${sheet}!${range}\"," +
                                    "\"updatedRows\": 0," +
                                    "\"updatedColumns\": 0," +
                                    "\"updatedCells\": 0" +
                                "}"));

        scenario
            .http()
            .send()
            .response(HttpStatus.OK)
            .payload("${responsePayload}");
    }

    private String getOperation(String requestUri) {
        String operation;
        if (requestUri.contains("/values/")) {
            operation = "values";
        } else if (requestUri.contains("/values:batchGet")) {
            operation = "valuesBatch";
        } else {
            operation = "spreadsheet";
        }

        return operation;
    }

    private String getTodoEntries(String majorDimension) {
        try {
            if ("ROWS".equals(majorDimension)) {
                return new ObjectMapper().writer().writeValueAsString(todoEntries);
            } else {
                return "[" + todoEntries.stream()
                                        .map(row -> row.get(0))
                                        .map(value -> "\"" + value + "\"")
                                        .collect(Collectors.joining(",")) + "]";
            }
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
