package com.github.motoryang.system.modules.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource")
public class Resource extends BaseEntity {

    /**
     * 资源组ID
     */
    private String groupId;

    /**
     * 资源名称
     */
    private String resName;

    /**
     * 资源类型（API、BUTTON）
     */
    private String resType;

    /**
     * 资源路径（API路径或组件ID）
     */
    private String resPath;

    /**
     * 资源编码
     */
    private String resCode;

    /**
     * 请求方式（GET/POST/PUT/DELETE/*）
     */
    private String resMethod;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
}
