package com.adesso.alfresco.auditable.override;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AuditableOverrideDTO {

    private final UUID nodeId;
    private final Map<String, Serializable> properties;

    @JsonCreator
    public static AuditableOverrideDTO of(
            @JsonProperty("nodeId") final UUID nodeId,
            @JsonProperty("properties") Map<String, Serializable> properties
    ) {
        return new AuditableOverrideDTO(nodeId, properties);
    }
}