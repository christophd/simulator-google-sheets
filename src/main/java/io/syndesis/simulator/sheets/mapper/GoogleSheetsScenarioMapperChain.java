package io.syndesis.simulator.sheets.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.message.Message;
import com.consol.citrus.simulator.config.SimulatorConfigurationProperties;
import com.consol.citrus.simulator.http.HttpRequestAnnotationScenarioMapper;
import com.consol.citrus.simulator.scenario.Scenario;
import com.consol.citrus.simulator.scenario.SimulatorScenario;
import com.consol.citrus.simulator.scenario.mapper.ScenarioMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * Scenario mapper chain goes through a list of mappers to find best match of extracted mapping keys. When no suitable
 * mapping key is found in the list of mappers a default mapper is used.
 *
 * @author Christoph Deppisch
 */
public class GoogleSheetsScenarioMapperChain implements ScenarioMapper, InitializingBean {

    @Autowired(required = false)
    private List<SimulatorScenario> scenarios = new ArrayList<>();

    @Autowired
    private SimulatorConfigurationProperties configuration;

    private final HttpRequestAnnotationScenarioMapper defaultMapper = new HttpRequestAnnotationScenarioMapper();
    private final List<ScenarioMapper> scenarioMappers;

    /**
     * Constructor using list of scenario mappers to chain when extracting mapping keys.
     * @param scenarioMappers
     */
    public GoogleSheetsScenarioMapperChain(ScenarioMapper ... scenarioMappers) {
        this.scenarioMappers = Arrays.asList(scenarioMappers);
    }

    @Override
    public String extractMappingKey(Message message) {
        return scenarioMappers.stream()
                .map(mapper -> {
                    try {
                        return Optional.ofNullable(mapper.extractMappingKey(message));
                    } catch (Exception e) {
                        return Optional.<String>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(StringUtils::hasLength)
                .filter(key -> scenarios.parallelStream()
                                        .anyMatch(scenario -> Optional.ofNullable(AnnotationUtils.findAnnotation(scenario.getClass(), Scenario.class))
                                                                        .map(Scenario::value)
                                                                        .orElse("")
                                                                        .equals(key)))
                .findFirst()
                .orElse(defaultMapper.extractMappingKey(message));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        defaultMapper.setScenarios(scenarios);
        defaultMapper.setConfiguration(configuration);
    }
}
