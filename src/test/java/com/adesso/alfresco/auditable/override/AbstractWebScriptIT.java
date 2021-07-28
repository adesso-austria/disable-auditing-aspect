package com.adesso.alfresco.auditable.override;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.format.DateTimeFormatter;

public abstract class AbstractWebScriptIT extends AbstractAlfrescoIT {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Log log;

    public AbstractWebScriptIT(final Log log) {
        this.log = log;
    }

    public static String getPlatformEndpoint() {
        final String platformEndpoint = System.getProperty("acs.endpoint.path");
        return StringUtils.isNotBlank(platformEndpoint) ? platformEndpoint : "http://localhost:8080/alfresco";
    }
}
