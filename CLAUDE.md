# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

`spms` 是一个基于 Spring Boot 3.3.5 / Java 17 的生产管理系统后端，涵盖人员权限、仓储、渠道（采购/销售）、资产与 IoT 采集模块。

**技术栈**：MyBatis-Plus 3.5.12、MySQL、Redis、Lombok、JWT (auth0/java-jwt)、Jakarta Bean Validation、JUnit 5 + Mockito。

## 构建与测试命令

```bash
# 编译（跳过测试）
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -DskipTests compile

# 测试编译
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -DskipTests test-compile

# 运行所有测试
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q test

# 运行单个测试类
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q test -Dtest=RoleServiceImplTest
```

如遇 Maven 仓库权限或网络问题，追加 `-Dmaven.repo.local=/private/tmp/spms-m2`。

## 模块结构

代码按业务域划分，每个包内均有 `controller / entity / mapper / model / service / service/impl` 子包：

- `base` — 跨模块基类：`BaseEntity`、`BaseService<E>`、`ApiController`、`PageQuery<T>`、`PageParams`、`IdRequest`、`RejectRequest`
- `common` — 横切关注点：配置（`EntityMetaObjectHandler`）、异常处理（`GlobalExceptionHandler`）、安全（JWT + Redis Session）、工具类
- `personal` — 用户、角色、部门、菜单、权限、组织单元
- `asset` — 设备、物料（IoT 资产主数据）
- `iot` — 采集参数（`ParameterEntity`）
- `channel` — 客户、供应商、采购单、销售单、价格表
- `wms` — 入库、出库、移库、库存、存储位
- `system` — 编码规则（`CodeRuleService`）、系统配置

---

# 一、规范

## 1.1 请求 / 响应约定

- **所有接口返回** `Json<T>`（`Json.data(...)` / `Json.success(...)` / `Json.error(...)`）。Controller 方法签名禁止使用裸类型 `Json`，必须带泛型参数。
- **分页**：请求用 `PageQuery<FilterType>`，响应用 `PageResult<T>`；通过 `PageResult.from(iPage, mapper)` 转换。
- **分页参数兼容两种格式**（顶层 `pageNum/pageSize` 或嵌套 `page.pageNum/pageSize`），由 `BaseService#getPageNum/getPageSize` 统一处理，上限 100。
- **HTTP 方法统一用 POST**，路径风格 `/user/getPage`、`/role/add`、`/purchase/audit`、`/sale/reject`。
- **Controller 注解**：`@Api("/path")` = `@RestController + @RequestMapping`；继承 `ApiController` 可调用 `getCurrentUserId()`。
- **Controller 类必须加 `@Validated`**，`@RequestBody` 参数必须加 `@Valid` 触发 Bean Validation。

## 1.2 命名约定

| 类型 | 规范 | 示例 |
|---|---|---|
| Entity | `XxxEntity` | `PurchaseEntity` |
| Mapper | `XxxMapper` | `PurchaseMapper` |
| Service 接口 | `XxxService` | `PurchaseService` |
| Service 实现 | `XxxServiceImpl` | `PurchaseServiceImpl` |
| Controller | `XxxController` | `PurchaseController` |
| 新增请求 DTO | `XxxAddRequest` | `PurchaseAddRequest` |
| 更新请求 DTO | `XxxUpdateRequest` | `SaleUpdateRequest` |
| 完成请求 DTO | `XxxFinishRequest` | `InputFinishRequest` |
| 分页过滤器 | `XxxPageFilter` | `PurchasePageFilter` |
| 通用 ID 请求 | 复用 `IdRequest` | — |
| 通用驳回请求 | 复用 `RejectRequest` | — |
| 状态枚举 | `XxxStatus` | `InputStatus.AUDITING` |

## 1.3 单据状态流转

| 模块 | 流转链 |
|---|---|
| 采购 | AUDITING → PURCHASING → IN_STORAGE \| REJECTED |
| 销售 | AUDITING → OUT_STORAGE \| REJECTED |
| 入库 | AUDITING → INPUTTING → FINISHED \| REJECTED |
| 出库 | AUDITING → OUTPUTTING → FINISHED \| REJECTED |
| 移库 | AUDITING → MOVING → FINISHED \| REJECTED |

`update`/`audit`/`reject` 必须前置校验状态，仅 AUDITING 可修改/审批/驳回。

## 1.4 import 顺序（阿里规范）

```java
// 1. 三方/项目普通 import（按字母序，按包分组）
import com.baomidou.mybatisplus.xxx;
import com.spms.xxx;
import lombok.xxx;
import org.springframework.xxx;

// 2. JDK 包
import java.math.BigDecimal;
import java.util.List;

// 3. 静态 import 独立块（最后）
import static com.spms.common.util.ParamUtils.requireId;
```

---

# 二、代码实践

## 2.1 字段填充：MetaObjectHandler 自动接管

`BaseEntity` 字段 `createTime / updateTime / isDisabled / isPublished` 由 `EntityMetaObjectHandler` 在 MyBatis 层自动填充，**禁止**手动 `setCreateTime` / `setUpdateTime`。

```java
// ✅ 正确：直接 insert/updateById，框架自动填充
PurchaseEntity entity = new PurchaseEntity();
entity.setBillCode(...).setStatus(...);
purchaseMapper.insert(entity);

// ❌ 错误：手动填充
entity.setCreateTime(System.currentTimeMillis());
entity.setUpdateTime(System.currentTimeMillis());
```

`BaseService` 仅保留：
- `checkEditable(entity)` — 已发布则禁止修改
- `getPageNum/getPageSize` — 分页参数提取

## 2.2 参数校验：注解优先，Service 防御兜底

**Controller 边界**：用 Jakarta Bean Validation 注解（在 record / DTO / Entity 字段上）。

```java
public record PurchaseUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        String reason,
        @NotEmpty(message = "采购明细不能为空") List<@Valid PurchaseDetailEntity> details
) {}

@PostMapping("update")
public Json<String> update(@RequestBody @Valid PurchaseUpdateRequest request) { ... }
```

校验失败由 `GlobalExceptionHandler` 统一捕获 `MethodArgumentNotValidException` / `ConstraintViolationException`，返回 `Json.error(PARAM_INVALID, ...)`。

**Service 内部**：仅对 DB 查询结果或派生值使用 `ParamUtils`：
- `requireId(id, "...")` — `getRequiredXxx(Long id)` 防御性校验
- `requirePositiveQuantity(detail.getQuantity(), "...")` — 防御 DB 数据异常
- `requireNotNull` / `requireText` / `trimToNull` — 数据规范化

```java
// ✅ Service 防御性校验（DB 来源）
private PurchaseEntity getRequiredPurchase(Long id) {
    requireId(id, "采购单ID不能为空");
    PurchaseEntity purchase = purchaseMapper.selectById(id);
    if (purchase == null) {
        throw new AppException(CommonError.DATA_NOT_FOUND, "采购单不存在");
    }
    return purchase;
}

// ❌ 不要在 Service 中重复校验已被注解保护的请求字段
public void add(PurchaseAddRequest request) {
    requireNotNull(request, "...");        // 注解已保证
    requireId(request.id(), "...");        // @Positive 已保证
}
```

## 2.3 请求 DTO：强类型，不复用 Entity

**add / update 必须用专用 record DTO**，不直接接收 Entity：

```java
// ✅ Controller / Service 接口
public Json<String> add(@RequestBody @Valid PurchaseAddRequest request)
public Json<String> update(@RequestBody @Valid PurchaseUpdateRequest request)
public Json<String> audit(@RequestBody @Valid IdRequest request)
public Json<String> reject(@RequestBody @Valid RejectRequest request)
public Json<String> getDetail(@RequestBody @Valid IdRequest request)
```

Entity 仅做 DB 映射，不承担请求 DTO 职责。

## 2.4 MyBatis-Plus 约定

- **简单 CRUD**：`BaseMapper` 方法 + Wrapper（`Wrappers.lambdaQuery`/`lambdaUpdate`）
- **分页查询**：`Page<T>` + `IPage<T>`，Mapper 签名 `IPage<XxxEntity> getPageList(Page<XxxEntity> page, @Param("params") Map<String,Object> params)`
- **复杂关联或树**：保留 XML，用 `<where>/<set>/<foreach>/<sql>/<include>`，禁止手写 `where 1=1` / `limit`
- **查询参数构建**：统一用 `QueryParams.of(filter).put(...).putTrim(...).toMap()`，避免大量 null 判断

## 2.5 状态变更：构造新实体而非修改 exist

```java
// ✅ 推荐
public void audit(IdRequest request) {
    PurchaseEntity exist = getRequiredPurchase(request.id());
    if (!PurchaseStatus.AUDITING.getValue().equals(exist.getStatus())) {
        throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
    }
    PurchaseEntity update = new PurchaseEntity();
    update.setId(request.id());
    update.setStatus(PurchaseStatus.PURCHASING.getValue());
    purchaseMapper.updateById(update);  // updateTime 由 MetaObjectHandler 填充
}

// ❌ 不要
exist.setStatus(...);
purchaseMapper.updateById(exist);  // 会写回所有字段
```

## 2.6 状态流转必须 `@Transactional`

`add` / `update` / `audit` / `reject` / `addFinish` 涉及多表写入或库存联动的方法必须加 `@Transactional(rollbackFor = Exception.class)`。

## 2.7 编码生成

业务编码统一调 `CodeRuleService#createCode(CodeRuleField.XXX)`：

```java
private String resolveBillCode(String billCode) {
    String code = trimToNull(billCode);
    return code != null ? code : codeRuleService.createCode(CodeRuleField.PURCHASE_BILL_CODE);
}

private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
    String code = trimToNull(requestBillCode);
    return code != null ? code : existBillCode;
}
```

**新增 CodeRuleField 枚举值时，`key` 必须唯一**（已修复 STORAGE_CODE 与 DEVICE_CODE 重复 bug）。

## 2.8 树结构

部门 / 菜单 / 权限 / 仓库等树形数据**不分页**，统一用 `TreeUtils.buildTree(list, getId, getParentId, setChildren)`。

## 2.9 重复键防护

业务唯一约束（如名称 + 编码）通过 `checkDuplicate(name, code, excludeId)` 模式实现。**update 必须传 `request.getId()` 作为 `excludeId`**，否则会把自身计入重复。

## 2.10 库存原子操作

库存增减用 SQL 原子操作（`InventoryMapper.increaseQuantity` / `decreaseQuantity` / `decreaseForMove` / `increaseForMove`），先 update，update 行数为 0 时再 insert。前提是 `inventory(material_id, storage_id, type)` 上有唯一索引。

## 2.11 依赖注入

统一用 `@RequiredArgsConstructor` + `final` 字段：

```java
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends BaseService<PurchaseEntity> implements PurchaseService {
    private final PurchaseMapper purchaseMapper;
    private final CodeRuleService codeRuleService;
}
```

禁止混用 `@Resource` / `@Autowired` / `@AllArgsConstructor`。

## 2.12 异常处理

- 业务异常抛 `AppException(CommonError.XXX, message)`，由 `GlobalExceptionHandler` 统一处理
- 不要 `catch (Exception e)` 静默吞异常
- 不要在业务代码中直接返回 HTTP 状态码

---

# 三、注意事项

## 3.1 安全机制

`RequestInterceptor` 在每次请求前执行：

1. 注解判断访问级别：
   - `@AuthIgnore` — 完全公开（如 `/user/login`）
   - `@Permission(login=true, authorize=false)` — 需登录，不校验权限
   - `@Permission(authorize=true)`（默认）— 需登录 + 权限校验
2. `TokenService` 用 HMAC256 JWT 校验 token，提取 `userId`
3. `LoginSessionService` 比对 Redis 中缓存的 token（单点登录，key = `login_token_{userId}`）
4. 权限标识自动由类名 + 方法名生成（如 `Role_getPage`），`userId=1` 为超级管理员跳过校验
5. 用户权限/菜单缓存到 Redis（`user_permission_{userId}` / `user_menu_{userId}`），登录/登出/更新用户时清除
6. 密码：`SHA1(SHA1(password+salt) + SHA1(salt+password))`，通过 `PermissionUtil.encodePassword`

## 3.2 设备模块默认值（新增时）

- `status=4`（关机）、`alarm=0`（正常）、`partCount=0`、`isReporting=true`、`rate=1000`
- `code` 为空时自动生成，`uuid` 默认等于 `code`
- 参数删除前须检查设备绑定，系统参数禁止删除

## 3.3 BigDecimal 注意

- 金额 / 数量字段统一用 `BigDecimal`（包括 `MaterialEntity.purchasePrice/salePrice`）
- 比较用 `compareTo` 而非 `equals`（`equals` 会比较 scale）
- 累加前判 null：`detail.getFinishQuantity() == null ? BigDecimal.ZERO : detail.getFinishQuantity()`
- 乘除前判 null：`calculateTotalPrice` 中 `detail.getPrice()` / `detail.getQuantity()` 为 null 时跳过或抛异常

## 3.4 `${}` vs `#{}`

XML 中只用 `#{}` 防 SQL 注入。`${}` 仅限于排序字段名等可信值（且必须先白名单校验）。

## 3.5 单据修改限制

- 仅 `AUDITING` 状态可修改 / 审批 / 驳回
- 修改时先 `delete` 旧明细再 `insert` 新明细（不要 update 明细）
- `billCode` 为空时保留原值（用 `resolveUpdateBillCode`），不要被空字符串覆盖

## 3.6 Repeat 防御

- `getRequiredXxx(Long id)` 内部的 `requireId` 是防御性校验（保留），因为可能被 DB 派生 ID 调用
- 不要在已被 `@Valid` + 注解保护的请求字段上重复 `requireId`

## 3.7 当前已知遗留项

- `application.yml` 中数据库密码、JWT 密钥仍为明文，建议改为环境变量
- `SaleServiceImpl.saveDetails` 仍是逐条 insert，明细多时可改批量
- 部分模块（personal、asset、iot）尚未应用强类型 DTO 改造

## 3.8 禁止事项

- 不恢复 PageHelper，不把树接口改成分页，不引入复杂分页父类
- 不随意重构无关模块，不删除已有改动
- 不用 `${}` 拼接用户输入（SQL 注入），只用 `#{}`
- 不手写 `.codegraph` 数据文件（由 MCP codegraph 工具维护）
- 不在 Entity 上同时承担 DB 映射和请求 DTO 职责（add/update 用专用 record）
- 不手动 `setCreateTime` / `setUpdateTime`（MetaObjectHandler 自动填充）
- 不混用 `@Resource` / `@Autowired`，统一 `@RequiredArgsConstructor`
- 不裸类型返回 `Json`，必须 `Json<T>`
- 不在 `add` 接口里用 Entity 接收请求，必须用 `XxxAddRequest` record

---

# 四、新增模块开发清单

新增一个业务模块（以 `xxx` 为例）的标准步骤：

1. **Entity**：`XxxEntity extends BaseEntity`，`@TableName("xxx")`，关联对象用 `@TableField(exist = false)`
2. **状态枚举**（如有）：`XxxStatus`，含 `getValue()`
3. **Mapper**：`XxxMapper extends BaseMapper<XxxEntity>`，复杂查询写 XML
4. **请求 DTO**：
   - `XxxPageFilter`（record）
   - `XxxAddRequest`（record，含 `@NotEmpty/@Valid` 等注解）
   - `XxxUpdateRequest`（record，含 `@Positive id`）
   - `XxxFinishRequest`（如有完成动作）
5. **Service 接口**：`XxxService`，方法签名用强类型 DTO
6. **Service 实现**：`XxxServiceImpl extends BaseService<XxxEntity> implements XxxService`，`@RequiredArgsConstructor`
7. **Controller**：`@Api("xxx")`、`@Permission`、`@Validated`、`@RequiredArgsConstructor`，每个 `@RequestBody` 加 `@Valid`
8. **CodeRuleField**（如需编码）：新增枚举值，`key` 全局唯一
9. **GlobalExceptionHandler**：无需改动，已覆盖所有常见异常
10. **编译验证**：`mvn -DskipTests compile`
