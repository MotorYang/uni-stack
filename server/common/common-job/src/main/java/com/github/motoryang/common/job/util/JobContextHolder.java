package com.github.motoryang.common.job.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * XXL-JOB 上下文辅助工具类
 */
public class JobContextHolder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JobContextHolder.class);

    /**
     * 获取字符串类型的任务参数
     * @return 参数 Optional
     */
    public static Optional<String> getParam() {
        String param = XxlJobHelper.getJobParam();
        return StringUtils.hasText(param) ? Optional.empty() : Optional.of(param);
    }

    /**
     * 将任务参数自动转换为指定的对象（支持 Record 或 POJO）
     * @param clazz 目标类
     * @param <T>   泛型
     * @return  转换后的对象 Optional
     */
    public static <T> Optional<T> getParamAs(Class<T> clazz) {
        return getParam().flatMap(json -> {
            try {
                return Optional.ofNullable(OBJECT_MAPPER.readValue(json, clazz));
            } catch (JsonProcessingException e) {
                log.error("XXL-JOB 参数 JSON 解析失败 | 目标类型: {} | 原始数据: {} | 原因: {}",
                        clazz.getSimpleName(), json, e.getMessage());
                return Optional.empty();
            }
        });
    }

    /**
     * 格式化日志并打印到调度中心与本地控制台
     */
    public static void log(String pattern, Object... args) {
        String message = (args == null || args.length == 0)
                ? pattern
                : String.format(pattern.replace("{}", "%s"), args);
        XxlJobHelper.log(message);
        log.info("[XXL-JOB] {}", message);
    }

    /**
     * 获取分片信息
     * @return 分片信息
     */
    public static ShardInfo getShardInfo() {
        return new ShardInfo(XxlJobHelper.getShardIndex(), XxlJobHelper.getShardTotal());
    }

    /**
     * 分片信息
     * @param index 分片索引
     * @param total 分片总数
     */
    public record ShardInfo(int index, int total) {}
}
