/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adesso.alfresco.batchimport;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A demonstration Java controller for the Hello World Web Script.
 *
 * @author martin.bergljung@alfresco.com
 * @since 2.1.0
 */
public class DisableAuditingWebScript extends AbstractWebScript {
    private static Log logger = LogFactory.getLog(DisableAuditingWebScript.class);

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        logger.info("Serving request");
        try {
            JSONObject obj = new JSONObject();
            obj.put("name", "Alfresco");
            String jsonString = obj.toString();
            res.getWriter().write(jsonString);
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON");
        }
    }
}