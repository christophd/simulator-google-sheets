package io.syndesis.simulator.sheets.mapper;

import java.util.Optional;

import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.scenario.mapper.ScenarioMapper;
import io.syndesis.simulator.util.VariableHelper;

/**
 * @author Christoph Deppisch
 */
public class SpreadsheetIdScenarioMapper implements ScenarioMapper {

    @Override
    public String extractMappingKey(Message message) {
        return VariableHelper.extractSpreadsheetIdFromUri(Optional.ofNullable(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                                                                    .map(Object::toString)
                                                                    .orElse(""));
    }
}
