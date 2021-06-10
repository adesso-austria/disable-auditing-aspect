
package com.adesso.alfresco.auditable.override;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class AuditableOverrideWebScript extends AbstractWebScript {

    private static final Log log = LogFactory.getLog(AuditableOverrideWebScript.class);
    private static final StoreRef workspaceSpacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private static final ObjectMapper mapper = new ObjectMapper();

    private BehaviourFilter policyBehaviourFilter;
    private ServiceRegistry serviceRegistry;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final AuditableOverrideDTO[] requestDto;
        try {
            requestDto = mapper.readValue(req.getContent().getInputStream(), AuditableOverrideDTO[].class);
        } catch (final Exception e) {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.getWriter().append(e.getMessage());
            return;
        }

        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

        for (final AuditableOverrideDTO auditableOverrideDTO : requestDto) {
            final NodeRef nodeRef = new NodeRef(workspaceSpacesStore, auditableOverrideDTO.getNodeId().toString());
            for (final Map.Entry<String, Serializable> property : auditableOverrideDTO.getProperties().entrySet()) {
                switch (property.getKey()) {
                    case "cm:created":
                        serviceRegistry.getNodeService().setProperty(nodeRef, ContentModel.PROP_CREATED, property.getValue());
                        break;
                    case "cm:creator":
                        serviceRegistry.getNodeService().setProperty(nodeRef, ContentModel.PROP_CREATOR, property.getValue());
                        break;
                    case "cm:modified":
                        serviceRegistry.getNodeService().setProperty(nodeRef, ContentModel.PROP_MODIFIED, property.getValue());
                        break;
                    case "cm:modifier":
                        serviceRegistry.getNodeService().setProperty(nodeRef, ContentModel.PROP_MODIFIER, property.getValue());
                        break;
                    case "cm:accessed":
                        serviceRegistry.getNodeService().setProperty(nodeRef, ContentModel.PROP_ACCESSED, property.getValue());
                        break;
                    default:
                        log.warn(String.format("Unrecognized property \"%s\" with value \"%s\", skipped override for NodeRef \"%s\".", property.getKey(), property.getValue(), nodeRef));
                        break;
                }
            }
        }

        res.setStatus(HttpStatus.OK.value());
    }

    public void setPolicyBehaviourFilter(final BehaviourFilter filter) {
        this.policyBehaviourFilter = filter;
    }

    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

}