package com.adesso.alfresco.auditable.override;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.transaction.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(value = AlfrescoTestRunner.class)
public class AuditableOverrideWebScriptIT extends AbstractWebScriptIT {

    private static final Log log = LogFactory.getLog(AuditableOverrideWebScriptIT.class);
    private static final StoreRef workspaceSpacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    private static final String testFolderName = "folder1";
    private static final String testDocumentName = "doc1.txt";

    private TransactionService transactionService;

    private NodeRef testFolderNodeRef;
    private NodeRef testDocumentNodeRef;

    public AuditableOverrideWebScriptIT() {
        super(log);
    }

    @Before
    public void setup() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        final NodeRef rootNodeRef = getServiceRegistry().getNodeService().getRootNode(workspaceSpacesStore);
        assertNotNull(rootNodeRef);

        this.transactionService = getApplicationContext().getBean(TransactionService.class);
        assertNotNull(this.transactionService);

        final UserTransaction userTransaction = this.transactionService.getNonPropagatingUserTransaction();
        userTransaction.begin();

        try {
            final List<NodeRef> nodes = getServiceRegistry().getSearchService().selectNodes(rootNodeRef, "/app:company_home", new QueryParameterDefinition[0], getServiceRegistry().getNamespaceService(), true);
            assertEquals(1, nodes.size());

            final NodeRef companyHomeNodeRef = nodes.get(0);

            final FileInfo testFolderFileInfo = getServiceRegistry().getFileFolderService().create(companyHomeNodeRef, testFolderName, ContentModel.TYPE_FOLDER);
            this.testFolderNodeRef = testFolderFileInfo.getNodeRef();
            assertNotNull(this.testFolderNodeRef);

            final FileInfo testDocumentFileInfo = getServiceRegistry().getFileFolderService().create(this.testFolderNodeRef, testDocumentName, ContentModel.TYPE_CONTENT);
            this.testDocumentNodeRef = testDocumentFileInfo.getNodeRef();
            assertNotNull(this.testDocumentNodeRef);

            final ContentWriter contentWriter1 = getServiceRegistry().getContentService().getWriter(this.testDocumentNodeRef, ContentModel.PROP_CONTENT, false);
            contentWriter1.setEncoding("UTF-8");
            contentWriter1.setMimetype("text/plain");
            contentWriter1.putContent("Test Document");
        } finally {
            userTransaction.commit();
        }
    }

    @Test
    public void test() throws Exception {
        final int year = 2000;
        final Date newCreatedDate = Date.from(LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());

        final AuditableOverrideDTO overrideFolderCreated = AuditableOverrideDTO.of(
                UUID.fromString(testFolderNodeRef.getId()),
                Collections.singletonMap("cm:created", DATE_TIME_FORMATTER.format(newCreatedDate.toInstant().atOffset(ZoneOffset.UTC)))
        );

        final AuditableOverrideDTO overrideDocumentCreated = AuditableOverrideDTO.of(
                UUID.fromString(testDocumentNodeRef.getId()),
                Collections.singletonMap("{http://www.alfresco.org/model/content/1.0}created", DATE_TIME_FORMATTER.format(newCreatedDate.toInstant().atOffset(ZoneOffset.UTC)))
        );

        HttpResponse httpResponse = callWebScript(new AuditableOverrideDTO[]{overrideFolderCreated, overrideDocumentCreated});

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            log.error(httpResponse.getEntity());
        }

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());

        final UserTransaction userTransaction = this.transactionService.getNonPropagatingUserTransaction(true);
        userTransaction.begin();

        try {
            // test folder1 property cm:created can be updated
            final Date testFolderCreated = (Date) getServiceRegistry().getNodeService().getProperty(testFolderNodeRef, ContentModel.PROP_CREATED);
            assertEquals(year - 1900, testFolderCreated.getYear());

            // test doc1.txt property cm:created can be updated
            final Date testDocumentCreated = (Date) getServiceRegistry().getNodeService().getProperty(testDocumentNodeRef, ContentModel.PROP_CREATED);
            assertEquals(year - 1900, testDocumentCreated.getYear());
        } finally {
            userTransaction.commit();
        }
    }

    @After
    public void teardown() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        final UserTransaction userTransaction = this.transactionService.getNonPropagatingUserTransaction();
        userTransaction.begin();

        getServiceRegistry().getFileFolderService().delete(this.testDocumentNodeRef);
        getServiceRegistry().getFileFolderService().delete(this.testFolderNodeRef);

        userTransaction.commit();
    }

    private HttpResponse callWebScript(final AuditableOverrideDTO[] payload) throws Exception {
        final String webscriptURL = getPlatformEndpoint() + "/s/auditable/override";

        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

        try (final CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()) {
            final HttpPut putRequest = new HttpPut(webscriptURL);

            if (payload != null) {
                putRequest.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(payload), ContentType.APPLICATION_JSON));
            }

            return httpclient.execute(putRequest);
        }
    }
}
