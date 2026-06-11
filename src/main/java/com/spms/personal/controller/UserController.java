package com.spms.personal.controller;


import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.AuthIgnore;
import com.spms.common.security.Permission;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.model.UserLoginRequest;
import com.spms.personal.model.UserPageFilter;
import com.spms.personal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Api("/user")
@RequiredArgsConstructor
public class UserController extends ApiController {
    private final UserService userService;

    @AuthIgnore
    @PostMapping("/login")
    public Json<String> login(@Valid @RequestBody UserLoginRequest request) {
        return Json.data(userService.login(request), "登录成功");
    }

    @Permission(login = true)
    @PostMapping("/getPage")
    public Json<PageResult<UserEntity>> getPage(@RequestBody PageQuery<UserPageFilter> request) {
        return Json.data(userService.getPage(request));
    }

    @Permission(login = true)
    @PostMapping("/getDetail")
    public Json<UserEntity> getDetail(@RequestBody Map<String, Long> map) {
        Long id = map.get("id");
        return Json.data(userService.getDetail(id));
    }

    @Permission(login = true)
    @PostMapping("/update")
    public Json<String> update(@RequestBody UserEntity userEntity) {
        userService.update(userEntity);
        return Json.success("修改成功");
    }

    @Permission(authorize = false)
    @PostMapping("getMyInfo")
    public Json<UserEntity> getMyInfo() {
        long currentUserId = getCurrentUserId();
        UserEntity myInfo = userService.getMyInfo(currentUserId);
        return Json.data(myInfo);
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

    @Permission(login = false,authorize = false)
    @PostMapping("/logout")
    public Json<String> logout() {
        userService.logout(getLoginToken());
        return Json.success("退出成功");
    }
}
