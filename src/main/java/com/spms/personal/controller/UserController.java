package com.spms.personal.controller;


import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.common.result.Json;
import com.spms.common.security.AuthIgnore;
import com.spms.common.security.Permission;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.model.UserLoginRequest;
import com.spms.personal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api("/user")
@RequiredArgsConstructor
public class UserController extends ApiController {
    private final UserService userService;

    @AuthIgnore
    @PostMapping("/login")
    public Json<String> login(@Valid @RequestBody UserLoginRequest request) {
        return Json.data(userService.login(request), "登录成功");
    }

    @Permission(authorize = false)
    @PostMapping("getMyInfo")
    public Json<UserEntity> getMyInfo() {
        long currentUserId = getCurrentUserId();
        return Json.data(userService.getMyInfo(currentUserId) );
    }

    @Permission(login = true, authorize = false)
    @PostMapping("/getMyPermissionList")
    public Json<List<String>> getMyPermissionList() {
        long currentUserId = getCurrentUserId();
        return Json.data(userService.getMyPermissionList(currentUserId));
    }

    @Permission(authorize = false)
    @PostMapping("getMyMenuList")
    public Json getMyMenuList() {
        long currentUserId = getCurrentUserId();
        return Json.data(userService.getMyMenuList(currentUserId));

    }

    @Permission(login = false)
    @PostMapping("logout")
    public Json<String> logout() {
        return Json.success("");
    }
}
