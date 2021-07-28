
package com.adesso.alfresco.auditable.override;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AuditableOverrideWebScript extends AbstractWebScript {

    private static final Log log = LogFactory.getLog(AuditableOverrideWebScript.class);
    private static final StoreRef workspaceSpacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private static final ObjectMapper mapper = new ObjectMapper();

    private Set<QName> auditableAspectProperties;
    private BehaviourFilter policyBehaviourFilter;
    private ServiceRegistry serviceRegistry;

    @PostConstruct
    public void init() {
        auditableAspectProperties = serviceRegistry.getDictionaryService().getAspect(ContentModel.ASPECT_AUDITABLE).getProperties().keySet();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        final AuditableOverrideDTO[] requestDto;
        try {
            requestDto = mapper.readValue(req.getContent().getInputStream(), AuditableOverrideDTO[].class);
        } catch (final Exception e) {
            log.error(e);
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.getWriter().append(e.getMessage());
            return;
        }

        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

        for (final AuditableOverrideDTO auditableOverrideDTO : requestDto) {
            final NodeRef nodeRef = new NodeRef(workspaceSpacesStore, auditableOverrideDTO.getNodeId().toString());
            final Map<QName, Serializable> properties = new HashMap<>();
            for (final Map.Entry<String, Serializable> dtoProperties : auditableOverrideDTO.getProperties().entrySet()) {
                try {
                    final QName propertyQName = QName.resolveToQName(serviceRegistry.getNamespaceService(), dtoProperties.getKey());
                    if (auditableAspectProperties.contains(propertyQName)) {
                        properties.put(propertyQName, dtoProperties.getValue());
                        log.info(String.format("Overriding property \"%s\" of NodeRef \"%s\" with value \"%s\".", propertyQName.toPrefixString(serviceRegistry.getNamespaceService()), nodeRef, dtoProperties.getValue()));
                    } else {
                        log.warn(String.format("Property \"%s\" is no property of the \"cm:auditable\"-Aspect, skipped override for NodeRef \"%s\".", propertyQName.toPrefixString(serviceRegistry.getNamespaceService()), nodeRef));
                    }
                } catch (final InvalidQNameException e) {
                    log.warn(String.format("Property \"%s\" (with value \"%s\") is unknown, skipped override for NodeRef \"%s\".", dtoProperties.getKey(), dtoProperties.getValue(), nodeRef));
                }
            }
            if (!properties.isEmpty()) {
                serviceRegistry.getNodeService().setProperties(nodeRef, properties);
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