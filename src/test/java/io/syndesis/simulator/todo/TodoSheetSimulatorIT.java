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

package io.syndesis.simulator.todo;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import io.syndesis.simulator.SimulatorClientConfig;
import io.syndesis.simulator.sheets.scenario.v4.todo.TodoSheetScenario;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SimulatorClientConfig.class)
public class TodoSheetSimulatorIT extends JUnit4CitrusTestDesigner {

    @Autowired
    private HttpClient simulatorClient;

    private String accessToken = "cd887efc-7c7d-4e8e-9580-f7502123badf";

    /**
     * Get todosheet spreadsheet.
     */
    @Test
    @CitrusTest
    public void testGetTodoSpreadsheet() {
        variable("spreadsheetId", TodoSheetScenario.SPREADSHEET_ID);
        variable("title", "TodoSheet");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .validate("$.spreadsheetId", "${spreadsheetId}")
                .validate("$.properties.title", "${title}");
    }

    /**
     * Get todosheet values.
     */
    @Test
    @CitrusTest
    public void testGetTodoValues() {
        variable("spreadsheetId", TodoSheetScenario.SPREADSHEET_ID);
        variable("sheet", "TodoSheet");
        variable("range", "A1:A4");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}")
                .queryParam("majorDimension", "ROWS")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                            "\"range\": \"${sheet}!${range}\"," +
                            "\"majorDimension\": \"ROWS\"," +
                            "\"values\": [[\"Walk the dog\"],[\"Feed the dog\"],[\"Wash the dog\"],[\"Play with the dog\"]]" +
                        "}");
    }

    /**
     * Get todosheet values.
     */
    @Test
    @CitrusTest
    public void testGetTodoValuesBatch() {
        variable("spreadsheetId", TodoSheetScenario.SPREADSHEET_ID);
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values:batchGet")
                .queryParam("range", "TestSheet!A1:A4")
                .queryParam("majorDimension", "ROWS")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"${spreadsheetId}\"," +
                        "\"valueRanges\": [" +
                            "{" +
                                "\"range\": \"TestSheet!A1:A4\"," +
                                "\"majorDimension\": \"ROWS\"," +
                                "\"values\": [[\"Walk the dog\"],[\"Feed the dog\"],[\"Wash the dog\"],[\"Play with the dog\"]]" +
                            "}" +
                        "]}");
    }

    /**
     * Update todosheet values.
     */
    @Test
    @CitrusTest
    public void testUpdateTodoValues() {
        variable("spreadsheetId", TodoSheetScenario.SPREADSHEET_ID);
        variable("sheet", "TodoSheet");
        variable("range", "A5");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .put("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}")
                .header("Authorization", "Bearer ${accessToken}")
                .payload("{\"values\":[[\"New todo entry\"]]}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                            "\"spreadsheetId\": \"${spreadsheetId}\"," +
                            "\"updatedRange\": \"${sheet}!${range}\"," +
                            "\"updatedRows\": 0," +
                            "\"updatedColumns\": 0," +
                            "\"updatedCells\": 0" +
                        "}");
    }
}
