package edu.university.lab.module.user.controller;

import edu.university.lab.common.api.ApiResponse;
import edu.university.lab.common.api.PageResponse;
import edu.university.lab.common.constant.Messages;
import edu.university.lab.common.query.PageQuery;
import edu.university.lab.module.user.dto.CreateUserRequest;
import edu.university.lab.module.user.dto.ResetPasswordRequest;
import edu.university.lab.module.user.dto.UpdateUserRequest;
import edu.university.lab.module.user.dto.UserListItem;
import edu.university.lab.module.user.entity.User;
import edu.university.lab.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping
    public ApiResponse<PageResponse<UserListItem>> page(@Valid PageQuery query) {
        return ApiResponse.success(PageResponse.from(userService.pageQuery(query)));
    }

    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasAuthority('user:view')")
    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Integer id) {
        return ApiResponse.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PreAuthorize("hasAuthority('user:edit')")
    @PostMapping
    public ApiResponse<Boolean> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(Messages.USER_CREATED, userService.createUser(request));
    }

    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('user:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> update(@PathVariable Integer id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(Messages.USER_UPDATED, userService.updateUser(id, request));
    }

    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasAuthority('user:edit')")
    @PutMapping("/{id}/reset-password")
    public ApiResponse<Boolean> resetPassword(@PathVariable Integer id, @Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.success(Messages.PASSWORD_RESET_SUCCESS, userService.resetPassword(id, request.getNewPassword()));
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('user:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Integer id) {
        return ApiResponse.success(Messages.USER_DELETED, userService.removeById(id));
    }
}
