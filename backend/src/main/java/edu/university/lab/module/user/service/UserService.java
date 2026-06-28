package edu.university.lab.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.user.dto.CreateUserRequest;
import edu.university.lab.module.user.dto.UpdateUserRequest;
import edu.university.lab.module.user.dto.UserListItem;
import edu.university.lab.module.user.entity.User;

public interface UserService extends IService<User> {

    Page<UserListItem> pageQuery(PageQuery query);

    boolean createUser(CreateUserRequest request);

    boolean updateUser(Integer userId, UpdateUserRequest request);

    boolean resetPassword(Integer userId, String newPassword);
}
