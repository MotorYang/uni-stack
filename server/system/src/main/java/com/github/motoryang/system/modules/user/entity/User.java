package com.github.motoryang.system.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.motoryang.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别（0未知 1男 2女）
     */
    private Integer gender;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 主职部门ID（非数据库字段，通过关联表查询）
     */
    @TableField(exist = false)
    private String deptId;

    /**
     * 主职部门名称（非数据库字段）
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 主职部门职务（非数据库字段）
     */
    @TableField(exist = false)
    private String position;
}
