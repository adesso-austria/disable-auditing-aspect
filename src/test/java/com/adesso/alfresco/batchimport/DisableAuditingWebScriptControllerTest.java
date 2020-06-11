package com.adesso.alfresco.batchimport;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.extensions.webscripts.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit testing the Web Script Java Controller
 *
 * @author martin.bergljung@alfresco.com
 * @version 1.0
 * @since 3.0
 */
public class DisableAuditingWebScriptControllerTest {

    @Test
    public void testController() throws IOException {
        WebScriptRequest req = Mockito.mock(WebScriptRequest.class);
        WebScriptResponse res = Mockito.mock(WebScriptResponse.class);
        when(res.getWriter()).thenReturn(new OutputStreamWriter(new ByteArrayOutputStream()));
        DisableAuditingWebScript ws = new DisableAuditingWebScript();
        ws.execute(req, res);

        // TODO: add assert
    }
}