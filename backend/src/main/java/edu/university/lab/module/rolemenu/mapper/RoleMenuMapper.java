package edu.university.lab.module.rolemenu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.lab.module.rolemenu.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Delete("DELETE FROM role_menu WHERE role_id = #{roleId}")
    int deleteForceByRoleId(@Param("roleId") Integer roleId);
}
