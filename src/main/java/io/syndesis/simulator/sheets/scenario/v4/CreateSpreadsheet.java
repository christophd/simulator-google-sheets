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

package io.syndesis.simulator.sheets.scenario.v4;

import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;
import com.consol.citrus.simulator.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Christoph Deppisch
 */
@Scenario("CreateSpreadsheet")
@RequestMapping(value = "/v4/spreadsheets", method = RequestMethod.POST)
public class CreateSpreadsheet extends AbstractSimulatorScenario {

    private final TemplateService templates;

    @Autowired
    public CreateSpreadsheet(TemplateService templates) {
        this.templates = templates;
    }

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario
            .http()
            .receive()
            .post()
            .extractFromPayload("$.properties.title", "title");

        scenario.echo("Received new spreadsheet: ${title}");

        scenario.createVariable("spreadsheetId", "citrus:randomString(44)");

        scenario
            .http()
            .send()
            .response(HttpStatus.OK)
            .payload(templates.getJsonMessageTemplate("spreadsheet"));
    }
}
