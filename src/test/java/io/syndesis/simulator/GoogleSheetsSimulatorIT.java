/*
 * Copyright 2006-2017 the original author or authors.
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

package io.syndesis.simulator;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SimulatorClientConfig.class)
public class GoogleSheetsSimulatorIT extends JUnit4CitrusTestDesigner {

    @Autowired
    private HttpClient simulatorClient;

    @Value("${simulator.oauth2.client.accessToken}")
    private String accessToken;

    /**
     * Sends get spreadsheet request to server.
     */
    @Test
    @CitrusTest
    public void testGetSpreadsheet() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestSheet");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    /**
     * Sends create spreadsheet request to server.
     */
    @Test
    @CitrusTest
    public void testCreateSpreadsheet() {
        variable("title", "TestSheet");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .post("/v4/spreadsheets")
                .header("Authorization", "Bearer ${accessToken}")
                .payload("{\"properties\": {\"title\": \"${title}\"}}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"@ignore@\", \"properties\": {\"title\": \"${title}\"}}");
    }

    /**
     * Sends get sheet values request to server.
     */
    @Test
    @CitrusTest
    public void testGetValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestSheet");
        variable("range", "A1:C5");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"range\": \"${sheet}!${range}\",\"majorDimension\": \"ROWS\",\"values\": []}");
    }

    /**
     * Sends get sheet values batch request to server.
     */
    @Test
    @CitrusTest
    public void testGetValuesBatch() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestSheet");
        variable("range", "A1:C5");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}/values:batchGet")
                .queryParam("range", "${sheet}!${range}")
                .queryParam("majorDimension", "ROWS")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                        "\"spreadsheetId\": \"${spreadsheetId}\"," +
                        "\"valueRanges\": [" +
                            "{" +
                                "\"range\": \"${sheet}!${range}\"," +
                                "\"majorDimension\": \"ROWS\"," +
                                "\"values\": []" +
                            "}" +
                        "]}");
    }

    /**
     * Sends update sheet values request to server.
     */
    @Test
    @CitrusTest
    public void testUpdateValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestSheet");
        variable("range", "A1:C5");
        variable("accessToken", accessToken);

        variable("updatedRows", 2);
        variable("updatedColumns", 2);
        variable("updatedCells", 4);

        http().client(simulatorClient)
                .send()
                .put("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}")
                .header("Authorization", "Bearer ${accessToken}")
                .payload("{\"values\":[[\"A1\",\"B1\"],[\"A2\",\"B2\"]]}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/updateValuesResponse.json"));
    }

    /**
     * Sends append sheet values request to server.
     */
    @Test
    @CitrusTest
    public void testAppendValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestSheet");
        variable("range", "A1");
        variable("accessToken", accessToken);

        variable("updatedRows", 1);
        variable("updatedColumns", 4);
        variable("updatedCells", 4);

        http().client(simulatorClient)
                .send()
                .post("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}:append")
                .header("Authorization", "Bearer ${accessToken}")
                .payload("{\"values\":[[\"A1\",\"B1\", \"C1\", \"D1\"]]}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/appendValuesResponse.json"));
    }

    /**
     * Sends clear sheet values request to server.
     */
    @Test
    @CitrusTest
    public void testClearValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestSheet");
        variable("range", "A1:D10");
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .post("/v4/spreadsheets/${spreadsheetId}/values/${sheet}!${range}:clear")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"${spreadsheetId}\", \"clearedRange\": \"${sheet}!${range}\"}");
    }

    /**
     * Sends unsupported request to server.
     */
    @Test
    @CitrusTest
    public void testUnsupportedOperation() {
        variable("accessToken", accessToken);

        http().client(simulatorClient)
                .send()
                .post("/v4/spreadsheets/something/completely/different")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .messageType(MessageType.PLAINTEXT)
                .payload("Unsupported operation on path '/v4/spreadsheets/something/completely/different'");
    }
}
