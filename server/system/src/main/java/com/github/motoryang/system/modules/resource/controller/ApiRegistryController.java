package com.github.motoryang.system.modules.resource.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.redis.api.ApiInfo;
import com.github.motoryang.common.redis.constants.RedisConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API 注册表控制器
 */
@Tag(name = "API注册表", description = "查询各微服务注册的API")
@RestController
@RequestMapping("/api-registry")
@RequiredArgsConstructor
public class ApiRegistryController {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取所有已注册服务名")
    @GetMapping("/services")
    public RestResult<List<String>> getServices() {
        Set<String> keys = redisTemplate.keys(RedisConstants.REDIS_API_KEY + "*");
        if (keys.isEmpty()) {
            return RestResult.ok(List.of());
        }
        List<String> services = keys.stream()
                .map(k -> k.replace(RedisConstants.REDIS_API_KEY, ""))
                .sorted()
                .toList();
        return RestResult.ok(services);
    }

    @Operation(summary = "获取指定服务的API列表")
    @GetMapping("/apis")
    public RestResult<List<ApiInfo>> getApis(
            @Parameter(description = "服务名称", required = true)
            @RequestParam String serviceName) {
        String key = RedisConstants.REDIS_API_KEY + serviceName;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return RestResult.ok(List.of());
        }
        try {
            List<ApiInfo> apis = objectMapper.readValue(json, new TypeReference<List<ApiInfo>>() {});
            return RestResult.ok(apis);
        } catch (JsonProcessingException e) {
            return RestResult.ok(new ArrayList<>());
        }
    }
}
