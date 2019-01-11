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

package io.syndesis.simulator.custom;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import io.syndesis.simulator.SimulatorClientConfig;
import io.syndesis.simulator.sheets.scenario.v4.custom.SolarSystemScenario;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SimulatorClientConfig.class)
public class SolarSystemSimulatorIT extends JUnit4CitrusTestDesigner {

    @Autowired
    private HttpClient simulatorClient;

    @Value("${simulator.oauth2.client.accessToken}")
    private String accessToken;

    /**
     * Get solar system values with rows as major dimension.
     */
    @Test
    @CitrusTest
    public void testGetSolarSystemRows() {
        variable("spreadsheetId", SolarSystemScenario.SPREADSHEET_ID);
        variable("accessToken", accessToken);
        variable("range", "A1:C10");
        variable("majorDimension", "ROWS");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values:batchGet")
                .queryParam("ranges", "${range}")
                .queryParam("majorDimension", "${majorDimension}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"${spreadsheetId}\"," +
                        "\"valueRanges\": [" +
                            "{" +
                                "\"range\": \"Sheet1!${range}\"," +
                                "\"majorDimension\": \"${majorDimension}\"," +
                                "\"values\": [" +
                                    "[\"Name\", \"Radius\", \"Distance\"]," +
                                    "[\"Sun\", 695000, 0]," +
                                    "[\"Jupiter\", 71492, 778.5]," +
                                    "[\"Saturn\", 60268, 1427.0]," +
                                    "[\"Neptune\", 24766, 4497.1]," +
                                    "[\"Uranus\", 25559, 2871.0]," +
                                    "[\"Earth\", 6378, 149.6]," +
                                    "[\"Venus\", 6052, 108.2]," +
                                    "[\"Mars\", 3397, 227.9]," +
                                    "[\"Mercury\", 2440, 57.9]" +
                                "]" +
                            "}" +
                        "]}");
    }

    /**
     * Get solar system values with columns as major dimension.
     */
    @Test
    @CitrusTest
    public void testGetSolarSystemColumns() {
        variable("spreadsheetId", SolarSystemScenario.SPREADSHEET_ID);
        variable("accessToken", accessToken);
        variable("range", "A1:C10");
        variable("majorDimension", "COLUMNS");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values:batchGet")
                .queryParam("ranges", "${range}")
                .queryParam("majorDimension", "${majorDimension}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"${spreadsheetId}\"," +
                        "\"valueRanges\": [" +
                            "{" +
                                "\"range\": \"Sheet1!${range}\"," +
                                "\"majorDimension\": \"${majorDimension}\"," +
                                "\"values\": [" +
                                    "[\"Name\", \"Sun\", \"Jupiter\", \"Saturn\", \"Neptune\", \"Uranus\", \"Earth\", \"Venus\", \"Mars\", \"Mercury\"]," +
                                    "[\"Radius\", 695000, 71492, 60268, 24766, 25559, 6378, 6052, 3397, 2440]," +
                                    "[\"Distance\", 0, 1427.0, 778.5, 4497.1, 2871.0, 149.6, 108.2, 227.9, 57.9]" +
                                "]" +
                            "}" +
                        "]}");
    }
}
