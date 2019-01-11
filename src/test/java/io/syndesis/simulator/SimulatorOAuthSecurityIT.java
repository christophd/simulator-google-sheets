package io.syndesis.simulator;

import java.util.Optional;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.junit.JUnit4CitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.server.HttpServer;
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
public class SimulatorOAuthSecurityIT extends JUnit4CitrusTestDesigner {

    @Autowired
    private HttpClient simulatorClient;

    @Autowired
    private HttpServer appServer;

    @Value("${simulator.oauth2.client.accessToken}")
    private String accessToken;

    private final String clientId = "syndesis-client";
    private final String clientSecret = "syndesis";

    @Test
    @CitrusTest
    public void testMissingAccessToken() {
        variable("spreadsheetId", "citrus:randomString(44)");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @CitrusTest
    public void testInvalidAccessToken() {
        variable("spreadsheetId", "citrus:randomString(44)");
        variable("accessToken", "someExpiredToken");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @CitrusTest
    public void testClientCredentialsGrantType() {
        variable("id", clientId);
        variable("secret", clientSecret);

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=client_credentials&client_id=${id}&client_secret=${secret}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                            "\"access_token\": \"@variable(accessToken)@\"," +
                            "\"token_type\": \"bearer\"," +
                            "\"expires_in\": \"@greaterThan(2000)@\"," +
                            "\"scope\": \"read write trust\"" +
                        "}");

        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestData");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    @Test
    @CitrusTest
    public void testPasswordGrantType() {
        variable("id", clientId);
        variable("secret", clientSecret);

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=password&username=${id}&password=${secret}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                            "\"access_token\": \"@variable(accessToken)@\"," +
                            "\"token_type\": \"bearer\"," +
                            "\"expires_in\": \"@greaterThan(2000)@\"," +
                            "\"refresh_token\": \"@variable(refreshToken)@\"," +
                            "\"scope\": \"read write trust\"" +
                        "}");

        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestData");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    @Test
    @CitrusTest
    public void testClientCredentialsError() {
        variable("id", "wrong-client");
        variable("secret", "wrong");

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=client_credentials&client_id=${id}&client_secret=${secret}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @CitrusTest
    public void testRefreshTokenGrantType() {
        variable("id", clientId);
        variable("secret", clientSecret);
        variable("refreshToken", "bdbbe5ec-6081-4c6c-8974-9c4abfc0fdcc");

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=refresh_token&client_id=${id}&client_secret=${secret}&refresh_token=${refreshToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                        "\"access_token\": \"@variable(accessToken)@\"," +
                        "\"token_type\": \"bearer\"," +
                        "\"refresh_token\": \"${refreshToken}\"," +
                        "\"expires_in\": \"@greaterThan(2000)@\"" +
                        "}");

        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestData");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    @Test
    @CitrusTest
    public void testRefreshTokenError() {
        variable("id", clientId);
        variable("secret", clientSecret);
        variable("refreshToken", "someExpiredToken");

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=refresh_token&client_id=${id}&client_secret=${secret}&refresh_token=${refreshToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.BAD_REQUEST)
                .payload("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid refresh token: ${refreshToken}\"}");
    }

    @Test
    @CitrusTest
    public void testAuthorizationCodeResponseTypeCode() {
        variable("id", clientId);
        variable("secret", clientSecret);

        http().client(simulatorClient)
                .send()
                .get("/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "${id}")
                .queryParam("redirect_uri", "http://localhost:8081/testapp")
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})");

        http().server(appServer)
                .receive()
                .get("/testapp")
                .validationCallback((message, testContext) -> {
                    String queryParamString = Optional.ofNullable(message.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS))
                                                      .map(Object::toString)
                                                      .orElse("");

                    testContext.setVariable("authorization_code", queryParamString.substring("code=".length()));
                });

        http().server(appServer)
                .respond(HttpStatus.OK);

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK);

        http().client(simulatorClient)
                .send()
                .post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("grant_type=authorization_code&client_id=${id}&code=${authorization_code}&redirect_uri=http://localhost:8081/testapp");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                            "\"access_token\": \"@variable(accessToken)@\"," +
                            "\"token_type\": \"bearer\"," +
                            "\"expires_in\": \"@greaterThan(2000)@\"," +
                            "\"refresh_token\": \"@variable(refreshToken)@\"," +
                            "\"scope\": \"read write trust\"" +
                        "}");

        variable("spreadsheetId", "citrus:randomString(44)");
        variable("title", "TestData");

        http().client(simulatorClient)
                .send()
                .get("/v4/spreadsheets/${spreadsheetId}")
                .header("Authorization", "Bearer ${accessToken}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("templates/spreadsheet.json"));
    }

    @Test
    @CitrusTest
    public void testCheckToken() {
        variable("id", clientId);
        variable("secret", clientSecret);
        variable("token", accessToken);

        http().client(simulatorClient)
                .send()
                .post("/oauth/check_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Basic citrus:encodeBase64(${id}:${secret})")
                .payload("token=${token}");

        http().client(simulatorClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{\"authorities\":[\"ROLE_TRUSTED_CLIENT\",\"ROLE_CLIENT\"],\"client_id\":\"${id}\"}");
    }

}
