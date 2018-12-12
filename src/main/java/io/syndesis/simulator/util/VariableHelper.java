package io.syndesis.simulator.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.Message;

/**
 * @author Christoph Deppisch
 */
public final class VariableHelper {

    /**
     * Prevent instantiation.
     */
    private VariableHelper() {
        super();
    }

    /**
     * Extract spreadsheet id from URI path parameter to test variables.
     * @param message
     * @param testContext
     */
    public static void extractSpreadsheetIdFromUri(Message message, TestContext testContext) {
        String requestUri = Optional.ofNullable(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                .map(Object::toString)
                .orElse("");

        Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/(\\S+).*").matcher(requestUri);

        if (requestMatcher.find()) {
            testContext.setVariable("spreadsheetId", requestMatcher.group(1));
        }
    }

    /**
     * Extract URI path parameter to test variables.
     * @param message
     * @param testContext
     */
    public static void extractVariablesFromValueUri(Message message, TestContext testContext) {
        String requestUri = Optional.ofNullable(message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                .map(Object::toString)
                .orElse("");

        Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/(\\S+)/values/(\\S+)!(\\S+)").matcher(requestUri);

        if (requestMatcher.find()) {
            testContext.setVariable("spreadsheetId", requestMatcher.group(1));
            testContext.setVariable("sheet", requestMatcher.group(2));
            testContext.setVariable("range", requestMatcher.group(3));
        }
    }
}
