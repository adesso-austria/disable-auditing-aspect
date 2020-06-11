package com.adesso.alfresco.batchimport;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DisableAuditingWebScriptIT {

    private static final String ACS_ENDPOINT_PROP = "acs.endpoint.path";
    private static final String ACS_DEFAULT_ENDPOINT = "http://localhost:8080/alfresco";

    @Test
    @Ignore
    public void testWebScriptCall() throws Exception {
        String webscriptURL = getPlatformEndpoint() + "/service/sample/helloworld";
        String expectedResponse = "Message: 'Hello from JS!' 'HelloFromJava'";

        // Login credentials for Alfresco Repo
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

        // Create HTTP Client with credentials
        CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();

        // Execute Web Script call
        try {
            HttpGet httpget = new HttpGet(webscriptURL);
            HttpResponse httpResponse = httpclient.execute(httpget);
            assertEquals("Incorrect HTTP Response Status",
                    HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
            HttpEntity entity = httpResponse.getEntity();
            assertNotNull("Response from Web Script is null", entity);
            assertEquals("Incorrect Web Script Response", expectedResponse, EntityUtils.toString(entity));
        } finally {
            httpclient.close();
        }
    }

    private String getPlatformEndpoint() {
        final String platformEndpoint = System.getProperty(ACS_ENDPOINT_PROP);
        return StringUtils.isNotBlank(platformEndpoint) ? platformEndpoint : ACS_DEFAULT_ENDPOINT;
    }
}