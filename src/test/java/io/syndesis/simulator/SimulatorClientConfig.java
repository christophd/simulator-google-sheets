package io.syndesis.simulator;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class SimulatorClientConfig {

    @Bean
    public HttpServer appServer() {
        return CitrusEndpoints.http()
                .server()
                .port(8081)
                .autoStart(true)
                .build();
    }

    @Bean
    public HttpClient simulatorClient() {
        return CitrusEndpoints.http()
                .client()
                .requestUrl("https://localhost:8443")
                .requestFactory(sslRequestFactory())
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public org.apache.http.client.HttpClient httpClient() {
        try {
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(new ClassPathResource("googleapis.jks", GoogleSheetsSimulator.class).getFile(), "secret".toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext, NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }
}
