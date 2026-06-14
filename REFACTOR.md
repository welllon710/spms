# SPMS 重构报告

## 重构依据

对标以下优秀 Java 开源项目的实践：

- **Spring Boot / Spring Framework 官方示例** — 强类型参数、显式字段赋值、事务边界
- **MyBatis-Plus 官方示例** — 原子 SQL、Wrapper 用法
- **阿里巴巴 Java 开发手册** — 禁止用 `Double` 存储金额、禁止字段污染更新
- **Effective Java（Joshua Bloch）** — 消除重复代码、方法职责单一

发现的核心代码异味：

| 异味类型 | 具体表现 |
|---------|---------|
| **Bug** | `updateById(request)` 直接写入前端原始对象，污染 id/createTime/状态等字段 |
| **Bug** | `requireId` 校验数量（Long 型）导致合法数量被误拒 |
| **并发缺陷** | SELECT + UPDATE 两步操作之间存在库存数量竞态 |
| **类型精度** | `Double` 存储数量/金额，浮点精度丢失 |
| **重复代码** | `initAddBaseEntity`、`initUpdateBaseEntity`、`getId(Map)`、`requirePositiveQuantity` 在3个 Service 里各自复制一份 |
| **弱类型接口** | `getDetail(Map<String, Object>)` 无类型约束，运行时才报错 |
| **意图模糊** | `BeanUtils.copyProperties` 隐式复制所有字段，难以 review |
| **安全风险** | 数据库密码、JWT 密钥硬编码在 application.yml |

---

## 已完成

### P1 — 修复 PurchaseServiceImpl 的 Bug
**文件**：`PurchaseServiceImpl`

- `addFinish`：`requireId(request.quantity())` 错误地把数量当 id 校验 → 改用 `requirePositiveQuantity`
- `addFinish`：`purchaseDetailMapper.updateById(List)` 不存在此签名，导致编译报错 → 改为逐条 update
- `addFinish`：`BigDecimal.valueOf(entity.getFinishQuantity())` 当 `finishQuantity=null` 时 NPE → 加 null 保护
- `audit`/`reject`：`purchaseMapper.updateById(request)` 字段污染 → 改为构造最小 update 对象
- `audit`：错误码用 `PARAM_MISSING` → 改为语义正确的 `PARAM_INVALID`
- `getDetail`：`(String) request.get("id")` 强转 ClassCastException → 改为 `parseIdFromMap`

### P2 — 消除重复私有方法
**文件**：`BaseService`、`ParamUtils`、`InputServiceImpl`、`OutputServiceImpl`、`MoveServiceImpl`

- `initAddBaseEntity(BaseEntity)` / `initUpdateBaseEntity(BaseEntity)` 提升至 `BaseService`
- `requirePositiveQuantity(BigDecimal, String)` 移入 `ParamUtils` 静态方法
- `parseIdFromMap(Map<String, Object>)` 移入 `BaseService`
- 删除各 Service Impl 中的私有副本

### P3 — 强类型化接口参数，消除 Map 入参
**文件**：`IdRequest`（新建）、Input/Output/Purchase/Sale/Move 的 Service 接口、Impl、Controller

- 新建 `com.spms.base.IdRequest` record
- 所有 `getDetail(Map<String, Object>)` → `getDetail(IdRequest)`
- `PurchaseService.add(PurchasePageFilter)` → 新建 `PurchaseAddRequest` record，分离查询过滤器与新增请求职责

### P4 — 替换 BeanUtils.copyProperties，改为显式字段赋值
**文件**：`InputServiceImpl`、`OutputServiceImpl`、`PurchaseServiceImpl`、`SaleServiceImpl`、`MoveServiceImpl`

- `BeanUtils.copyProperties(request, entity)` 全部替换为显式 `entity.setXxx(request.getXxx())` 赋值
- 避免隐式带入 `id`、`createTime`、`isPublished`、`details` 等非预期字段
- 移除所有无用 `import org.springframework.beans.BeanUtils`

### P5 — 库存操作并发安全
**文件**：`InventoryMapper`、`InputServiceImpl`、`OutputServiceImpl`

- 新增 `increaseQuantity(@Update 原子 SQL)` — `quantity = quantity + #{quantity}`，返回影响行数
- 新增 `decreaseQuantity(@Update 原子 SQL)` — `quantity = quantity - #{quantity} WHERE quantity >= #{quantity}`，0 行影响即库存不足
- 替换 SELECT + UPDATE 两步操作，消除并发竞态
- 注：`increaseQuantity` 在记录不存在时回退到 INSERT，建议在 `(material_id, storage_id, type)` 上建唯一索引

### P6 — 数量/金额字段类型统一为 BigDecimal
**实体（9个）**：`InputDetailEntity`、`OutputDetailEntity`、`MoveDetailEntity`、`InventoryEntity`、`PurchaseDetailEntity`、`PurchaseEntity`、`SaleDetailEntity`、`SaleEntity`、`SalePriceEntity`、`PurchasePriceEntity`

**请求 record（4个）**：`InputFinishRequest`、`OutputFinishRequest`、`MoveFinishRequest`、`PurchaseFinishRequest`

- 所有 `Double quantity/price/finishQuantity/totalPrice` → `BigDecimal`
- `ParamUtils.requirePositiveQuantity` 签名改为 `(BigDecimal, String)`
- 消除所有 `BigDecimal.valueOf(x == null ? 0D : x)` 和 `.doubleValue()` 转换
- `InventoryMapper` 原子方法参数类型同步更新为 `BigDecimal`

### P7 — 配置文件凭据安全化
**文件**：`application.yml`

- 数据库 URL / 用户名 / 密码改为 `${DB_URL}`、`${DB_USERNAME}`、`${DB_PASSWORD}` 占位符
- JWT 密钥改为 `${JWT_SECRET}` 占位符
- 本地开发默认值保留在注释中，不提交到版本库

---

## 未完成 / 待办

### 🔴 必须完成

| 项目 | 说明 |
|-----|-----|
| **编译验证** | 改动涉及 30+ 文件，未跑过 `mvn -DskipTests compile` 验证零编译错误 |
| **DB 唯一索引** | `InventoryMapper.increaseQuantity` 并发安全依赖 `(material_id, storage_id, type)` 唯一索引，需补 DDL/迁移脚本 |

### 🟡 建议后续处理

| 项目 | 说明 |
|-----|-----|
| `SaleService.add` 参数 | 当前仍用 `SalePageFilter`，应与 Purchase 一样新建 `SaleAddRequest` 分离职责 |
| `MoveServiceImpl` 库存移库并发 | `moveInventory` 仍用 SELECT+UPDATE 两步，应与 Input/Output 一样改为原子 SQL |
| `PurchaseServiceImpl.audit/reject` 未加 `initUpdateEntity` | 状态流转缺少 `updateTime` 更新 |
| 单元测试覆盖 | 当前仅有 `RoleServiceImplTest`，P1–P6 改动缺少测试回归 |
| `SaleServiceImpl.saveDetails` 批量插入 | 当前逐条 `insert`，明细多时性能差，建议用 `insertBatchSomeColumn` |

---

## 编译命令

```bash
env JAVA_HOME=/Users/ww/Library/Java/JavaVirtualMachines/ms-17.0.17/Contents/Home \
  mvn -q -DskipTests compile
```
