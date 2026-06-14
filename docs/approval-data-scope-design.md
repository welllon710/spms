# 审批流与数据隔离需求设计

本文档用于补充当前项目中涉及审核功能的设计边界。当前项目已有“审核/驳回”动作，但本质是单据状态流转，还没有完整审批流和业务数据隔离。

## 现状

当前已涉及审核动作的模块：

| 模块 | 主表 | 当前审核行为 |
| --- | --- | --- |
| 采购单 | `purchase` | `审核中` 通过后进入 `采购中`，驳回后进入 `已驳回` |
| 销售单 | `sale` | `审核中` 通过后进入 `出库中`，驳回后进入 `已驳回` |
| 入库单 | `input` | `审核中` 通过后进入 `入库中`，驳回后进入 `已驳回` |
| 出库单 | `output` | `审核中` 通过后进入 `出库中`，驳回后进入 `已驳回` |
| 移库单 | `move` | 文档已规划，后续需要同样支持审核 |

当前已有权限控制：

- Controller 使用 `@Permission`。
- `RequestInterceptor` 校验登录态和接口权限。
- 用户通过角色获得权限列表。

当前缺失能力：

- 没有审批节点、审批任务、审批记录。
- 没有记录审核人、审核时间、提交人、提交部门。
- 没有按部门、本人、角色范围过滤业务数据。
- 有接口权限的人理论上可以查询或审核所有同类单据。

## 设计目标

第一版目标：

1. 保留当前简单审核动作，不立刻引入复杂流程引擎。
2. 补齐单据创建人、创建部门、审核人、审核时间、驳回人、驳回时间。
3. 实现基础数据隔离：全部、本人、本部门、本部门及下级、自定义部门。
4. 查询、详情、审核、修改、删除都要受数据范围控制。
5. 后续可以平滑升级到完整审批流。

第二版目标：

1. 增加审批定义、审批节点、审批实例、审批任务、审批记录。
2. 支持按业务类型配置审批流。
3. 支持指定用户、指定角色、部门负责人等审批人规则。
4. 审批通过后由审批流回调业务单据状态。

## 关联表

### 已有关联表

| 表 | 作用 |
| --- | --- |
| `user` | 当前登录用户、建单人、审核人、驳回人 |
| `department` | 用户所属部门、单据所属部门、数据范围过滤 |
| `user_department_list` | 用户与部门关系 |
| `role` | 用户角色，后续承载数据范围配置 |
| `user_role_list` | 用户与角色关系 |
| `permission` | 接口权限 |
| `role_permission_list` | 角色与接口权限关系 |
| `purchase` | 采购单审核 |
| `sale` | 销售单审核 |
| `input` | 入库单审核 |
| `output` | 出库单审核 |
| `move` | 移库单审核，后续实现 |

### 第一版建议新增表

第一版不强制新增审批流表，但建议新增一张通用审核记录表，用于留痕。

#### `audit_record`

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `business_type` | varchar(64) | 业务类型，如 `purchase`、`sale`、`input`、`output`、`move` |
| `business_id` | bigint | 业务单据 ID |
| `business_code` | varchar(64) | 单据编号，冗余方便查询 |
| `action` | varchar(32) | 操作：`submit`、`audit`、`reject`、`cancel` |
| `from_status` | int | 操作前状态 |
| `to_status` | int | 操作后状态 |
| `comment` | varchar(512) | 审核意见或驳回原因 |
| `operator_id` | bigint | 操作人 ID |
| `operator_name` | varchar(64) | 操作人名称，冗余留痕 |
| `department_id` | bigint | 操作人部门 ID |
| `create_time` | bigint | 操作时间 |

用途：

1. 保留每次审核和驳回记录。
2. 详情页可以展示审核历史。
3. 后续升级审批流时，可迁移为 `approval_record` 的基础数据。

### 第二版审批流表

如果后续需要真正审批流，建议新增以下表。

#### `approval_definition`

审批流定义表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `code` | varchar(64) | 流程编码，如 `purchase_audit` |
| `name` | varchar(128) | 流程名称 |
| `business_type` | varchar(64) | 业务类型 |
| `version` | int | 版本号 |
| `is_enabled` | tinyint | 是否启用 |
| `create_time` | bigint | 创建时间 |
| `update_time` | bigint | 更新时间 |
| `is_disabled` | tinyint | 是否禁用 |
| `is_published` | tinyint | 是否发布 |

#### `approval_node`

审批节点表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `definition_id` | bigint | 审批定义 ID |
| `node_code` | varchar(64) | 节点编码 |
| `node_name` | varchar(128) | 节点名称 |
| `node_type` | varchar(32) | 节点类型：`start`、`approve`、`end` |
| `approve_type` | varchar(32) | 审批方式：`any`、`all` |
| `approver_type` | varchar(32) | 审批人类型：`user`、`role`、`department_manager` |
| `approver_user_id` | bigint | 指定审批人 |
| `approver_role_id` | bigint | 指定审批角色 |
| `department_id` | bigint | 指定审批部门 |
| `order_no` | int | 节点顺序 |
| `reject_type` | varchar(32) | 驳回方式：结束、退回上一节点、退回发起人 |
| `create_time` | bigint | 创建时间 |
| `update_time` | bigint | 更新时间 |

#### `approval_instance`

审批实例表，一张业务单据对应一次审批实例。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `definition_id` | bigint | 审批定义 ID |
| `business_type` | varchar(64) | 业务类型 |
| `business_id` | bigint | 业务单据 ID |
| `business_code` | varchar(64) | 单据编号 |
| `status` | varchar(32) | 状态：`running`、`approved`、`rejected`、`canceled` |
| `current_node_id` | bigint | 当前节点 ID |
| `apply_user_id` | bigint | 发起人 |
| `apply_department_id` | bigint | 发起部门 |
| `start_time` | bigint | 发起时间 |
| `finish_time` | bigint | 完成时间 |
| `create_time` | bigint | 创建时间 |
| `update_time` | bigint | 更新时间 |

#### `approval_task`

审批任务表，表示某个节点下待某人处理的任务。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `instance_id` | bigint | 审批实例 ID |
| `node_id` | bigint | 审批节点 ID |
| `status` | varchar(32) | 状态：`pending`、`approved`、`rejected`、`canceled` |
| `assignee_user_id` | bigint | 指定处理人 |
| `assignee_role_id` | bigint | 指定处理角色 |
| `claim_user_id` | bigint | 实际认领/处理人 |
| `approve_time` | bigint | 处理时间 |
| `comment` | varchar(512) | 审批意见 |
| `create_time` | bigint | 创建时间 |
| `update_time` | bigint | 更新时间 |

#### `approval_record`

审批流操作记录表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `instance_id` | bigint | 审批实例 ID |
| `task_id` | bigint | 审批任务 ID |
| `node_id` | bigint | 审批节点 ID |
| `action` | varchar(32) | 操作：`submit`、`approve`、`reject`、`cancel`、`transfer` |
| `operator_id` | bigint | 操作人 |
| `operator_name` | varchar(64) | 操作人名称 |
| `comment` | varchar(512) | 处理意见 |
| `create_time` | bigint | 操作时间 |

## 需要增加的字段

### 建议加入 `BaseEntity`

这些字段用于所有需要数据隔离的业务表。是否直接放入 `BaseEntity`，需要结合数据库改表范围决定；如果担心影响太大，可以先只加在单据主表。

| Java 字段 | 数据库字段 | 类型建议 | 说明 |
| --- | --- | --- | --- |
| `createUserId` | `create_user_id` | bigint | 创建人 |
| `updateUserId` | `update_user_id` | bigint | 最后修改人 |
| `departmentId` | `department_id` | bigint | 数据所属部门 |

可选预留：

| Java 字段 | 数据库字段 | 类型建议 | 说明 |
| --- | --- | --- | --- |
| `tenantId` | `tenant_id` | bigint | 租户 ID，当前项目不是多租户，第一版可不加 |

### 单据主表建议增加字段

适用表：

- `purchase`
- `sale`
- `input`
- `output`
- `move`

| Java 字段 | 数据库字段 | 类型建议 | 说明 |
| --- | --- | --- | --- |
| `submitUserId` | `submit_user_id` | bigint | 提交审核人 |
| `submitTime` | `submit_time` | bigint | 提交审核时间 |
| `auditUserId` | `audit_user_id` | bigint | 审核通过人 |
| `auditTime` | `audit_time` | bigint | 审核通过时间 |
| `rejectUserId` | `reject_user_id` | bigint | 驳回人 |
| `rejectTime` | `reject_time` | bigint | 驳回时间 |
| `rejectReason` | `reject_reason` | varchar(512) | 驳回原因，部分表已有 |

说明：

1. 当前 `input`、`output` 已有 `rejectReason`。
2. 采购单、销售单如果也支持驳回原因，应补 `reject_reason`。
3. `submitUserId/submitTime` 第一版可以在新增单据时默认等于创建人和创建时间。

### `role` 表建议增加字段

用于配置角色数据范围。

| Java 字段 | 数据库字段 | 类型建议 | 说明 |
| --- | --- | --- | --- |
| `dataScope` | `data_scope` | varchar(32) | 数据范围：`all`、`self`、`department`、`department_and_children`、`custom_department` |

可选新增关系表：

#### `role_department_scope_list`

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `role_entity_id` | bigint | 角色 ID |
| `department_list_id` | bigint | 可访问部门 ID |

用途：当角色 `data_scope = custom_department` 时，读取该关系表作为数据范围。

## 数据隔离规则

### 数据范围枚举

| 值 | 含义 |
| --- | --- |
| `all` | 全部数据 |
| `self` | 仅本人创建的数据 |
| `department` | 本部门数据 |
| `department_and_children` | 本部门及下级部门数据 |
| `custom_department` | 自定义部门数据 |

### 多角色合并规则

用户可能拥有多个角色，数据范围按最大范围合并：

1. 任一角色为 `all`，则拥有全部数据。
2. `department_and_children` 大于 `department`。
3. `custom_department` 与部门范围取并集。
4. `self` 只补充本人创建数据。

### 应用位置

数据隔离必须作用在这些操作上：

| 操作 | 规则 |
| --- | --- |
| 分页查询 | SQL 增加数据范围条件 |
| 详情查询 | 查询后校验当前用户是否可访问 |
| 修改 | 校验当前用户是否可编辑该单据 |
| 删除 | 校验当前用户是否可删除该数据 |
| 审核/驳回 | 校验当前用户是否可访问且有审核权限 |
| 执行入库/出库 | 校验当前用户是否可操作该单据 |

## 第一版接口调整建议

现有接口保持不变：

- `POST /purchase/audit`
- `POST /purchase/reject`
- `POST /sale/audit`
- `POST /sale/reject`
- `POST /input/audit`
- `POST /input/reject`
- `POST /output/audit`
- `POST /output/reject`

第一版只增强行为：

1. 审核通过时写入 `auditUserId`、`auditTime`。
2. 驳回时写入 `rejectUserId`、`rejectTime`、`rejectReason`。
3. 写入 `audit_record`。
4. 操作前检查当前用户数据范围。

第二版再新增审批流接口：

| 接口 | 说明 |
| --- | --- |
| `POST /approvalDefinition/getPage` | 查询审批流定义 |
| `POST /approvalDefinition/add` | 新增审批流定义 |
| `POST /approvalDefinition/update` | 修改审批流定义 |
| `POST /approvalDefinition/delete` | 删除审批流定义 |
| `POST /approval/submit` | 提交审批 |
| `POST /approval/audit` | 审批通过 |
| `POST /approval/reject` | 审批驳回 |
| `POST /approval/getMyTaskPage` | 我的待办 |
| `POST /approval/getRecordList` | 审批记录 |

## Service 设计建议

第一版建议新增两个通用服务。

### `DataScopeService`

职责：

1. 根据当前用户查询角色数据范围。
2. 计算可访问部门 ID 列表。
3. 构造查询参数，例如 `createUserId`、`departmentIds`。
4. 提供详情、审核、修改前的数据访问校验。

建议方法：

```java
DataScope getCurrentUserScope(long userId);

Map<String, Object> buildScopeParams(long userId);

void requireAccessible(String businessType, Long businessId, long userId);
```

### `AuditRecordService`

职责：

1. 保存审核、驳回、提交记录。
2. 查询某个业务单据的审核记录。

建议方法：

```java
void record(AuditRecordEntity record);

List<AuditRecordEntity> getByBusiness(String businessType, Long businessId);
```

## 落地顺序

### 第一阶段：补字段和留痕

1. 业务主表补 `create_user_id`、`update_user_id`、`department_id`。
2. 单据主表补 `audit_user_id`、`audit_time`、`reject_user_id`、`reject_time`。
3. 采购单、销售单补 `reject_reason`。
4. 新增 `audit_record` 表。
5. 新增 `AuditRecordEntity`、Mapper、Service。
6. 改造现有 `audit/reject` 方法写入审核字段和审核记录。

### 第二阶段：数据隔离

1. `role` 增加 `data_scope`。
2. 如需要自定义部门范围，增加 `role_department_scope_list`。
3. 新增 `DataScopeService`。
4. 改造分页 XML 或 Wrapper 查询条件。
5. 改造详情、修改、审核、删除前的数据访问校验。

### 第三阶段：完整审批流

1. 新增 `approval_definition`、`approval_node`、`approval_instance`、`approval_task`、`approval_record`。
2. 将业务单据 `audit/reject` 逐步收敛到审批服务。
3. 业务单据只保留状态和结果字段。
4. 审批流通过后回调业务服务修改单据状态。

## 注意事项

1. 数据隔离比完整审批流更优先。没有数据隔离时，有审核接口权限的人可能操作所有单据。
2. 第一版不要把审批流做得过重，先保证留痕、审核人、审核时间和数据范围。
3. 数据范围条件要同时覆盖分页、详情和写操作，不能只做分页过滤。
4. 审核记录属于业务审计数据，不建议物理删除。
5. 后续如果引入多租户，再统一补 `tenant_id` 并在所有查询中作为最高优先级隔离条件。
