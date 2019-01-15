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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.scenario.AbstractSimulatorScenario;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.ScenarioDesigner;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.syndesis.simulator.util.VariableHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Christoph Deppisch
 */
@Scenario("UpdateSheetValues")
@RequestMapping(value = "/v4/spreadsheets/{spreadsheetId}/values/{range}", method = RequestMethod.PUT)
public class UpdateSheetValues extends AbstractSimulatorScenario {

    @Override
    public void run(ScenarioDesigner scenario) {
        scenario
            .http()
            .receive()
            .put()
            .validationCallback(new UpdateRequestHandler());

        scenario
            .http()
            .send()
            .response(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .payload(new ClassPathResource("templates/updateValuesResponse.json"));
    }

    public static class UpdateRequestHandler implements ValidationCallback {
        private JsonObjectParser jsonObjectParser = new JsonObjectParser.Builder(new JacksonFactory()).build();

        @Override
        public void validate(Message message, TestContext context) {
            VariableHelper.createVariablesFromUri(message, context);

            try {
                ValueRange valueRange = jsonObjectParser.parseAndClose(new StringReader(message.getPayload(String.class)), ValueRange.class);
                List<List<Object>> values = Optional.ofNullable(valueRange.getValues()).orElse(Collections.emptyList());

                context.setVariable("updatedRows", values.size());
                context.setVariable("updatedColumns", Optional.ofNullable(values.get(0)).map(Collection::size).orElse(0));
                context.setVariable("updatedCells", values.size() * Optional.ofNullable(values.get(0)).map(Collection::size).orElse(0));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to parse value range", e);
            }
        }
    }
}
