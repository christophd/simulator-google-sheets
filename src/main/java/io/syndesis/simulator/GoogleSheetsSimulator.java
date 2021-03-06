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

package io.syndesis.simulator;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.StaticEndpointAdapter;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.http.SimulatorRestAdapter;
import com.consol.citrus.simulator.http.SimulatorRestConfigurationProperties;
import com.consol.citrus.simulator.scenario.mapper.ContentBasedJsonPathScenarioMapper;
import com.consol.citrus.simulator.scenario.mapper.ScenarioMapper;
import io.syndesis.simulator.sheets.mapper.GoogleSheetsScenarioMapperChain;
import io.syndesis.simulator.sheets.mapper.SpreadsheetIdScenarioMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
@SpringBootApplication
public class GoogleSheetsSimulator extends SimulatorRestAdapter {

    public static void main(String[] args) {
        SpringApplication.run(GoogleSheetsSimulator.class, args);
    }

    @Override
    public String urlMapping(SimulatorRestConfigurationProperties simulatorRestConfiguration) {
        return "/v4/spreadsheets/**";
    }

    @Override
    public ScenarioMapper scenarioMapper() {
        return new GoogleSheetsScenarioMapperChain(new SpreadsheetIdScenarioMapper(),
                                                   new ContentBasedJsonPathScenarioMapper().addJsonPathExpression("$.properties.title"));
    }

    @Override
    public EndpointAdapter fallbackEndpointAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
}
