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

package io.syndesis.simulator.sheets.starter;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.simulator.model.ScenarioParameter;
import com.consol.citrus.simulator.model.ScenarioParameterBuilder;
import com.consol.citrus.simulator.scenario.AbstractScenarioStarter;
import com.consol.citrus.simulator.scenario.ScenarioRunner;
import com.consol.citrus.simulator.scenario.Starter;
import com.consol.citrus.simulator.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Christoph Deppisch
 */
@Starter("SyndesisStarter")
public class SyndesisStarter extends AbstractScenarioStarter {

    private final TemplateService templateService;

    @Autowired
    public SyndesisStarter(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void run(ScenarioRunner scenario) {
        scenario.echo("${title}");
        scenario.echo("${payload}");
    }

    @Override
    public List<ScenarioParameter> getScenarioParameters() {
        List<ScenarioParameter> scenarioParameter = new ArrayList<>();

        // name (text box)
        scenarioParameter.add(new ScenarioParameterBuilder()
                .name("title")
                .label("Title")
                .required()
                .textbox()
                .value("TestSheet")
                .build());

        // greeting (text area)
        scenarioParameter.add(new ScenarioParameterBuilder()
                .name("payload")
                .label("Payload")
                .required()
                .textarea()
                .value(templateService.getJsonMessageTemplate("spreadsheet"))
                .build());

        return scenarioParameter;
    }
}
