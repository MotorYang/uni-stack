package com.github.motoryang.common.redis.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.motoryang.common.redis.constants.RedisConstants;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API 扫描器
 * <p>
 * 在应用启动完成后扫描所有 @RequestMapping 端点，并将其存储到 Redis 中。
 * </p>
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "uni.api.scanner.enabled", havingValue = "true", matchIfMissing = true)
public class ApiScanner implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApiScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    /**
     * 需要排除的路径前缀
     */
    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/actuator",
            "/error",
            "/swagger",
            "/v3/api-docs",
            "/webjars"
    );

    public ApiScanner(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
                      StringRedisTemplate redisTemplate,
                      ObjectMapper objectMapper) {
        log.info("API Scanner: Enabled");
        this.handlerMapping = handlerMapping;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            List<ApiInfo> apis = scanApis();
            String key = RedisConstants.REDIS_API_KEY + serviceName;
            String json = objectMapper.writeValueAsString(apis);
            redisTemplate.opsForValue().set(key, json);
            log.info("API Scanner: Registered {} APIs for service '{}' to Redis", apis.size(), serviceName);
        } catch (JsonProcessingException e) {
            log.error("API Scanner: Failed to serialize APIs to JSON", e);
        } catch (Exception e) {
            log.error("API Scanner: Failed to register APIs to Redis", e);
        }
    }

    /**
     * 扫描所有 API 端点
     */
    private List<ApiInfo> scanApis() {
        List<ApiInfo> apis = new ArrayList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 跳过标记为 @Hidden 的端点
            if (isHidden(handlerMethod)) {
                continue;
            }

            // 获取路径
            Set<PathPattern> patterns = mappingInfo.getPathPatternsCondition() != null
                    ? mappingInfo.getPathPatternsCondition().getPatterns()
                    : Set.of();

            for (PathPattern pattern : patterns) {
                String path = pattern.getPatternString();

                // 跳过排除的路径
                if (isExcludedPath(path)) {
                    continue;
                }

                // 获取请求方法
                Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
                String methodStr = methods.isEmpty() ? "*" :
                        methods.stream().map(Enum::name).collect(Collectors.joining(","));

                // 获取注解信息
                Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
                Tag tag = handlerMethod.getBeanType().getAnnotation(Tag.class);

                String summary = operation != null ? operation.summary() : "";
                String description = operation != null ? operation.description() : "";
                String[] tags = tag != null ? new String[]{tag.name()} : new String[]{};

                apis.add(new ApiInfo(path, methodStr, summary, description, tags));
            }
        }

        return apis;
    }

    /**
     * 检查是否标记为 Hidden
     */
    private boolean isHidden(HandlerMethod handlerMethod) {
        // 检查方法级别
        Hidden methodHidden = handlerMethod.getMethodAnnotation(Hidden.class);
        if (methodHidden != null) {
            return true;
        }
        // 检查类级别
        Hidden classHidden = handlerMethod.getBeanType().getAnnotation(Hidden.class);
        return classHidden != null;
    }

    /**
     * 检查路径是否应该被排除
     */
    private boolean isExcludedPath(String path) {
        for (String prefix : EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
