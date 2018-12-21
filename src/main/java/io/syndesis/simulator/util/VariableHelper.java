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

package io.syndesis.simulator.util;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.Message;
import org.springframework.util.StringUtils;

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
     * Extract spreadsheet id from URI path parameter.
     * @param uri the request uri containing path parameters.
     */
    public static String extractSpreadsheetIdFromUri(String uri) {
        Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/([^/]+).*").matcher(uri);

        if (requestMatcher.find()) {
            return requestMatcher.group(1);
        }

        return "";
    }

    /**
     * Extract request parameter to test variables combining request uri path parameters and request query parameters.
     * @param request the message providing the request uri.
     * @param testContext the test context to create variables in.
     */
    public static void createVariablesFromRequest(Message request, TestContext testContext) {
        createVariablesFromQueryParams(request, testContext);
        createVariablesFromUri(request, testContext);
    }

    /**
     * Extract request query parameter to test variables.
     * @param request the message providing the request uri.
     * @param testContext the test context to create variables in.
     */
    public static void createVariablesFromQueryParams(Message request, TestContext testContext) {
        String queryParamString = Optional.ofNullable(request.getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS))
                .map(Object::toString)
                .orElse("");

        Map<String, String> params = Stream.of(queryParamString.split(","))
                .map(keyValue -> Optional.ofNullable(StringUtils.split(keyValue, "=")).orElse(new String[] {keyValue, ""}))
                .filter(keyValue -> StringUtils.hasText(keyValue[0]))
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));

        if (Optional.ofNullable(params.get("range")).orElse("").contains("!")) {
            String[] rangeCoordinate = params.get("range").split("!");
            params.put("sheet", rangeCoordinate[0]);
            params.put("range", rangeCoordinate[1]);
        }

        params.forEach(testContext::setVariable);
    }

    /**
     * Extract URI path parameter to test variables.
     * @param request the message providing the request uri.
     * @param testContext the test context to create variables in.
     */
    public static void createVariablesFromUri(Message request, TestContext testContext) {
        String requestUri = Optional.ofNullable(request.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI))
                .map(Object::toString)
                .orElse("");

        if (requestUri.contains("/values/")) {
            if (requestUri.contains("!")) {
                Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/(\\S+)/values/(\\S+)!(\\S+)").matcher(requestUri);

                if (requestMatcher.find()) {
                    testContext.setVariable("spreadsheetId", requestMatcher.group(1));
                    testContext.setVariable("sheet", requestMatcher.group(2));

                    String range = requestMatcher.group(3);

                    testContext.setVariable("range", Stream.of(":clear", ":append")
                            .filter(range::endsWith)
                            .map(suffix -> range.substring(0, range.length() - suffix.length()))
                            .findAny()
                            .orElse(range));
                }
            } else {
                Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/(\\S+)/values/(\\S+)").matcher(requestUri);

                if (requestMatcher.find()) {
                    testContext.setVariable("spreadsheetId", requestMatcher.group(1));

                    String range = requestMatcher.group(2);

                    testContext.setVariable("range", Stream.of(":clear", ":append")
                            .filter(range::endsWith)
                            .map(suffix -> range.substring(0, range.length() - suffix.length()))
                            .findAny()
                            .orElse(range));
                }
            }
        } else {
            Matcher requestMatcher = Pattern.compile("/v4/spreadsheets/([^/]+).*").matcher(requestUri);

            if (requestMatcher.find()) {
                testContext.setVariable("spreadsheetId", requestMatcher.group(1));
            }
        }
    }
}
