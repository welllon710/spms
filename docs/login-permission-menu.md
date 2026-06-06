# Login, User, Permission, Menu Logic

This document records the first migration slice from `SPMS-Server` to this project:

- `POST /user/login`
- `POST /user/getMyInfo`
- `POST /user/getMyPermissionList`
- `POST /user/getMyMenuList`

The original project uses JPA and AirPower4J. This project will use MyBatis, so the logic should stay consistent while the implementation can be different.

## Related Tables

Core tables:

| Table | Purpose |
| --- | --- |
| `user` | User account, profile, password and disabled state |
| `role` | User role |
| `permission` | Permission identity list |
| `menu` | Frontend menu tree |
| `department` | User department |

Relation tables:

| Table | Columns | Purpose |
| --- | --- | --- |
| `user_role_list` | `user_entity_id`, `role_list_id` | User to role |
| `user_department_list` | `user_entity_id`, `department_list_id` | User to department |
| `role_permission_list` | `role_entity_id`, `permission_list_id` | Role to permission |
| `role_menu_list` | `role_entity_id`, `menu_list_id` | Role to menu |

Optional table for later:

| Table | Purpose |
| --- | --- |
| `user_third_login` | Third-party account binding/login. Not required for the first account-password login slice. |

## Table Fields

### `user`

Main fields used by this slice:

| Column | Purpose |
| --- | --- |
| `id` | User ID |
| `email` | Login account |
| `phone` | Phone number |
| `password` | Encrypted password |
| `salt` | Password salt |
| `nickname` | Nickname |
| `avatar` | Avatar URL |
| `real_name` | Real name |
| `id_card` | ID card |
| `gender` | Gender dictionary value |
| `is_disabled` | Whether the account is disabled |

Base fields:

| Column | Purpose |
| --- | --- |
| `create_time` | Created timestamp |
| `update_time` | Updated timestamp |
| `is_published` | Published flag |

### `role`

| Column | Purpose |
| --- | --- |
| `id` | Role ID |
| `name` | Role name |
| `code` | Role code |
| `is_disabled` | Disabled flag |

### `permission`

| Column | Purpose |
| --- | --- |
| `id` | Permission ID |
| `identity` | Permission key returned to frontend |
| `name` | Permission name |
| `parent_id` | Parent permission ID |
| `type` | Permission type |
| `is_system` | System permission flag |
| `is_disabled` | Disabled flag |

### `menu`

| Column | Purpose |
| --- | --- |
| `id` | Menu ID |
| `name` | Menu name |
| `parent_id` | Parent menu ID |
| `path` | Route path |
| `component` | Frontend component path |
| `icon` | Menu icon |
| `order_no` | Sort order |
| `is_disabled` | Disabled flag |

### `department`

| Column | Purpose |
| --- | --- |
| `id` | Department ID |
| `name` | Department name |
| `code` | Department code |
| `parent_id` | Parent department ID |
| `order_no` | Sort order |
| `is_disabled` | Disabled flag |

## Login Logic

Endpoint:

```text
POST /user/login
```

Input:

```json
{
  "email": "admin@example.com",
  "password": "123456"
}
```

Steps:

1. Query user by email.
2. If user does not exist, return account/password invalid.
3. Hash the submitted password with the stored `salt`.
4. Compare with stored `password`.
5. If password is wrong, return account/password invalid.
6. If `is_disabled = 1`, return disabled error.
7. Delete user permission/menu cache if cache is used.
8. Generate token.
9. Return token.

Suggested SQL:

```sql
select *
from user
where email = #{email}
limit 1;
```

Response shape:

```json
{
  "code": 0,
  "message": "登录成功",
  "data": "token"
}
```

## Get My Info Logic

Endpoint:

```text
POST /user/getMyInfo
```

Steps:

1. Read current user ID from token.
2. Query `user` by ID.
3. Do not return sensitive fields such as `password` and `salt`.
4. Return user info.

Suggested SQL:

```sql
select id,
       create_time,
       update_time,
       is_disabled,
       is_published,
       avatar,
       email,
       gender,
       id_card,
       nickname,
       phone,
       real_name
from user
where id = #{userId}
limit 1;
```

If department info is needed:

```sql
select d.*
from department d
join user_department_list ud on ud.department_list_id = d.id
where ud.user_entity_id = #{userId}
order by d.order_no asc, d.id asc;
```

## Get My Permission List Logic

Endpoint:

```text
POST /user/getMyPermissionList
```

Original behavior:

- Root user is `user.id = 1`.
- Root user gets all permissions.
- Normal user gets permissions through roles.
- Return only permission `identity` values, not full permission objects.
- Duplicate permissions are removed.

Root user SQL:

```sql
select distinct p.identity
from permission p
where coalesce(p.is_disabled, 0) = 0
order by p.id asc;
```

Normal user SQL:

```sql
select distinct p.identity
from user_role_list ur
join role r on r.id = ur.role_list_id
join role_permission_list rp on rp.role_entity_id = r.id
join permission p on p.id = rp.permission_list_id
where ur.user_entity_id = #{userId}
  and coalesce(r.is_disabled, 0) = 0
  and coalesce(p.is_disabled, 0) = 0
order by p.id asc;
```

Response shape:

```json
{
  "code": 0,
  "message": "查询成功",
  "data": [
    "user:add",
    "user:update"
  ]
}
```

## Get My Menu List Logic

Endpoint:

```text
POST /user/getMyMenuList
```

Original behavior:

- Root user should get all menus.
- Normal user gets menus through roles.
- Duplicate menus are removed.
- Returned menus should be assembled into a tree by `parent_id`.

Root user SQL:

```sql
select distinct m.*
from menu m
where coalesce(m.is_disabled, 0) = 0
order by m.order_no asc, m.id asc;
```

Normal user SQL:

```sql
select distinct m.*
from user_role_list ur
join role r on r.id = ur.role_list_id
join role_menu_list rm on rm.role_entity_id = r.id
join menu m on m.id = rm.menu_list_id
where ur.user_entity_id = #{userId}
  and coalesce(r.is_disabled, 0) = 0
  and coalesce(m.is_disabled, 0) = 0
order by m.order_no asc, m.id asc;
```

Tree assembly rule:

1. Treat `parent_id = 0` or `parent_id is null` as root menu.
2. Put each menu under the menu whose `id` equals its `parent_id`.
3. Sort siblings by `order_no`, then `id`.
4. Keep the response fields consistent with the original frontend expectation.

Response shape:

```json
{
  "code": 0,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "name": "系统管理",
      "parentId": 0,
      "path": "/system",
      "component": "",
      "icon": "setting",
      "orderNo": 1,
      "children": []
    }
  ]
}
```

## Cache Keys

The original project uses Redis for permission and menu cache:

| Cache | Key |
| --- | --- |
| User permissions | `user_permission_{userId}` |
| User menus | `user_menu_{userId}` |

For the first implementation, cache can be skipped. Once the SQL behavior is stable, add Redis cache with the same key names to keep behavior close to `SPMS-Server`.

## Migration Order

Recommended implementation order:

1. Create user domain model and mapper.
2. Implement `POST /user/login`.
3. Implement token parsing and `POST /user/getMyInfo`.
4. Implement permission mapper and `POST /user/getMyPermissionList`.
5. Implement menu mapper, tree assembly, and `POST /user/getMyMenuList`.
6. Add Redis cache after the SQL behavior is correct.

