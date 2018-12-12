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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SimulatorClientConfig.class)
public class GoogleSheetsSimulatorIT extends JUnit4CitrusTestDesigner {

    @Autowired
    private HttpClient sheetsClient;

    /**
     * Sends get spreadsheet request to server expecting positive response message.
     */
    @Test
    @CitrusTest
    public void testGetSpreadsheet() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestData");

        http().client(sheetsClient)
                .send()
                .get("/${spreadsheetId}");

        http().client(sheetsClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    /**
     * Sends create spreadsheet request to server expecting positive response message.
     */
    @Test
    @CitrusTest
    public void testCreateSpreadsheet() {
        variable("title", "TestData");

        http().client(sheetsClient)
                .send()
                .post()
                .payload("{\"properties\": {\"title\": \"${title}\"}}");

        http().client(sheetsClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"spreadsheetId\": \"@ignore@\", \"properties\": {\"title\": \"${title}\"}}");
    }

    /**
     * Sends get sheet values request to server expecting positive response message.
     */
    @Test
    @CitrusTest
    public void testGetValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestData");
        variable("range", "A1:C5");

        http().client(sheetsClient)
                .send()
                .get("/${spreadsheetId}/values/${sheet}!${range}");

        http().client(sheetsClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"range\": \"${sheet}!${range}\",\"majorDimension\": \"ROWS\",\"values\": []}");
    }

    /**
     * Sends update sheet values request to server expecting positive response message.
     */
    @Test
    @CitrusTest
    public void testUpdateValues() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("sheet", "TestData");
        variable("range", "A1:C5");

        variable("updatedRows", 2);
        variable("updatedColumns", 2);
        variable("updatedCells", 4);

        http().client(sheetsClient)
                .send()
                .put("/${spreadsheetId}/values/${sheet}!${range}")
                .payload("{\"values\":[[\"A1\",\"B1\"],[\"A2\",\"B2\"]]}");

        http().client(sheetsClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/updateValuesResponse.json"));
    }
}
