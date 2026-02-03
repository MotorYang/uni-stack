package com.github.motoryang.common.web.api;

import java.io.Serializable;

/**
 * API information record
 *
 * @param path        API path, e.g., "/users/{id}"
 * @param method      Request method, e.g., "GET", "POST", "*"
 * @param summary     Summary from @Operation annotation
 * @param description Description from @Operation annotation
 * @param tags        Tags from @Tag annotation
 */
public record ApiInfo(
        String path,
        String method,
        String summary,
        String description,
        String[] tags
) implements Serializable {
}
