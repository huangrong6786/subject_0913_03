# EVOPS 基础工作区

这是 `001-evops` 题包的初始 Java 工作区。它只提供构建、配置、统一返回、异常处理、MyBatis-Plus 和 H2 本地数据库基础设施，不预先实现 F1 的站点、充电枪、会话和结算业务；F1 由执行模型从这里开始完成。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

启动前准备：

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。

题包根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

题面和质检卷在上一级 `packets/` 目录；模型工作区不得复制 `answers.md`、验收测试或参考修正。

## 古籍修复工序与纸张状态追踪模块

### 业务对象与数据表（schema.sql 自动初始化）

- `t_archive_volume` 馆藏册（**业务键：馆藏册号 + 修复版本唯一**），跟踪含水率、酸碱度、纤维强度；状态：REGISTERED 已登记 → BATCHED 已入批 → RESTORING 修复中 → ACCEPTED 已验收 → POSTED 已落账
- `t_restore_batch` 修复批次（批次号唯一）+ `t_batch_volume` 批次-馆藏册关联
- `t_restore_order` 修复工单/工序（工单号唯一），状态：CREATED → IN_PROGRESS → COMPLETED → ACCEPTED；建单与完工各保留一次纸张状态快照，完工登记工时
- `t_inspect_sample` 检测样本（样本编号唯一），落库保留纸张状态快照并同步馆藏册当前纸张状态
- `t_accept_record` 验收记录（验收单号唯一），验收通过可签发报告（签发即落账）

业务日期：送修日期 `received_date`、批次建立日期 `biz_date`、检测日期 `inspect_date`、验收日期 `accept_date`（另有工单开工/完工日期）。

### REST 接口（统一返回 ApiResponse：success / message / data）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/restore/volumes | 登记馆藏册（初始纸张状态 + 送修日期） |
| GET | /api/restore/volumes?keyword=&status= | 查询馆藏册 |
| GET | /api/restore/volumes/{id} | 馆藏册详情 |
| GET | /api/restore/volumes/{id}/trace | 全链路追踪：馆藏册+工单+样本+验收 |
| DELETE | /api/restore/volumes/{id} | 删除（已验收/已落账拒绝） |
| POST | /api/restore/batches | 批次建立（同事务：批次+关联+馆藏册入批） |
| GET | /api/restore/batches 、/{id} | 批次列表 / 详情（含关联馆藏册） |
| DELETE | /api/restore/batches/{id} | 删除（已关联馆藏册拒绝） |
| POST | /api/restore/orders | 创建工单（馆藏册须已入批，转修复中） |
| PUT | /api/restore/orders/{id}/transition | 状态流转 START / COMPLETE（完工登记工时+纸张快照） |
| GET | /api/restore/orders?batchId=&volumeId=&status= | 查询工单 |
| DELETE | /api/restore/orders/{id} | 删除（已验收拒绝；无工单时馆藏册回退已入批） |
| POST | /api/restore/samples | 登记检测样本（纸张快照，同步馆藏册状态） |
| GET | /api/restore/samples?orderId=&volumeId= | 查询样本 |
| POST | /api/restore/accepts | 验收登记（PASSED/REJECTED；issueReport=true 同事务签发落账） |
| PUT | /api/restore/accepts/{id}/issue-report | 签发报告（馆藏册落账 POSTED，仅一次） |
| GET | /api/restore/accepts?orderId=&result= | 查询验收记录 |
| DELETE | /api/restore/accepts/{id} | 删除（已签发报告/已验收拒绝） |

### 事务与审计约束

- 所有跨表写入（批次建立、建单、完工、检测、验收、签发）在同一 `@Transactional` 事务中完成，失败整体回滚。
- 每笔业务单据落库保存：请求号 `request_no`（服务端生成 REQ-*）、操作者 `operator`、业务时区 `biz_timezone`（ZoneId 校验）、版本快照 `version_snapshot`。
- 检测、工序（建单/完工）、验收均保留纸张状态快照 `paper_snapshot`（含水率/酸碱度/纤维强度）。
- 状态流转全部使用条件更新（`UPDATE ... WHERE status=?`），并发下仅一个请求成功；业务键由数据库唯一约束兜底。
- 已验收或已落账记录不能直接删除；已签发报告不可直接删除。
- 并发与删除守卫由 `RestoreConcurrencyTest`（5 并发写入）覆盖：`mvn test`。
