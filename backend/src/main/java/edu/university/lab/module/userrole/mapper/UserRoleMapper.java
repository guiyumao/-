package edu.university.lab.module.userrole.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.lab.module.userrole.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteForceByUserId(@Param("userId") Integer userId);
}
