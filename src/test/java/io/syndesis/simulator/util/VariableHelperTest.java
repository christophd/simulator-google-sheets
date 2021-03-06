package io.syndesis.simulator.util;

import java.util.UUID;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.DefaultMessage;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class VariableHelperTest {

    @Test
    public void testSpreadsheetUri() {
        String spreadsheetId = UUID.randomUUID().toString();
        Assert.assertEquals("", VariableHelper.extractSpreadsheetIdFromUri(""));
        Assert.assertEquals("", VariableHelper.extractSpreadsheetIdFromUri("/v4/spreadsheets"));
        Assert.assertEquals("", VariableHelper.extractSpreadsheetIdFromUri("/v4/spreadsheets/"));
        Assert.assertEquals(spreadsheetId, VariableHelper.extractSpreadsheetIdFromUri("/v4/spreadsheets/" + spreadsheetId));
        Assert.assertEquals(spreadsheetId, VariableHelper.extractSpreadsheetIdFromUri(String.format("/v4/spreadsheets/%s/values/A1", spreadsheetId)));
        Assert.assertEquals(spreadsheetId, VariableHelper.extractSpreadsheetIdFromUri(String.format("/v4/spreadsheets/%s/values/A1!B2", spreadsheetId)));
        Assert.assertEquals(spreadsheetId, VariableHelper.extractSpreadsheetIdFromUri(String.format("/v4/spreadsheets/%s/values/A1!C4:clear", spreadsheetId)));
        Assert.assertEquals(spreadsheetId, VariableHelper.extractSpreadsheetIdFromUri(String.format("/v4/spreadsheets/%s/values/A1!A10:append", spreadsheetId)));
    }

    @Test
    public void testEmptyRequestUri() {
        TestContext context = new TestContext();
        VariableHelper.createVariablesFromUri(new DefaultMessage(), context);
        Assert.assertFalse(context.getVariables().containsKey("spreadsheetId"));
    }

    @Test
    public void testEmptyQueryParams() {
        TestContext context = new TestContext();

        VariableHelper.createVariablesFromQueryParams(new HttpMessage(), context);

        Assert.assertFalse(context.getVariables().containsKey("spreadsheetId"));
        Assert.assertFalse(context.getVariables().containsKey("sheet"));
        Assert.assertFalse(context.getVariables().containsKey("range"));
    }

    @Test
    public void testQueryParams() {
        String spreadsheetId = UUID.randomUUID().toString();
        String sheet = "TestSheet";
        String range = "A1:D10";

        TestContext context = new TestContext();

        VariableHelper.createVariablesFromQueryParams(new HttpMessage()
                                                        .queryParam("spreadsheetId", spreadsheetId)
                                                        .queryParam("majorDimension", "COLUMNS")
                                                        .queryParam("range", sheet + "!" + range), context);

        Assert.assertEquals(spreadsheetId, context.getVariable("spreadsheetId"));
        Assert.assertEquals(sheet, context.getVariable("sheet"));
        Assert.assertEquals(range, context.getVariable("range"));
        Assert.assertEquals("COLUMNS", context.getVariable("majorDimension"));
    }

    @Test
    public void testUpdateValuesUriNoSheet() {
        String spreadsheetId = UUID.randomUUID().toString();
        String range = "A1:D10";

        String uri = String.format("/v4/spreadsheets/%s/values/%s", spreadsheetId, range);

        TestContext context = new TestContext();

        VariableHelper.createVariablesFromUri(new DefaultMessage().setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, uri), context);
        Assert.assertEquals(spreadsheetId, context.getVariable("spreadsheetId"));
        Assert.assertEquals("Sheet1", context.getVariable("sheet"));
        Assert.assertEquals(range, context.getVariable("range"));
    }

    @Test
    public void testUpdateValuesUri() {
        String spreadsheetId = UUID.randomUUID().toString();
        String sheet = "TestSheet";
        String range = "A1:D10";

        String uri = String.format("/v4/spreadsheets/%s/values/%s!%s", spreadsheetId, sheet, range);

        TestContext context = new TestContext();

        VariableHelper.createVariablesFromUri(new DefaultMessage().setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, uri), context);
        Assert.assertEquals(spreadsheetId, context.getVariable("spreadsheetId"));
        Assert.assertEquals(sheet, context.getVariable("sheet"));
        Assert.assertEquals(range, context.getVariable("range"));
    }

    @Test
    public void testAppendValuesUri() {
        String spreadsheetId = UUID.randomUUID().toString();
        String sheet = "TestSheet";
        String range = "A1:D10";

        String uri = String.format("/v4/spreadsheets/%s/values/%s!%s:append", spreadsheetId, sheet, range);

        TestContext context = new TestContext();

        VariableHelper.createVariablesFromUri(new DefaultMessage().setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, uri), context);
        Assert.assertEquals(spreadsheetId, context.getVariable("spreadsheetId"));
        Assert.assertEquals(sheet, context.getVariable("sheet"));
        Assert.assertEquals(range, context.getVariable("range"));
    }

    @Test
    public void testClearValuesUri() {
        String spreadsheetId = UUID.randomUUID().toString();
        String sheet = "TestSheet";
        String range = "A1:D10";

        String uri = String.format("/v4/spreadsheets/%s/values/%s!%s:clear", spreadsheetId, sheet, range);

        TestContext context = new TestContext();

        VariableHelper.createVariablesFromUri(new DefaultMessage().setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, uri), context);
        Assert.assertEquals(spreadsheetId, context.getVariable("spreadsheetId"));
        Assert.assertEquals(sheet, context.getVariable("sheet"));
        Assert.assertEquals(range, context.getVariable("range"));
    }
}