package edu.university.lab.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.common.service.BaseCrudService;
import edu.university.lab.module.user.dto.CreateUserRequest;
import edu.university.lab.module.user.dto.UpdateUserRequest;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.mapper.UserMapper;
import edu.university.lab.module.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends BaseCrudService<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<User> pageQuery(PageQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .orderByDesc(User::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(User::getRealName, query.getKeyword())
                .or()
                .like(User::getUsername, query.getKeyword())
                .or()
                .like(User::getUserNo, query.getKeyword()));
        }
        return pageQuery(query, wrapper);
    }

    @Override
    public boolean createUser(CreateUserRequest request) {
        User user = new User();
        user.setLaboratoryId(request.getLaboratoryId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getInitialPassword()));
        user.setRealName(request.getRealName());
        user.setUserNo(request.getUserNo());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        return save(user);
    }

    @Override
    public boolean updateUser(Integer userId, UpdateUserRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setLaboratoryId(request.getLaboratoryId());
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setUserNo(request.getUserNo());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        return updateById(user);
    }

    @Override
    public boolean resetPassword(Integer userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return updateById(user);
    }
}
