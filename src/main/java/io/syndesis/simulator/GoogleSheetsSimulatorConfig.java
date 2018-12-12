package io.syndesis.simulator;

import java.util.Collections;

import com.consol.citrus.http.servlet.RequestCachingServletFilter;
import com.consol.citrus.simulator.http.SimulatorRestAutoConfiguration;
import com.consol.citrus.simulator.http.SimulatorRestConfigurationProperties;
import io.syndesis.simulator.filter.GzipServletFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
@AutoConfigureAfter(SimulatorRestAutoConfiguration.class)
public class GoogleSheetsSimulatorConfig {

    @Bean
    public FilterRegistrationBean requestCachingServletFilter(SimulatorRestConfigurationProperties configurationProperties) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new RequestCachingServletFilter());
        filterRegistrationBean.setUrlPatterns(Collections.singleton(configurationProperties.getUrlMapping()));
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean gzipServletFilter(SimulatorRestConfigurationProperties configurationProperties) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new GzipServletFilter());
        filterRegistrationBean.setUrlPatterns(Collections.singleton(configurationProperties.getUrlMapping()));
        return filterRegistrationBean;
    }
}
