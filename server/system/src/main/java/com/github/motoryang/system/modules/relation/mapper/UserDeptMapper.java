package com.github.motoryang.system.modules.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.relation.entity.UserDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户部门关联 Mapper
 */
@Mapper
public interface UserDeptMapper extends BaseMapper<UserDept> {

    /**
     * 批量插入用户部门关联
     */
    int insertBatch(@Param("list") List<UserDept> list);

    /**
     * 根据用户ID逻辑删除关联
     */
    @Update("UPDATE sys_user_dept SET deleted = 1 WHERE user_id = #{userId} AND deleted = 0")
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 根据部门ID逻辑删除关联
     */
    @Update("UPDATE sys_user_dept SET deleted = 1 WHERE dept_id = #{deptId} AND deleted = 0")
    int deleteByDeptId(@Param("deptId") String deptId);

    /**
     * 根据用户ID和部门ID逻辑删除关联
     */
    @Update("UPDATE sys_user_dept SET deleted = 1 WHERE user_id = #{userId} AND dept_id = #{deptId} AND deleted = 0")
    int deleteByUserIdAndDeptId(@Param("userId") String userId, @Param("deptId") String deptId);

    /**
     * 批量逻辑删除指定部门下的用户关联
     */
    int deleteBatchByDeptIdAndUserIds(@Param("deptId") String deptId, @Param("userIds") List<String> userIds);

    /**
     * 查询用户的部门ID列表
     */
    @Select("SELECT dept_id FROM sys_user_dept WHERE user_id = #{userId} AND deleted = 0")
    List<String> selectDeptIdsByUserId(@Param("userId") String userId);

    /**
     * 查询用户的主职部门ID
     */
    @Select("SELECT dept_id FROM sys_user_dept WHERE user_id = #{userId} AND is_primary = 1 AND deleted = 0 LIMIT 1")
    String selectPrimaryDeptIdByUserId(@Param("userId") String userId);

    /**
     * 查询部门下的用户ID列表
     */
    @Select("SELECT user_id FROM sys_user_dept WHERE dept_id = #{deptId} AND deleted = 0")
    List<String> selectUserIdsByDeptId(@Param("deptId") String deptId);

    /**
     * 清除用户的所有主职标记
     */
    @Update("UPDATE sys_user_dept SET is_primary = 0 WHERE user_id = #{userId} AND deleted = 0")
    int clearPrimaryByUserId(@Param("userId") String userId);

    /**
     * 设置指定关联为主职
     */
    @Update("UPDATE sys_user_dept SET is_primary = 1 WHERE user_id = #{userId} AND dept_id = #{deptId} AND deleted = 0")
    int setPrimary(@Param("userId") String userId, @Param("deptId") String deptId);

    /**
     * 更新职务
     */
    @Update("UPDATE sys_user_dept SET position = #{position} WHERE user_id = #{userId} AND dept_id = #{deptId} AND deleted = 0")
    int updatePosition(@Param("userId") String userId, @Param("deptId") String deptId, @Param("position") String position);

    /**
     * 查询用户在指定部门的关联信息
     */
    @Select("SELECT * FROM sys_user_dept WHERE user_id = #{userId} AND dept_id = #{deptId} AND deleted = 0")
    UserDept selectByUserIdAndDeptId(@Param("userId") String userId, @Param("deptId") String deptId);

    /**
     * 查询用户的所有部门关联
     */
    @Select("SELECT * FROM sys_user_dept WHERE user_id = #{userId} AND deleted = 0")
    List<UserDept> selectByUserId(@Param("userId") String userId);
}
