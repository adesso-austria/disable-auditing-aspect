package com.adesso.alfresco.batchimport;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DisableAuditingRequestDto {
    final List<String> paths;
    final boolean disable;

    private DisableAuditingRequestDto(final List<String> paths, final boolean disable) {
        this.paths = paths;
        this.disable = disable;
    }
    
    @JsonCreator
    public static DisableAuditingRequestDto of(
        @JsonProperty("paths") final List<String> paths,
        @JsonProperty("disable") final boolean disable
    ) {
        return new DisableAuditingRequestDto(paths, disable);
    }

    // Testing
    public List<String> getPaths() {
        return paths;
    }
    public boolean getDisable() {
        return disable;
    }
}