package com.spms.personal.service.impl;

import com.spms.common.redis.RedisHelper;
import com.spms.common.security.LoginSessionService;
import com.spms.personal.entity.UserEntity;
import com.spms.personal.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginSessionService loginSessionService;

    @Mock
    private RedisHelper redisHelper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
        ReflectionTestUtils.setField(userService, "loginSessionService", loginSessionService);
        ReflectionTestUtils.setField(userService, "redisHelper", redisHelper);
    }

    @Test
    void readsPermissionListFromJsonCache() {
        when(redisHelper.get("user_permission_2")).thenReturn("[\"role_getPage\"]");

        List<String> permissions = userService.getMyPermissionList(2L);

        assertThat(permissions).containsExactly("role_getPage");
    }

    @Test
    void storesPermissionListAsSerializableValue() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        List<String> permissions = List.of("role_getPage");
        when(redisHelper.get("user_permission_2")).thenReturn(null);
        when(userMapper.getById(2L)).thenReturn(user);
        when(userMapper.getPermissionIdentityListByUserId(2L)).thenReturn(permissions);

        List<String> result = userService.getMyPermissionList(2L);

        assertThat(result).containsExactly("role_getPage");
        verify(redisHelper).set("user_permission_2", permissions);
    }

    @Test
    void rebuildsPermissionListWhenCachedValueIsInvalidJson() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        List<String> permissions = List.of("role_getPage");
        when(redisHelper.get("user_permission_2")).thenReturn("[role_getPage]");
        when(userMapper.getById(2L)).thenReturn(user);
        when(userMapper.getPermissionIdentityListByUserId(2L)).thenReturn(permissions);

        List<String> result = userService.getMyPermissionList(2L);

        assertThat(result).containsExactly("role_getPage");
        verify(redisHelper).delete("user_permission_2");
        verify(redisHelper).set("user_permission_2", permissions);
    }

    @Test
    void logoutClearsSessionAndUserCaches() {
        userService.logout(2L);

        verify(loginSessionService).delete(2L);
        verify(redisHelper).delete("user_permission_2");
        verify(redisHelper).delete("user_menu_2");
    }
}
