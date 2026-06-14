# 资产、库存与设备模块需求汇总

本文档汇总旧项目中“物料和设备资产管理”相关需求，并按当前 `spms` 后端项目的结构整理为后续开发规格。

当前项目已存在：

- `asset/material`：物料 CRUD 基础实现。
- `asset/device`：设备 CRUD 基础实现，支持设备参数绑定。
- `iot/parameter`：采集参数 CRUD 基础实现。
- `personal/unit`：单位 CRUD 基础实现。
- `wms/storage`：仓库 CRUD 基础实现。
- `wms/inventory`：库存分页查询实现，返回物料、单位、仓库信息。
- `wms/input`：入库单第一版流程已实现，包括新增、修改、详情、分页、审核、驳回、执行入库。
- `wms/output`：出库单第一版流程已实现，包括新增、修改、详情、分页、审核、驳回、执行出库。
- `wms/entity`：`move`、`move_detail` 仍以实体骨架为主。
- `system/coderule`：编码规则实体和轻量编码服务。

后续实现仍按当前项目约定：新增业务模块优先使用 MyBatis-Plus，分页使用 `PageQuery<T>`、`Page<T>` / `IPage<T>` 和 `PageResult<T>`。`PageQuery<T>` 需要兼容顶层 `pageNum/pageSize` 和嵌套 `page.pageNum/pageSize` 两种前端入参。

## 模块边界

资产相关能力拆成三层：

| 层级 | 模块 | 说明 |
| --- | --- | --- |
| 主数据 | `asset`、`personal/unit` | 物料、设备、单位、仓库等基础资料 |
| 库存 | `wms` | 库存、入库、出库、移库，负责数量变化 |
| 采集 | `device`、`iot` | 设备采集配置、实时数据、历史数据 |

第一版建议先打通主数据和库存流转；设备采集依赖 Redis、InfluxDB、MQTT、IoT 参数表，可以作为第二版。

## 关联表

| 表名 | 作用 | 备注 |
| --- | --- | --- |
| `unit` | 单位 | 当前项目已有 |
| `material` | 物料主数据 | 当前项目已有 CRUD |
| `device` | 设备主数据 | 当前项目已有 CRUD |
| `parameter` | 设备采集参数 | 当前项目已有 CRUD，属于 IoT 参数 |
| `device_parameter` | 设备与参数多对多 | 当前项目已有绑定关系维护 |
| `storage` | 仓库/库位 | 当前已有基础 CRUD 和树形列表 |
| `inventory` | 库存 | 当前已有分页查询，执行入库时会新增或累加库存 |
| `input` | 入库单 | 当前已实现第一版流程 |
| `input_detail` | 入库明细 | 当前已实现第一版流程 |
| `output` | 出库单 | 当前已实现第一版流程 |
| `output_detail` | 出库明细 | 当前已实现第一版流程 |
| `move` | 移库单 | 待实现 |
| `move_detail` | 移库明细 | 待实现 |
| `coderule` | 编码规则 | 当前项目已有轻量服务 |
| `config` | 系统配置 | 当前仅有实体骨架 |

## 编码规则

这些业务编码均遵循当前项目轻量 `CodeRuleService` 的规则：字段值为空时自动生成；前端传值时保留前端传入值。

| 业务 | 规则字段 | 默认前缀 | 重置方式 | 模板 | 流水长度 | 示例 |
| --- | --- | --- | --- | --- | ---: | --- |
| 物料编码 | `MaterialCode` | `MT` | 年 | `yyyy` | 4 | `MT20260001` |
| 单位编码 | `UnitCode` | `UN` | 年 | `yyyy` | 4 | `UN20260001` |
| 设备编码 | `DeviceCode` | `DV` | 年 | `yyyy` | 4 | `DV20260001` |
| 仓库编码 | `StorageCode` | `ST` | 年 | `yyyy` | 4 | `ST20260001` |
| 入库单号 | `InputBillCode` | `IN` | 日 | `yyyymmdd` | 4 | `IN202606120001` |
| 出库单号 | `OutputBillCode` | `OUT` | 日 | `yyyymmdd` | 4 | `OUT202606120001` |
| 移库单号 | `MoveBillCode` | `MV` | 日 | `yyyymmdd` | 4 | `MV202606120001` |

说明：旧项目文档明确采购/销售单号规则，物料/单位/设备/仓库/出入库/移库编码在新项目中按同类业务规则归纳。若数据库中已有不同 `coderule` 配置，以数据库配置为准。

当前已落地：

- `CodeRuleField.DEVICE_CODE`：设备编码。
- `CodeRuleField.INPUT_BILL_CODE`：入库单号。
- `CodeRuleField.OUTPUT_BILL_CODE`：出库单号。
- 采购/销售单号相关编码规则在渠道模块中使用。

## 物料管理

接口前缀：`/material`

当前项目已有：分页、详情、新增、修改、删除，并且分页/详情已联查单位。

### 字段

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `name` | 物料名称 | 必填，唯一 |
| `code` | 物料编码 | 唯一，不传自动生成 |
| `spc` | 规格型号 | 可选 |
| `materialType` | 物料类型 | 默认可按 `1` |
| `useType` | 使用方式 | 默认可按 `1` |
| `unitId` | 默认单位 | 必填 |
| `purchasePrice` | 采购标准价 | 空值默认 `0` |
| `salePrice` | 销售标准价 | 空值默认 `0` |

### 枚举

物料类型：

| 值 | 含义 |
| ---: | --- |
| `1` | 自产品 |
| `2` | 外购品 |

物料使用方式：

| 值 | 含义 |
| ---: | --- |
| `1` | 工具类 |
| `2` | 消耗品 |

### 待补规则

- `code` 为空时生成 `MaterialCode`。
- `unitId` 必填校验。
- `purchasePrice`、`salePrice` 为空时默认 `0`。
- `name/code` 唯一性已做，但应确认自动编码后的唯一性异常提示。

## 单位管理

接口前缀：`/unit`

当前项目已有基础 CRUD。

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `name` | 单位名称 | 必填，唯一 |
| `code` | 单位编码 | 唯一，不传自动生成 |

待补规则：

- `code` 为空时生成 `UnitCode`。
- 删除单位时如果已被物料引用，应禁止删除。

## 仓库管理

接口前缀：`/storage`

仓库用于组织库存位置，支持树形结构。

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `name` | 仓库/库位名称 | 必填 |
| `code` | 仓库编码 | 唯一，不传自动生成 |
| `parentId` | 父级仓库 | 根节点可为空 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /storage/getList` | 查询完整仓库树 |
| `POST /storage/getDetail` | 查询详情 |
| `POST /storage/add` | 新增 |
| `POST /storage/update` | 修改 |
| `POST /storage/delete` | 删除 |

### 规则

- 仓库是树结构，不强行分页。
- 查询某仓库库存时，应包含其全部子仓库库存。
- 删除仓库前应检查是否存在子节点或库存引用。

## 库存管理

接口前缀：`/inventory`

库存表示“某物料在某个仓库或生产单元下的数量”。

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `materialId` | 物料 | 必填 |
| `quantity` | 库存数量 | 不允许小于 `0` |
| `type` | 库存类型 | `1` 仓库，`2` 生产单元 |
| `storageId` | 仓库 | 仓库库存必填 |
| `structureId` | 生产单元 | 生产单元库存必填 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /inventory/getPage` | 分页查询库存 |
| `POST /inventory/getDetail` | 查询库存详情 |

### 业务能力

库存模块对外提供内部服务方法：

| 方法 | 说明 |
| --- | --- |
| `increase(materialId, storageId, quantity)` | 入库或移库入目标仓时增加库存 |
| `decrease(inventoryId, quantity)` | 出库或移库出来源仓时扣减库存 |
| `getOrCreate(materialId, storageId)` | 目标库存不存在时创建 |

### 规则

- 扣减库存后不能小于 `0`，否则报“库存数量不足”。
- 仓库树过滤时包含子仓库。
- 生产单元树过滤时包含子生产单元；如果生产单元模块尚未实现，第一版可只支持仓库库存。

## 入库单

接口前缀：`/input`

当前项目已实现第一版普通入库流程：

- `InputController`
- `InputService` / `InputServiceImpl`
- `InputMapper` / `InputDetailMapper`
- `InputPageFilter`
- `InputFinishRequest`
- `InputStatus` / `InputType`

已实现范围包括：分页、详情、新增、修改、审核、驳回、执行入库并增加库存。

### 主表字段：`input`

| 字段 | 说明 |
| --- | --- |
| `billCode` | 入库单号，不传自动生成 |
| `status` | 入库状态 |
| `type` | 入库类型 |
| `purchaseId` | 采购入库关联采购单 |
| `structureId` | 退料/生产单元位置 |
| `orderId` | 生产订单 |
| `moveId` | 移库单 |
| `rejectReason` | 驳回原因 |

### 明细字段：`input_detail`

| 字段 | 说明 |
| --- | --- |
| `billId` | 入库单 ID |
| `materialId` | 入库物料 |
| `quantity` | 应入库数量 |
| `finishQuantity` | 已入库数量 |
| `isFinished` | 明细是否完成 |

### 状态

| 值 | 状态 |
| ---: | --- |
| `1` | 审核中 |
| `2` | 已驳回 |
| `3` | 入库中 |
| `4` | 已完成 |

### 类型

| 值 | 类型 |
| ---: | --- |
| `1` | 普通入库 |
| `2` | 移库入库 |
| `3` | 采购入库 |
| `4` | 生产入库 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /input/getPage` | 分页查询 |
| `POST /input/getDetail` | 查询详情，带明细和物料 |
| `POST /input/add` | 新增入库单 |
| `POST /input/update` | 修改入库单 |
| `POST /input/audit` | 审核通过，状态变入库中 |
| `POST /input/reject` | 驳回，记录驳回原因 |
| `POST /input/addFinish` | 执行入库，增加库存 |

### 请求模型

分页查询使用 `PageQuery<InputPageFilter>`：

| 字段 | 说明 |
| --- | --- |
| `billCode` | 按入库单号模糊查询 |
| `status` | 入库状态 |
| `type` | 入库类型 |
| `purchaseId` | 采购单 ID |
| `moveId` | 移库单 ID |

执行入库使用 `InputFinishRequest`：

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `id` | 入库明细 ID | 必填，不是入库单 ID |
| `quantity` | 本次入库数量 | 必填，必须大于 `0` |
| `storageId` | 目标仓库 ID | 与 `storage.id` 二选一 |
| `storage` | 目标仓库对象 | 支持读取 `storage.id` |

### 规则

- 新增必须带明细。
- `billCode` 为空时生成 `InputBillCode`。
- 新增状态默认 `审核中`。
- 只有 `审核中` 能审核或驳回。
- 修改只允许在未进入 `入库中` 或 `已完成` 前进行；修改后状态回到 `审核中`。
- 驳回必须传 `rejectReason`。
- 执行入库时请求中的 `id` 是明细 ID，不是单据 ID。
- 执行入库必须传入目标仓库。
- 本次入库数量累加到明细 `finishQuantity`。
- 本次入库数量必须大于 `0`。
- 累计 `finishQuantity` 不能超过明细 `quantity`。
- `finishQuantity >= quantity` 时明细完成。
- 所有明细完成后，入库单状态变 `已完成`。
- 库存不存在则创建，存在则增加数量。
- 当前第一版只处理仓库库存，库存类型固定为 `1`。
- 详情查询返回明细，明细会带 `material` 和 `material.unit`。
- 分页查询返回采购单/移库单的基础关联信息。
- 采购入库完成后回写采购单完成数量和状态，属于后续渠道联动阶段，当前未实现。

## 出库单

接口前缀：`/output`

当前项目已实现普通出库第一版流程：分页、详情、新增、修改、审核、驳回、执行出库。执行出库时会扣减来源库存，并在全部明细完成后将出库单置为已完成。

### 主表字段：`output`

| 字段 | 说明 |
| --- | --- |
| `billCode` | 出库单号，不传自动生成 |
| `status` | 出库状态 |
| `type` | 出库类型 |
| `saleId` | 销售出库关联销售单 |
| `moveId` | 移库单 |
| `pickingId` | 领料单 |
| `rejectReason` | 驳回原因 |

### 明细字段：`output_detail`

| 字段 | 说明 |
| --- | --- |
| `billId` | 出库单 ID |
| `inventoryId` | 来源库存 |
| `materialId` | 出库物料 |
| `quantity` | 应出库数量 |
| `finishQuantity` | 已出库数量 |
| `isFinished` | 明细是否完成 |

### 状态

| 值 | 状态 |
| ---: | --- |
| `1` | 审核中 |
| `2` | 已驳回 |
| `3` | 出库中 |
| `4` | 已完成 |

### 类型

| 值 | 类型 |
| ---: | --- |
| `1` | 普通出库 |
| `2` | 移库出库 |
| `3` | 销售出库 |
| `4` | 领料出库 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /output/getPage` | 分页查询 |
| `POST /output/getDetail` | 查询详情，带明细、库存和物料 |
| `POST /output/add` | 新增出库单 |
| `POST /output/update` | 修改出库单 |
| `POST /output/audit` | 审核通过，状态变出库中 |
| `POST /output/reject` | 驳回，记录驳回原因 |
| `POST /output/addFinish` | 执行出库，扣减库存 |

### 规则

- 新增必须带明细。
- `billCode` 为空时生成 `OutputBillCode`。
- 新增状态默认 `审核中`。
- 只有 `审核中` 能审核或驳回。
- 执行出库时请求中的 `id` 是明细 ID，不是单据 ID。
- 出库明细的 `inventoryId` 必填。
- 来源库存的物料必须与明细 `materialId` 一致。
- 扣减库存不能小于 `0`。
- 本次出库数量累加到明细 `finishQuantity`。
- `finishQuantity >= quantity` 时明细完成。
- 所有明细完成后，出库单状态变 `已完成`。
- 销售出库完成后，需要回写销售单完成数量和状态。当前属于后续渠道联动阶段，暂未实现。

## 移库单

接口前缀：`/move`

### 主表字段：`move`

| 字段 | 说明 |
| --- | --- |
| `billCode` | 移库单号，不传自动生成 |
| `status` | 移库状态 |
| `storageId` | 目标仓库 |
| `rejectReason` | 驳回原因 |

### 明细字段：`move_detail`

| 字段 | 说明 |
| --- | --- |
| `billId` | 移库单 ID |
| `inventoryId` | 来源库存 |
| `quantity` | 应移动数量 |
| `finishQuantity` | 已移动数量 |
| `isFinished` | 明细是否完成 |

### 状态

| 值 | 状态 |
| ---: | --- |
| `1` | 审核中 |
| `2` | 已驳回 |
| `3` | 移动中 |
| `4` | 已完成 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /move/getPage` | 分页查询 |
| `POST /move/getDetail` | 查询详情 |
| `POST /move/add` | 新增移库单 |
| `POST /move/update` | 修改移库单 |
| `POST /move/audit` | 审核通过，状态变移动中 |
| `POST /move/reject` | 驳回 |
| `POST /move/addFinish` | 执行移库 |

### 规则

- 新增必须带明细。
- `billCode` 为空时生成 `MoveBillCode`。
- 新增必须选择目标仓库。
- 执行移库时从来源库存扣减，并给目标仓库库存增加。
- 目标仓库没有该物料库存时自动创建。
- 所有明细完成后，移库单状态变 `已完成`。
- 移库完成后自动生成一张已完成的移库入库单和一张已完成的移库出库单，用于库存流水留痕。

## 设备管理

接口前缀：`/device`

当前项目已实现第一版设备主数据：分页、详情、新增、修改、删除、设备参数绑定。不接 MQTT、Redis 实时报告、InfluxDB 历史数据。

### 字段

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `name` | 设备名称 | 必填，唯一 |
| `code` | 设备编码 | 唯一，不传自动生成 |
| `uuid` | 采集端识别码 | 唯一，空值默认等于 `code` |
| `status` | 当前状态 | 只读，默认关机 |
| `alarm` | 报警状态 | 只读，默认正常 |
| `partCount` | 实时产量 | 只读，默认 `0` |
| `isReporting` | 是否开启采集 | 默认开启 |
| `rate` | 采集频率 | 默认 `1000` |

### 当前实现

已实现：

- `DeviceController`
- `DeviceService` / `DeviceServiceImpl`
- `DeviceMapper` / `DeviceParameterMapper`
- `DevicePageFilter`
- `DeviceMapper.xml`
- `CodeRuleField.DEVICE_CODE`

已落地规则：

- `name` 必填。
- `name/code/uuid` 唯一校验。
- `code` 为空时使用 `CodeRuleService#createCode(CodeRuleField.DEVICE_CODE)` 自动生成。
- `uuid` 为空时默认等于 `code`。
- 新增默认 `status = 4`、`alarm = 0`、`partCount = 0`、`isReporting = true`、`rate = 1000`。
- 分页使用 MyBatis-Plus `selectPage` 和 Wrapper 条件查询。
- 详情返回 `parameters`。
- 新增/修改时如果传入 `parameters`，同步维护 `device_parameter` 关系。
- 删除设备时先删除 `device_parameter` 关系，再删除设备。

### 设备状态

| 值 | 状态 |
| ---: | --- |
| `0` | 未知 |
| `1` | 报警 |
| `2` | 运行 |
| `3` | 空闲 |
| `4` | 关机 |
| `5` | 调试 |

### 报警状态

| 值 | 状态 |
| ---: | --- |
| `0` | 正常 |
| `1` | 系统报警 |
| `2` | 手动报警 |
| `3` | 规则报警 |

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /device/getPage` | 分页查询设备 |
| `POST /device/getDetail` | 查询详情，带采集参数 |
| `POST /device/add` | 新增设备 |
| `POST /device/update` | 修改设备 |
| `POST /device/delete` | 删除设备 |
| `POST /device/getDeviceConfig` | 采集端根据 UUID 获取采集配置，免登录，待第二版实现 |
| `POST /device/getCurrentReport` | 获取实时上报数据，免登录，待第二版实现 |
| `POST /device/getDevicePayloadHistory` | 获取历史采集数据，免登录，待第二版实现 |

## 参数管理

接口前缀：`/parameter`

当前项目已实现第一版采集参数主数据 CRUD。

### 字段

| 字段 | 说明 | 规则 |
| --- | --- | --- |
| `code` | 参数编码 | 必填，唯一 |
| `label` | 参数名称 | 必填，唯一 |
| `isSystem` | 是否系统参数 | 新增默认 `false` |
| `dataType` | 数据类型 | 可选 |

### 当前实现

已实现：

- `ParameterController`
- `ParameterService` / `ParameterServiceImpl`
- `ParameterMapper`
- `ParameterPageFilter`

已落地规则：

- `code` 必填。
- `label` 必填。
- `code/label` 唯一校验。
- 新增时 `isSystem` 默认 `false`。
- 系统参数不能删除。
- 已被设备绑定的参数不能删除。

### 接口

| 接口 | 说明 |
| --- | --- |
| `POST /parameter/getPage` | 分页查询参数 |
| `POST /parameter/getDetail` | 查询参数详情 |
| `POST /parameter/add` | 新增参数 |
| `POST /parameter/update` | 修改参数 |
| `POST /parameter/delete` | 删除参数 |

### 第二版采集规则

- 设备与采集参数是多对多关系。
- 每台设备默认包含系统参数：状态、报警、实时产量。
- 采集端通过 UUID 获取设备配置。
- MQTT 上报后更新 `device.status`、`device.alarm`、`device.partCount`。
- 最新报告写 Redis。
- 历史数据写 InfluxDB。
- 相同设备参数值在短时间内重复上报时跳过。

## 与采购销售模块的联动

采购/销售模块不是库存本身，但需要通过 WMS 完成数量变化：

| 触发点 | 后续动作 |
| --- | --- |
| 采购单审核通过 | 状态变采购中，后续可生成或进入采购入库流程 |
| 采购入库完成 | 回写采购明细 `finishQuantity`，采购单最终完成 |
| 销售单审核通过 | 状态变出库中，并创建销售出库单 |
| 销售出库完成 | 回写销售明细 `finishQuantity`，销售单最终完成 |

第一版建议先把普通入库、普通出库、移库跑通，再接采购/销售联动。

## 推荐实施顺序

### 第一阶段：补齐主数据

1. 扩展 `CodeRuleField`：`DeviceCode`、`InputBillCode`、`OutputBillCode` 已完成；`MaterialCode`、`UnitCode`、`StorageCode`、`MoveBillCode` 待补或待校准。
2. 完善 `material`：自动编码、默认价格、单位必填。
3. 完善 `unit`：自动编码、删除引用校验。
4. 新增 `storage`：树形 CRUD。

### 第二阶段：库存核心

1. 新增 `inventory`：分页查询已完成；详情和独立内部服务能力待补。
2. 新增 `input` / `input_detail`：普通入库第一版流程已完成。
3. 新增 `output` / `output_detail`：普通出库第一版流程已完成。
4. 新增 `move` / `move_detail`：移库完整流程。

### 第三阶段：渠道联动

1. 采购单 `addFinish` 与采购入库完成联动。
2. 销售审核创建销售出库单。
3. 销售单 `addFinish` 与销售出库完成联动。
4. 梳理单据实际金额、完成数量、完成状态。

### 第四阶段：设备主数据

1. 新增 `device` CRUD。已完成。
2. 新增 `parameter` CRUD。已完成。
3. 设备绑定参数。已完成。
4. `getDeviceConfig` 免登录接口。

### 第五阶段：设备采集

1. Redis 实时报告。
2. MQTT 上报处理。
3. InfluxDB 历史数据。
4. `getCurrentReport` 和 `getDevicePayloadHistory`。

## 第一版验收标准

- 物料、单位、仓库可维护。
- 设备可分页查询、查看详情、新增、修改、删除。
- 设备可绑定采集参数，详情返回参数列表。
- 参数可分页查询、查看详情、新增、修改、删除。
- 系统参数不能删除，已被设备绑定的参数不能删除。
- 库存可分页查询，分页结果返回物料、物料单位和仓库。
- 普通入库能新增单据、修改单据、查询详情、分页查询、审核、驳回、执行入库。
- 普通入库执行时能创建或累加仓库库存，并在所有明细完成后把入库单标记为已完成。
- 普通出库能扣减库存，库存不足时报错。
- 移库能从来源库存扣减并增加目标仓库存。
- 出入库/移库单据具备新增、修改、详情、分页、审核、驳回、完成数量接口。
- 单据号可自动生成。
- 编译通过。
