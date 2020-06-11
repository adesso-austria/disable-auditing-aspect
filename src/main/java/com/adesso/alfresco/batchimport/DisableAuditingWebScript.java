
package com.adesso.alfresco.batchimport;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class DisableAuditingWebScript extends AbstractWebScript {
    private static Log logger = LogFactory.getLog(DisableAuditingWebScript.class);
    private static StoreRef rootRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    private static QName AUDITABLE_ASPECT = ContentModel.ASPECT_AUDITABLE;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    private SearchService searchService;
    private BehaviourFilter policyBehaviourFilter;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final DisableAuditingRequestDto requestDto = this.mapper.readValue(req.getContent().getInputStream(), DisableAuditingRequestDto.class);
        logger.info("Serving request " + requestDto);
        
        ResultSet result = searchService.query(
            rootRef, 
            SearchService.LANGUAGE_CMIS_STRICT,
            "select cmis:objectId from cmis:document where contains (" + toPathQuery(requestDto.paths) + ")"
        );
        
        if(requestDto.disable) {
            result.getNodeRefs().forEach(ref -> disableAspect(ref));
        } else {
            result.getNodeRefs().forEach(ref -> enableAspect(ref));
        }
        
        res.setStatus(204);
    }

    public void setSearchService(SearchService service) {
        this.searchService = service;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter filter) {
        this.policyBehaviourFilter = filter;
    }

    private void disableAspect(NodeRef ref) {
        policyBehaviourFilter.disableBehaviour(ref, AUDITABLE_ASPECT);
    }

    private void enableAspect(NodeRef ref) {
        policyBehaviourFilter.enableBehaviour(ref, AUDITABLE_ASPECT);
    }

    private String toPathQuery(List<String> paths) {
        return paths.stream().map(it -> "'PATH:\"" + it + "\"'").collect(Collectors.joining(","));
    }
}