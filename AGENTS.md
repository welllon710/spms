# AGENTS.md

## 后续模块开发约定

后续新增业务模块默认使用 MyBatis-Plus 写法，不再引入 PageHelper 或旧分页模式。

- 分页接口统一使用 `PageQuery<T>` 作为请求模型。
- 分页查询统一使用 MyBatis-Plus `Page<T>` / `IPage<T>`，并通过 `PageResult.from(...)` 返回。
- Mapper 分页方法把 `Page<T>` 放在第一个参数，查询条件统一使用 `@Param("params") Map<String, Object> params`。
- XML 中访问查询条件时使用 `params.xxx`，不要直接使用 `xxx`。
- 简单 CRUD 优先考虑 MyBatis-Plus 的 `BaseMapper` / Wrapper 写法；需要复杂关联、树结构、批量关系表操作或可读性更好的 SQL 时，可以继续保留 XML SQL。
- 树结构接口仍然返回完整 `List<T>`，不要为了统一而强行分页。

## 项目概览

本项目是 `spms` 后端服务，基于 Spring Boot 3.3.5、Java 17、MyBatis-Plus、MySQL、Redis。

项目从 AirPower4J / SPMS-Server 迁移而来。迁移时保持业务逻辑一致，但实现方式以当前项目结构和代码风格为准。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.12
- MySQL
- Redis
- Lombok
- JUnit 5
- Mockito

## 代码约定

- Controller 使用项目内的 `@ApiController` / `@Api` 注解。
- 接口统一返回 `Json<T>`。
- 分页返回使用 `PageResult<T>`。
- 分页请求使用 `PageQuery<T>`。
- 查询参数使用 `QueryParams` 构建，避免重复写 `filter == null ? null : filter.xxx()`。
- 通用实体字段继承 `BaseEntity`，包括 `id`、`createTime`、`updateTime`、`isPublished`、`isDisabled`。

## MyBatis-Plus 约定

当前项目保留 XML SQL，不强制使用 `BaseMapper`。

分页 mapper 写法：

```java
IPage<RoleEntity> getPageList(
        Page<RoleEntity> page,
        @Param("params") Map<String, Object> params
);
```

XML 中使用 `params.xxx`：

```xml
<if test="params.name != null and params.name != ''">
    and name like concat('%', #{params.name}, '%')
</if>
```

不要手写 `limit`，由 MyBatis-Plus 分页插件处理。

## XML 写法约定

MyBatis XML 中优先使用动态 SQL 标签表达条件和结构，少写命令式拼接或硬编码占位逻辑。

推荐使用：

- `<where>` 处理动态查询条件，避免手写 `where 1 = 1`。
- `<set>` 处理动态更新字段，避免手写逗号拼接。
- `<if>` 处理可选条件。
- `<foreach>` 处理批量插入、批量关联和 `in` 查询。
- `<trim>` / `<choose>` / `<when>` / `<otherwise>` 处理更复杂的动态结构。
- `<sql>` / `<include>` 复用字段列表，避免重复写列名。

示例：

```xml
<select id="getPageList" resultMap="roleResultMap">
    select
    <include refid="roleColumns"/>
    from `role`
    <where>
        <if test="params.name != null and params.name != ''">
            and name like concat('%', #{params.name}, '%')
        </if>
        <if test="params.code != null and params.code != ''">
            and code like concat('%', #{params.code}, '%')
        </if>
    </where>
    order by id desc
</select>
```

不推荐：

```xml
where 1 = 1
<if test="params.name != null and params.name != ''">
    and name like concat('%', #{params.name}, '%')
</if>
```

优先使用 `#{}` 参数绑定，不要用 `${}` 拼接用户输入。

## Service 约定

Service 实现类可以继承：

```java
BaseService<XxxEntity>
```

`BaseService` 只放通用能力，不做过度抽象。当前已有能力：

- 初始化新增实体
- 初始化更新实体
- 校验是否可编辑
- 分页参数默认值处理

不要重新引入复杂的 `BasePageService` 或过度封装分页逻辑。分页逻辑优先写在具体 service 里。

## 树结构约定

部门、菜单、权限这类树结构接口不强行分页。

树结构使用：

```java
TreeUtils.buildTree(...)
```

原因：分页会破坏树的完整性。

## 参数校验约定

常用工具：

```java
ParamUtils.requireNotNull(...)
ParamUtils.requireId(...)
ParamUtils.requireText(...)
ParamUtils.trimToNull(...)
```

不要在每个 service 里重复写大量空判断。

## 测试命令

使用 Java 17：

```bash
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -Dmaven.repo.local=/private/tmp/spms-m2 -DskipTests compile
```

完整测试：

```bash
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -Dmaven.repo.local=/private/tmp/spms-m2 test
```

## 禁止事项

- 不要随意重构无关模块。
- 不要恢复 PageHelper。
- 不要把树结构接口强行改成分页。
- 不要引入过度抽象的分页父类。
- 不要删除用户已有改动。
- 不要把简单 CRUD 一次性全部改成 `BaseMapper`，除非明确要求。

## 推荐工作方式

1. 先阅读相关 controller、service、mapper、XML。
2. 优先沿用已有代码风格。
3. 小步修改。
4. 修改后运行编译。
5. 涉及 service 行为时补充或更新测试。
