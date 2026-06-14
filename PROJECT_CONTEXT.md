# PROJECT_CONTEXT.md

> 本文档供新会话快速理清项目现状。结合 `CLAUDE.md`（规范/实践/注意事项）一起使用。

---

## 一、项目快照

- **名称**：spms（生产管理系统后端）
- **技术栈**：Spring Boot 3.3.5 / Java 17 / MyBatis-Plus 3.5.12 / MySQL / Redis / Lombok / JWT (auth0/java-jwt) / Jakarta Bean Validation / JUnit 5 + Mockito
- **服务端口**：18080，context-path：`/v1`
- **构建命令**：
  ```bash
  env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -DskipTests compile
  ```

---

## 二、模块结构（按业务域）

```
com.spms
├── base/        BaseEntity, BaseService<E>, ApiController, PageQuery<T>,
│                PageParams, PageResult, IdRequest, RejectRequest, SortParam, Api
├── common/
│   ├── config/        EntityMetaObjectHandler, AppProperties
│   ├── exception/     AppException, CommonError, GlobalExceptionHandler
│   ├── result/        Json<T>, PageResult<T>, ResultCode
│   ├── security/      TokenService, LoginSessionService, RequestInterceptor,
│                      Permission, AuthIgnore, PermissionUtil
│   └── util/          ParamUtils, QueryParams, TreeUtils
├── personal/    用户、角色、部门、菜单、权限、组织单元
├── asset/       设备、物料（IoT 资产主数据）
├── iot/         采集参数（ParameterEntity）
├── channel/     客户、供应商、采购单、销售单、价格表
├── wms/         入库、出库、移库、库存、存储位
└── system/      编码规则（CodeRuleService）、系统配置
```

每个业务包内都有 `controller / entity / mapper / model / service / service/impl` 子包。

---

## 三、关键架构决策（已落地）

### 3.1 公共字段自动填充（MetaObjectHandler）

`BaseEntity` 字段 `createTime / updateTime / isDisabled / isPublished` 由 `EntityMetaObjectHandler` 在 MyBatis 层自动填充。

- `BaseService` 仅保留 `checkEditable` + 分页参数提取
- 所有 ServiceImpl **不再有** `initAddEntity / initUpdateEntity / initAddBaseEntity / initUpdateBaseEntity` 调用
- `BaseEntity` 字段加了 `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)` 注解

### 3.2 强类型请求 DTO（record）

add/update/audit/reject 的请求统一用 record DTO，**禁止用 Entity 接收请求**：

| 模块 | Add/Update DTO | 通用 DTO |
|---|---|---|
| Purchase | `PurchaseAddRequest` / `PurchaseUpdateRequest` / `PurchaseFinishRequest` | `IdRequest`（audit/getDetail）<br>`RejectRequest`（reject） |
| Sale | `SaleAddRequest` / `SaleUpdateRequest` | 同上 |
| Input | `InputAddRequest` / `InputUpdateRequest` / `InputFinishRequest` | 同上 |
| Output | `OutputAddRequest` / `OutputUpdateRequest` / `OutputFinishRequest` | 同上 |
| Move | `MoveAddRequest` / `MoveUpdateRequest` / `MoveFinishRequest` | 同上 |

`personal / asset / iot` 模块**尚未**完成强类型 DTO 改造，仍用 Entity 接收请求。

### 3.3 参数校验

- **Controller 边界**：`@Validated` 类 + `@Valid @RequestBody` 参数 + record/Entity 字段上的 Jakarta 注解（`@NotNull / @NotBlank / @NotEmpty / @Positive / @DecimalMin / @Email / @Valid`）
- **Service 内部**：仅对 DB 查询结果或派生值用 `ParamUtils.requireId/requireText/requirePositiveQuantity`
- **统一异常处理**：`GlobalExceptionHandler` 已覆盖 `MethodArgumentNotValidException` / `ConstraintViolationException` / `AppException` / 各类 Spring/MyBatis/Redis 异常

### 3.4 单据状态流转

| 模块 | 流转链 |
|---|---|
| 采购 | AUDITING → PURCHASING → IN_STORAGE \| REJECTED |
| 销售 | AUDITING → OUT_STORAGE \| REJECTED |
| 入库 | AUDITING → INPUTTING → FINISHED \| REJECTED |
| 出库 | AUDITING → OUTPUTTING → FINISHED \| REJECTED |
| 移库 | AUDITING → MOVING → FINISHED \| REJECTED |

`update / audit / reject` 都前置校验状态，仅 AUDITING 可操作。状态变更**构造新 entity 再 update**，不修改 exist。

### 3.5 安全机制

`RequestInterceptor` 拦截每次请求：
1. `@AuthIgnore` 公开 / `@Permission(login=true, authorize=false)` 仅登录 / `@Permission`（默认）登录+权限
2. JWT (HMAC256) 校验 → Redis 单点登录比对（key = `login_token_{userId}`）
3. 权限标识 = 类名 + 方法名（如 `Role_getPage`），`userId=1` 跳过校验
4. 权限/菜单缓存 key = `user_permission_{userId}` / `user_menu_{userId}`
5. 密码：`SHA1(SHA1(password+salt) + SHA1(salt+password))`

### 3.6 库存原子操作

`InputServiceImpl / OutputServiceImpl / MoveServiceImpl.addFinish` 通过 SQL 原子操作（`InventoryMapper.increaseQuantity / decreaseQuantity / decreaseForMove / increaseForMove`）增减库存。先 update，行数为 0 时再 insert。

**前提**：`inventory(material_id, storage_id, type)` 上有唯一索引（迁移脚本：`src/main/resources/db/V1__inventory_unique_index.sql`）。

### 3.7 编码生成

业务编码统一调 `CodeRuleService#createCode(CodeRuleField.XXX)`：

```
CodeRuleField:
  SUPPLIER_CODE(2)、CUSTOMER_CODE(5)、DEVICE_CODE(6)、
  PURCHASE_BILL_CODE(8)、SALE_BILL_CODE(9)、INPUT_BILL_CODE(10)、
  OUTPUT_BILL_CODE(11)、MOVE_BILL_CODE(12)、STORAGE_CODE(13, "ST", DAY)、
  MATERIAL_CODE(14, "MT", YEAR)、UNIT_CODE(15, "UN", YEAR)
```

**注意**：之前 `STORAGE_CODE` 与 `DEVICE_CODE` 都是 key=6（已修复为 13）。新增枚举时 `key` 必须全局唯一，当前最大 key=15。

---

## 四、核心实体与表（推断）

所有表含 `id, create_time, update_time, is_disabled, is_published`（继承自 `BaseEntity`）。

| 表 | 关键字段 |
|---|---|
| `user` | nickname, real_name, email, phone, gender, id_card, password, salt, avatar |
| `role` | name, code |
| `department` | name, code, parent_id, order_no（树） |
| `menu` | name, parent_id, path, component, icon, order_no（树） |
| `permission` | name, code, parent_id（树） |
| `unit` | name, code |
| `device` | name, code, uuid, status, alarm, part_count, is_reporting, rate |
| `material` | code, name, spc, material_type, use_type, unit_id, **purchase_price (BigDecimal), sale_price (BigDecimal)** |
| `customer / supplier` | name, code |
| `purchase / sale` | bill_code, reason, status, total_price, [customer_id], reject_reason |
| `purchase_detail / sale_detail` | bill_id, price, quantity, finish_quantity, is_finished, material_id, [supplier_id] |
| `purchase_price / sale_price` | material_id, [supplier_id\|customer_id], price |
| `input / output / move` | bill_code, status, [type, move_id, ...], reject_reason |
| `input_detail / output_detail / move_detail` | bill_id, quantity, finish_quantity, is_finished, [material_id, inventory_id] |
| `inventory` | material_id, quantity, type, storage_id, structure_id（**唯一索引**：material_id+storage_id+type） |
| `storage` | code, name, parent_id（树） |
| `code_rule` | key, prefix, sn_type, current_sn, ... |

---

## 五、近期完成的重构（按时间倒序）

### Round 1 — 注解替代 ParamUtils 校验
- 新建 `RejectRequest`、`MoveEntity.storageId` 加 `@NotNull`
- 5 个 Service 接口的 `reject` 改用 `RejectRequest`
- 5 个 Controller 的 `reject` 加 `@Valid`
- 删除 ServiceImpl 中 Category A 的冗余 `requireNotNull/requireId/requireText`

### Round 2 — 项目熟悉 + 修复
- `SaleServiceImpl.update` 加 `resolveUpdateBillCode` 保护
- 删除 `SaleServiceImpl.validateDetails` 死代码
- `MaterialEntity.purchasePrice/salePrice` 改 `BigDecimal`
- 删除 `BaseService.parseIdFromMap` 死代码
- `PurchaseServiceImpl.finishPurchaseIfAllDetailsFinished` 改用 `initUpdateEntity`
- `StorageService.getById` 改强类型 `IdRequest`

### Round 3 — audit / update DTO 强类型化
- 5 个 Service 的 `audit` 改用 `IdRequest`
- `Sale/Purchase/Input/Output/Move` 的 `add/update` 全部改用专用 record DTO
- 删除 `validateOutput / validateMove`（被 `@Valid` + 注解替代）

### Round 4 — 阿里规范扫描修复
- `PurchaseController` 4 个裸类型 `Json` 加泛型 + 删除错误字符串参数
- `StorageServiceImpl.updateById` 修复 `excludeId=null` bug
- `SaleServiceImpl.update` 补状态校验
- `PurchaseServiceImpl.add/update` detail 加 `initAddBaseEntity`（已被 R5 替代）
- 5 个 ServiceImpl 整理 import 顺序

### Round 5 — MetaObjectHandler 接管公共字段
- 新建 `EntityMetaObjectHandler`
- `BaseEntity` 加 `@TableField(fill = ...)`
- 删除 `BaseService` 所有 `init*` 方法
- 删除全项目 16 个 ServiceImpl 中的 `init*Entity` 调用

### Round 6 — 第二轮异味清理
- `CodeRuleField.STORAGE_CODE` key 6→13（修复重复 bug）
- `calculateTotalPrice` 补 price/quantity 空值保护
- 删除 `validateOutput / validateMove / fillUnchangedUserFields` 死代码
- `SalePriceServiceImpl` 错误消息"采购物料"→"销售物料"
- `PurchaseServiceImpl.add/update` 提取共用 `saveDetails`
- `PurchaseServiceImpl.audit/reject` 补 `@Transactional`
- `UserServiceImpl` 注入方式统一 `@RequiredArgsConstructor` + final
- `StorageServiceImpl` 改 `@RequiredArgsConstructor`
- 删除 `RoleServiceImpl` 注释代码

### Round 7 — 需求补齐（主数据 + 仓库 + 库存）
- `CodeRuleField` 新增 `MATERIAL_CODE(14, "MT", YEAR)` / `UNIT_CODE(15, "UN", YEAR)`，修正 `STORAGE_CODE` 前缀 SR→ST
- `MaterialServiceImpl` — `code` 为空自动生成 `MATERIAL_CODE`，补 `unitId` 必填校验，`purchasePrice/salePrice` 空值默认 `BigDecimal.ZERO`
- `UnitServiceImpl` — `code` 为空自动生成 `UNIT_CODE`，删除前校验 `material.unit_id` 引用，注入 `MaterialMapper`
- `StorageServiceImpl` — 新增 `delete` 方法，删除前检查子节点（`parentId`）和库存引用（`storage_id`）；`StorageService` 接口和 `StorageController` 同步新增 `delete` 端点
- `InventoryService` — 新增 `getDetail(Long id)` 接口；`InventoryServiceImpl` 实现；`InventoryController` 新增 `getDetail` 端点；修复 `@AllArgsConstructor` → `@RequiredArgsConstructor`

每轮都跑过 `mvn -DskipTests compile` 验证通过。

---

## 六、当前已知遗留项

### 高优先级
- `application.yml` 数据库密码、JWT 密钥仍明文，建议改环境变量
- 缺少日志：库存增减、状态流转、登录登出等关键操作均无日志

### 中优先级
- `personal / asset / iot / system` 模块**尚未**应用强类型 DTO 改造（仍用 Entity 接收 add/update 请求）
- `SaleServiceImpl.saveDetails` 逐条 insert，可改批量 `insertBatchSomeColumn`
- 跨 Service 的重复代码（`resolveBillCode / saveDetails / finishXxxIfAllDetails / getRequiredXxx`）可考虑抽象，但需谨慎避免过度封装
- `SalePageFilter` 噪音字段（`details / detailList`）应清理
- `TokenService.catch (Exception)` 过宽

### 低优先级
- `DeviceEntity.status` 是裸 Integer，缺枚举（值 4=关机）
- 部分 Mapper 用 XML，部分用 Wrapper，风格不统一（不建议强行统一）

---

## 七、新会话快速上手清单

1. **读这个文档**，了解整体现状
2. **读 `CLAUDE.md`**，掌握规范、实践、禁止事项
3. **运行编译**确认环境 OK：
   ```bash
   env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home mvn -q -DskipTests compile
   ```
4. **如修改业务模块**，先看同类已重构模块（推荐 `PurchaseServiceImpl` / `SaleServiceImpl` / `InputServiceImpl` 作为模板）
5. **新增模块**按 `CLAUDE.md` 第四节"新增模块开发清单"10 步执行
6. **任何改动后**必须跑编译，编译通过再交付

---

## 八、参考文件位置

- 规范文档：`CLAUDE.md`
- 重构记录：`REFACTOR.md`
- 上下文文档：`PROJECT_CONTEXT.md`（本文件）
- 库迁移脚本：`src/main/resources/db/`
- MyBatis XML：`src/main/resources/mapper/`
- 全局异常处理：`src/main/java/com/spms/common/exception/GlobalExceptionHandler.java`
- 字段自动填充：`src/main/java/com/spms/common/config/EntityMetaObjectHandler.java`
- 安全拦截器：`src/main/java/com/spms/common/security/RequestInterceptor.java`
