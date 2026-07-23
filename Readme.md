# 20260723
test by molly

add by joanne

add by Milly

<<<<<<< HEAD
# DeskFlow — Hot Desk Booking API

芬兰办公室热桌预订 REST API。实现范围对应 [mini-project.md](../shared-content-fraser-team/mini-project/mini-project.md) 中 **Done means 之前** 的要求（领域模型 + 5 个必做接口 + 种子数据 + 创意功能）。幻灯片 / 演示验收清单不在本目录代码范围内。

## 怎么跑

```bash
cd MiniProject
mvn spring-boot:run
```

默认：`http://localhost:8080`  
H2 控制台：`http://localhost:8080/h2-console`（JDBC URL 用 `jdbc:h2:mem:deskflow`）

---

## 一步一步做了什么、为什么

### 第 0 步：读 brief，定边界

Brief 要求：Spring Boot + 数据库、desk/booking 模型、5 个固定契约接口、至少 1 个创意功能、启动即有种子数据。  
**为什么先定边界：** 5 小时项目，先做可演示的垂直切片，避免一开始就做 auth / Docker / 前端。

### 第 1 步：Maven 工程骨架（`pom.xml`）

- Spring Boot **4.1** + Java **25**（与你们 RESTSpringBoot 环境一致）
- `webmvc`：HTTP API
- `data-jpa`：实体 ↔ 表
- `h2`：内存库，零安装就能 demo
- `validation`：预留校验能力

**为什么用 H2 而不是先上 MySQL：** brief 允许 H2；本地/Codespaces 立刻能跑。表结构按 MySQL 友好方式设计，以后换 MySQL 主要改 datasource。

### 第 2 步：启动类 + 配置

- `DeskFlowApplication`：Boot 入口
- `application.properties`：端口 8080、`ddl-auto=update`、打开 H2 控制台

**为什么 `ddl-auto=update`：** 开发期让 JPA 按实体建表，省手写 SQL；演示够用。

### 第 3 步：领域模型（`model/`）

按 brief 建两张概念表：

| 实体 | 关键字段 | 约束 |
|------|----------|------|
| `Desk` | code, floor, hasMonitor, active | code 唯一；inactive 不可订 |
| `Booking` | desk, employeeName, date, createdAt | **同一 desk + 同一 date 最多一条** |

`booking` 上建了 `@UniqueConstraint(desk_id, booking_date)`。  
**为什么：** 核心业务规则是「一桌一日一订」；库级唯一约束是最后一道防线，接口层也会先查再返回 409。

### 第 4 步：Repository（`repository/`）

Spring Data 接口：按楼层/显示器筛桌子、按日期/员工查预订。  
**为什么：** 少写样板 SQL，Controller/Service 只关心业务。

### 第 5 步：DTO + 异常（`dto/`、`exception/`）

- 响应用 `DeskResponse` / `BookingResponse`，**不直接把 JPA 实体甩给前端**
- `ApiException` + `GlobalExceptionHandler` 统一成 `{ "error", "details" }` 和正确 HTTP 状态码

**为什么：** brief 的 400/404/409 要稳定可演示；实体带懒加载，直接序列化容易炸或泄露内部结构。

### 第 6 步：Service（业务规则集中）

`DeskBookingService` 负责：

1. 列桌子（可选 `floor` / `hasMonitor`）
2. 创建预订：校验空名、缺日期、桌子不存在(404)、inactive(400)、冲突(409)
3. 按日列预订、取消(204/404)
4. 创意：某日空闲桌；按员工名查自己的预订

**为什么单独 Service：** Controller 只做 HTTP；规则集中方便讲「409 从哪来」。

### 第 7 步：五个必做 API（`controller/`）

| 方法 | 路径 | 作用 |
|------|------|------|
| GET | `/api/health` | 探活 |
| GET | `/api/desks` | 列桌子，支持 `?floor=` / `?hasMonitor=` |
| POST | `/api/bookings` | 创建预订 → **201** |
| GET | `/api/bookings?date=` | 某日所有预订 |
| DELETE | `/api/bookings/{id}` | 取消 → **204** |

### 第 8 步：创意功能（必做但题目开放）

1. **`GET /api/desks/available?date=2026-07-24`**  
   返回当日仍空闲且 `active=true` 的桌子 —— demo 最直观。
2. **`GET /api/bookings?employeeName=Anna%20Kowalska`**  
   员工视角：我的全部预订。

### 第 9 步：种子数据（`DataSeeder`）

启动时若库空：

- **9 张桌**，覆盖 3/4/5 楼，含有/无显示器、一张 **inactive**（`HEL-3F-04`）方便演示 400
- **3 条样例预订**（含 2026-07-24）

**为什么：** 验收要求「不用手工插数就能 demo」。

---

## Demo 用 curl

```bash
# 1. Health
curl http://localhost:8080/api/health

# 2. 所有桌子 / 只看 4 楼 / 只要有显示器
curl http://localhost:8080/api/desks
curl "http://localhost:8080/api/desks?floor=4"
curl "http://localhost:8080/api/desks?hasMonitor=true"

# 3. 某日空闲桌（创意）
curl "http://localhost:8080/api/desks/available?date=2026-07-24"

# 4. 新建预订
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d "{\"deskId\":2,\"employeeName\":\"Test User\",\"date\":\"2026-07-24\"}"

# 5. 故意重复预订同一桌同一天 → 409
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d "{\"deskId\":1,\"employeeName\":\"Someone\",\"date\":\"2026-07-24\"}"

# 6. 订 inactive 桌 (id=4) → 400
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d "{\"deskId\":4,\"employeeName\":\"Someone\",\"date\":\"2026-07-25\"}"

# 7. 按日查预订
curl "http://localhost:8080/api/bookings?date=2026-07-24"

# 8. 按员工查（创意）
curl "http://localhost:8080/api/bookings?employeeName=Anna%20Kowalska"

# 9. 取消（把 {id} 换成真实 id）
curl -X DELETE http://localhost:8080/api/bookings/1
```

---

## 目录结构

```
MiniProject/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/deskflow/
    │   ├── DeskFlowApplication.java
    │   ├── config/DataSeeder.java
    │   ├── controller/          # HTTP 入口
    │   ├── dto/                 # 请求/响应形状
    │   ├── exception/           # 统一错误
    │   ├── model/               # JPA 实体 = 表
    │   ├── repository/          # 数据访问
    │   └── service/             # 业务规则
    └── resources/application.properties
```