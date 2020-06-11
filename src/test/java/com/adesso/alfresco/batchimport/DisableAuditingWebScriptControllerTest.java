package com.adesso.alfresco.batchimport;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class DisableAuditingWebScriptControllerTest {

    private ObjectMapper mapper = new ObjectMapper();
    private DisableAuditingWebScript webScript;
    
    private SearchService searchService;
    private BehaviourFilter policyBehaviourFilter;
    
    @Before
    public void setUp() {
        searchService = Mockito.mock(SearchService.class);
        policyBehaviourFilter = Mockito.mock(BehaviourFilter.class);
        webScript = new DisableAuditingWebScript();
        webScript.setSearchService(searchService);
        webScript.setPolicyBehaviourFilter(policyBehaviourFilter);
    }

    @Test
    public void testController() throws IOException {
        // Given
        WebScriptRequest req = Mockito.mock(WebScriptRequest.class, RETURNS_DEEP_STUBS);
        WebScriptResponse res = Mockito.mock(WebScriptResponse.class);
        ResultSet mockResult = Mockito.mock(ResultSet.class);
        
        when(req.getContent().getInputStream()).thenReturn(new ByteArrayInputStream(mapper.writeValueAsBytes(request(true))));
        when(searchService.query(any(StoreRef.class), any(String.class), any(String.class))).thenReturn(mockResult);
        when(mockResult.getNodeRefs()).thenReturn(mockNodeRefs());
        
        //When
        webScript.execute(req, res);

        verify(searchService, times(1)).query(any(StoreRef.class), any(String.class), any(String.class));
    }

    private DisableAuditingRequestDto request(boolean disable) {
        List<String> paths = new ArrayList();
        paths.add("/app:company_home");
        return DisableAuditingRequestDto.of(paths, disable);
    }
    private List<NodeRef> mockNodeRefs() {
        List<NodeRef> nodeRefs = new ArrayList();
        nodeRefs.add(new NodeRef("workspace://SpacesStore/2d0d935d-500c-44f1-81ce-4449e82ae897"));
        return nodeRefs;
    }
}