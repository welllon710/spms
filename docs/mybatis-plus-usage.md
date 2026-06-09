# MyBatis-Plus 使用说明

本文档记录 `spms` 项目中 MyBatis-Plus 的使用方式和代码约定。

当前项目的迁移策略是：保留 MyBatis XML 写 SQL 的方式，引入 MyBatis-Plus 主要用于 Spring Boot 集成、分页插件和 `Page/IPage` 分页模型。也就是说，本项目现阶段不是全面切换为 `BaseMapper + Wrapper` 模式。

## 依赖配置

项目使用 Spring Boot 3，所以依赖使用 `mybatis-plus-spring-boot3-starter`。

位置：`pom.xml`

```xml
<properties>
    <java.version>17</java.version>
    <mybatis-plus.version>3.5.12</mybatis-plus.version>
</properties>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

`mybatis-plus-jsqlparser` 用于支持分页拦截器。如果缺少它，`PaginationInnerInterceptor` 相关类或分页 SQL 解析可能无法正常工作。

## YAML 配置

位置：`src/main/resources/application.yml`

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

说明：

| 配置 | 作用 |
| --- | --- |
| `mapper-locations` | 指定 XML mapper 文件位置 |
| `map-underscore-to-camel-case` | 数据库下划线字段自动映射 Java 驼峰属性 |

迁移到 MyBatis-Plus 后，不再使用：

```yaml
mybatis:
  ...

pagehelper:
  ...
```

## 分页插件

位置：`src/main/java/com/spms/common/config/MybatisPlusConfig.java`

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

这里注册的是 MyBatis-Plus 的分页拦截器。

本项目数据库是 MySQL，所以使用：

```java
new PaginationInnerInterceptor(DbType.MYSQL)
```

## 分页请求模型

位置：`src/main/java/com/spms/base/PageQuery.java`

```java
public record PageQuery<T>(
        T filter,
        Integer pageNum,
        Integer pageSize
) {
}
```

请求结构统一为：

```json
{
  "filter": {
    "name": "admin",
    "code": "ADMIN",
    "isDisabled": false
  },
  "pageNum": 1,
  "pageSize": 20
}
```

说明：

| 字段 | 作用 |
| --- | --- |
| `filter` | 查询条件对象，不同模块使用不同 filter |
| `pageNum` | 当前页，从 1 开始 |
| `pageSize` | 每页数量 |

## 分页默认值

位置：`src/main/java/com/spms/base/BaseService.java`

```java
protected int getPageNum(PageQuery<?> request) {
    Integer pageNum = request == null ? null : request.pageNum();
    if (pageNum == null || pageNum < 1) {
        return DEFAULT_PAGE_NUM;
    }
    return pageNum;
}

protected int getPageSize(PageQuery<?> request) {
    Integer pageSize = request == null ? null : request.pageSize();
    if (pageSize == null || pageSize < 1) {
        return DEFAULT_PAGE_SIZE;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE);
}
```

当前约定：

| 配置 | 值 |
| --- | --- |
| 默认页码 | `1` |
| 默认每页数量 | `10` |
| 最大每页数量 | `100` |

Service 中创建 MyBatis-Plus 分页对象时，应使用这两个方法兜底：

```java
Page<RoleEntity> page = new Page<>(getPageNum(request), getPageSize(request));
```

## 分页返回模型

位置：`src/main/java/com/spms/common/result/PageResult.java`

```java
public record PageResult<T>(
        long total,
        int pageCount,
        List<T> list,
        Integer pageNum,
        Integer pageSize,
        SortParam sort
) {
    public static <T> PageResult<T> from(IPage<T> page, SortParam sort) {
        return new PageResult<>(
                page.getTotal(),
                (int) page.getPages(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                sort
        );
    }
}
```

MyBatis-Plus 返回的是 `IPage<T>`，Controller 对外返回项目自己的 `PageResult<T>`。

这样可以避免 Controller 直接暴露 MyBatis-Plus 的分页对象。

## Mapper 分页写法

分页 mapper 方法需要把 `Page<T>` 放在第一个参数。

示例：`RoleMapper`

```java
@Mapper
public interface RoleMapper {
    IPage<RoleEntity> getPageList(
            Page<RoleEntity> page,
            @Param("params") Map<String, Object> params
    );
}
```

注意点：

1. 返回值使用 `IPage<T>`。
2. 第一个参数使用 `Page<T>`。
3. 查询条件统一用 `@Param("params") Map<String, Object> params`。
4. XML 中条件要通过 `params.xxx` 访问。

## XML 分页 SQL 写法

示例：`src/main/resources/mapper/personal/RoleMapper.xml`

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
        <if test="params.isDisabled != null">
            and is_disabled = #{params.isDisabled}
        </if>
    </where>
    order by id desc
</select>
```

这里不需要手写 `limit`。

分页插件会根据 `Page<T>` 参数自动处理分页 SQL 和总数查询。

## Service 分页写法

示例：`RoleServiceImpl#getPage`

```java
@Override
public PageResult<RoleEntity> getPage(PageQuery<RolePageFilter> request) {
    RolePageFilter filter = request == null ? null : request.filter();
    Map<String, Object> params = QueryParams.of(filter)
            .putTrim("name", RolePageFilter::name)
            .putTrim("code", RolePageFilter::code)
            .put("isDisabled", RolePageFilter::isDisabled)
            .toMap();

    Page<RoleEntity> page = new Page<>(getPageNum(request), getPageSize(request));
    return PageResult.from(roleMapper.getPageList(page, params), DEFAULT_SORT);
}
```

本项目推荐这个顺序：

1. 从 `PageQuery` 中取 `filter`。
2. 使用 `QueryParams` 构建 XML 查询参数。
3. 使用 `new Page<>(getPageNum(request), getPageSize(request))` 创建分页对象。
4. 调用 mapper。
5. 使用 `PageResult.from(...)` 转成统一返回结构。

## QueryParams 查询参数工具

位置：`src/main/java/com/spms/common/util/QueryParams.java`

作用是减少这种重复防御代码：

```java
filter == null ? null : filter.name()
```

推荐写法：

```java
Map<String, Object> params = QueryParams.of(filter)
        .putTrim("identity", PermissionPageFilter::identity)
        .putTrim("name", PermissionPageFilter::name)
        .put("parentId", PermissionPageFilter::parentId)
        .put("type", PermissionPageFilter::type)
        .put("isSystem", PermissionPageFilter::isSystem)
        .put("isDisabled", PermissionPageFilter::isDisabled)
        .toMap();
```

方法说明：

| 方法 | 作用 |
| --- | --- |
| `put` | 原样放入参数，适合数字、布尔值、ID |
| `putTrim` | 字符串去首尾空格，空字符串转为 `null` |
| `toMap` | 返回 XML mapper 使用的参数 Map |

## 哪些模块使用分页

当前已经使用 MyBatis-Plus 分页的模块：

| 模块 | Mapper 方法 | 返回 |
| --- | --- | --- |
| 角色 | `RoleMapper#getPageList` | `IPage<RoleEntity>` |
| 单位 | `UnitMapper#getPageList` | `IPage<UnitEntity>` |
| 用户 | `UserMapper#getPageList` | `IPage<UserEntity>` |

这些模块的 controller 通常返回：

```java
Json<PageResult<XxxEntity>>
```

## 哪些模块不使用分页

当前以下模块的 `getPage` 实际返回树结构，不做 MyBatis-Plus 分页：

| 模块 | 原因 |
| --- | --- |
| 部门 | 需要返回部门树 |
| 权限 | 需要返回权限树 |
| 菜单 | 需要返回菜单树 |

这些模块仍然使用普通 `List<T>` 查询：

```java
List<DepartmentEntity> getPageList(Map<String, Object> params);
```

然后在 service 中调用：

```java
TreeUtils.buildTree(...)
```

注意：树结构接口不要为了统一而强行分页。分页会破坏树的完整性，比如只查出子节点但没有父节点，前端无法正确组树。

## 新增分页接口的推荐步骤

假设新增一个 `Product` 模块，需要分页列表。

### 1. 定义 filter

```java
public record ProductPageFilter(
        String name,
        String code,
        Boolean isDisabled
) {
}
```

### 2. Mapper 方法

```java
IPage<ProductEntity> getPageList(
        Page<ProductEntity> page,
        @Param("params") Map<String, Object> params
);
```

### 3. XML 查询

```xml
<select id="getPageList" resultMap="productResultMap">
    select
    <include refid="productColumns"/>
    from product
    <where>
        <if test="params.name != null and params.name != ''">
            and name like concat('%', #{params.name}, '%')
        </if>
        <if test="params.code != null and params.code != ''">
            and code like concat('%', #{params.code}, '%')
        </if>
        <if test="params.isDisabled != null">
            and is_disabled = #{params.isDisabled}
        </if>
    </where>
    order by id desc
</select>
```

### 4. Service 实现

```java
@Override
public PageResult<ProductEntity> getPage(PageQuery<ProductPageFilter> request) {
    ProductPageFilter filter = request == null ? null : request.filter();
    Map<String, Object> params = QueryParams.of(filter)
            .putTrim("name", ProductPageFilter::name)
            .putTrim("code", ProductPageFilter::code)
            .put("isDisabled", ProductPageFilter::isDisabled)
            .toMap();

    Page<ProductEntity> page = new Page<>(getPageNum(request), getPageSize(request));
    return PageResult.from(productMapper.getPageList(page, params), DEFAULT_SORT);
}
```

### 5. Controller 返回

```java
@PostMapping("/getPage")
public Json<PageResult<ProductEntity>> getPage(
        @RequestBody(required = false) PageQuery<ProductPageFilter> request
) {
    return Json.success(productService.getPage(request));
}
```

## CRUD 的当前写法

虽然项目已经引入 MyBatis-Plus，但当前 CRUD 仍然主要使用 XML mapper 自定义 SQL。

例如：

```java
int insert(RoleEntity role);

int update(RoleEntity role);

int deleteById(@Param("id") Long id);

RoleEntity getById(@Param("id") Long id);
```

这样做的好处是：

1. 迁移成本低。
2. SQL 可控，适合当前项目里较多关联表查询。
3. 不影响后续逐步引入 `BaseMapper` 或 `LambdaQueryWrapper`。

## 是否使用 BaseMapper

当前项目暂不强制使用：

```java
public interface RoleMapper extends BaseMapper<RoleEntity>
```

原因：

1. 项目已有大量 XML SQL。
2. 多数模块包含关联表、树结构、角色权限菜单等复杂查询。
3. 当前迁移目标是先稳定切换 MyBatis-Plus 分页，而不是一次性改掉全部 mapper 风格。

后续如果某个模块是简单 CRUD，可以再单独考虑使用 `BaseMapper`。

## 常见错误

### XML 中忘记写 params 前缀

错误写法：

```xml
<if test="name != null and name != ''">
    and name like concat('%', #{name}, '%')
</if>
```

分页 mapper 中参数已经写成：

```java
@Param("params") Map<String, Object> params
```

所以 XML 应写成：

```xml
<if test="params.name != null and params.name != ''">
    and name like concat('%', #{params.name}, '%')
</if>
```

### Mapper 分页参数顺序错误

推荐：

```java
IPage<RoleEntity> getPageList(Page<RoleEntity> page, @Param("params") Map<String, Object> params);
```

不要把 `Page<T>` 放在后面。

### 分页 SQL 手写 limit

分页查询 XML 不要写：

```sql
limit #{offset}, #{pageSize}
```

MyBatis-Plus 分页插件会自动处理。

### 树接口强行分页

部门、权限、菜单这类树结构接口不要直接套分页。

如果确实需要树结构分页，要先明确业务语义：分页的是根节点、搜索结果，还是完整树。没有明确前不要改。

## 项目约定总结

1. XML mapper 继续作为主要 SQL 承载。
2. 普通分页接口使用 MyBatis-Plus `Page/IPage`。
3. Controller 对外统一返回 `PageResult<T>`。
4. 查询参数使用 `QueryParams` 构建。
5. 分页默认值和最大值由 `BaseService` 统一处理。
6. 树结构接口返回 `List<T>`，不走 MyBatis-Plus 分页。
7. 不为了使用 MyBatis-Plus 而强行改成 `BaseMapper`。
