package com.github.motoryang.system.modules.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源组实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_resource_group")
public class ResourceGroup extends BaseEntity {

    /**
     * 资源组名称
     */
    private String resGroupName;

    /**
     * 资源组编码
     */
    private String resGroupCode;

    /**
     * 资源组描述
     */
    private String description;

    /**
     * 所属微服务标识（对应 spring.application.name）
     */
    private String serviceName;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;
}
