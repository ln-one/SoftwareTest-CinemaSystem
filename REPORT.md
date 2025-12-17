# 软件测试实验报告

## 电影院售票管理系统单元测试与覆盖率分析

---

## 摘要

本实验以电影院售票管理系统为测试对象，采用 JUnit 5 + Mockito 测试框架，对系统后端 Service 层进行了全面的单元测试和基于 OWASP Top 10 标准的安全测试。实验共设计并执行 162 个测试用例，其中包含 46 个专业安全测试用例，涵盖功能测试、OWASP 安全风险检测、并发攻击模拟、时间侧信道分析等多种场景。安全测试覆盖了访问控制失效、加密机制失效、注入攻击、不安全设计、身份认证失效等 5 大 OWASP 风险类别。通过 JaCoCo 工具进行覆盖率统计，Service 层指令覆盖率达到 97%，分支覆盖率达到 93%。在测试过程中发现 2 个代码缺陷和 12 个基于 OWASP 标准的安全风险点，并进行了详细的根因分析，提出了相应的修复建议。

**关键词**：OWASP Top 10；安全测试；单元测试；代码覆盖率；SQL注入；访问控制；JaCoCo

---

## 1. 引言

### 1.1 实验背景

软件测试是保证软件质量的重要手段。单元测试作为测试金字塔的基础层，能够在开发早期发现代码缺陷，降低修复成本。代码覆盖率是衡量测试充分性的重要指标，包括语句覆盖、分支覆盖、路径覆盖等多个维度。

### 1.2 实验目的

1. **掌握安全测试的方法** - 基于 OWASP Top 10 标准设计和执行专业安全测试
2. 掌握单元测试的设计原则与实现方法
3. 理解代码覆盖率的概念及度量方式（语句覆盖、条件覆盖）
4. 学习使用 JaCoCo 工具进行覆盖率统计
5. 通过测试用例发现软件缺陷并进行根因分析
6. 培养团队协作完成测试任务的能力
7. 掌握高级测试技术（参数化测试、并发测试、Mock技术）

### 1.3 实验环境

| 项目 | 版本/说明 |
|------|----------|
| 操作系统 | macOS / Windows 10 |
| JDK | 1.8+ |
| 构建工具 | Maven 3.6+ |
| IDE | IntelliJ IDEA 2023 |
| 测试框架 | JUnit 5.7 |
| Mock框架 | Mockito 3.6 |
| 覆盖率工具 | JaCoCo 0.8.8 |
| 版本控制 | Git |

---

## 2. 被测系统分析

### 2.1 系统概述

本实验选用**电影院售票管理系统**作为被测对象。该系统是一个完整的电影院在线售票解决方案，采用前后端分离架构，支持用户在线浏览电影、选座购票，以及管理员对影院、电影、场次、订单等信息的管理。系统具有典型的 Web 应用特征，包含用户认证、权限控制、数据操作等多种安全风险场景，适合进行全面的安全测试。

### 2.2 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                      前端层                              │
│  ┌─────────────────┐      ┌─────────────────┐          │
│  │  用户端 (Vue)    │      │  管理端 (Vue)    │          │
│  │  Element UI     │      │  Element UI     │          │
│  └────────┬────────┘      └────────┬────────┘          │
└───────────┼─────────────────────────┼───────────────────┘
            │         HTTP/REST       │
┌───────────┼─────────────────────────┼───────────────────┐
│           ▼                         ▼                   │
│  ┌─────────────────────────────────────────────┐       │
│  │            Controller 层                     │       │
│  └─────────────────────┬───────────────────────┘       │
│                        │                                │
│  ┌─────────────────────▼───────────────────────┐       │
│  │            Service 层 (测试目标)             │       │
│  └─────────────────────┬───────────────────────┘       │
│                        │                                │
│  ┌─────────────────────▼───────────────────────┐       │
│  │            Mapper 层 (MyBatis)              │       │
│  └─────────────────────┬───────────────────────┘       │
│                        │                                │
└────────────────────────┼────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │      MySQL 8.0      │
              └─────────────────────┘
```

### 2.3 核心业务模块


| 模块 | 功能描述 | 核心实体 |
|------|----------|----------|
| 用户管理 | 用户注册、登录、信息维护 | SysUser |
| 电影管理 | 电影信息的增删改查、票房统计 | SysMovie |
| 电影类别 | 电影分类管理、电影与类别关联 | SysMovieCategory |
| 影院管理 | 影院基本信息配置 | SysCinema |
| 影厅管理 | 影厅信息、座位配置 | SysHall |
| 场次管理 | 电影排片、场次查询 | SysSession |
| 订单管理 | 订单创建、支付、取消、超时处理 | SysBill |
| 角色管理 | 系统角色定义、权限分配 | SysRole |
| 资源管理 | 系统菜单资源、权限控制 | SysResource |

### 2.4 数据库设计

系统数据库包含以下核心表：

```sql
-- 用户表
sys_user (user_id, user_name, password, salt, sex, phone_number, role_id, ...)

-- 电影表  
sys_movie (movie_id, movie_name, movie_length, movie_area, release_date, movie_box_office, ...)

-- 订单表
sys_bill (bill_id, user_id, session_id, seats, pay_state, cancel_state, create_time, deadline, ...)

-- 场次表
sys_session (session_id, movie_id, hall_id, session_date, play_time, end_time, session_price, ...)

-- 影厅表
sys_hall (hall_id, cinema_id, hall_name, hall_category, row_nums, seat_nums, seat_state, ...)
```

---

## 3. 测试计划设计

### 3.1 测试范围

本次测试聚焦于后端 Service 层，该层承载了系统的核心业务逻辑。选择 Service 层作为测试目标的原因：

1. **业务逻辑集中**：Service 层包含了数据校验、业务规则、事务处理等核心逻辑
2. **可测试性强**：通过 Mock 技术可以隔离数据库依赖，实现快速、稳定的测试
3. **投入产出比高**：相比 Controller 层和 Mapper 层，Service 层测试能发现更多业务逻辑缺陷

### 3.2 测试策略

采用**白盒测试**方法，结合以下测试设计技术：

#### 3.2.1 语句覆盖 (Statement Coverage)

确保每条可执行语句至少被执行一次。这是最基本的覆盖准则。

#### 3.2.2 分支覆盖 (Branch Coverage)

确保每个判断语句的真假分支都至少被执行一次。例如：

```java
if (user == null) {           // 分支1: user为null
    throw new Exception();
}                              // 分支2: user不为null
```

#### 3.2.3 边界值分析

针对输入参数的边界条件设计测试用例：

- 空数组 `new Long[]{}`
- null 参数
- 单元素数组
- 大数据量

#### 3.2.4 等价类划分

将输入数据划分为有效等价类和无效等价类：

| 输入 | 有效等价类 | 无效等价类 |
|------|-----------|-----------|
| 用户ID | 存在的ID | 不存在的ID、null |
| 用户名 | 唯一的用户名 | 重复的用户名、null |
| 密码 | 正确密码 | 错误密码 |

### 3.3 测试用例设计原则

1. **单一职责**：每个测试用例只验证一个功能点
2. **独立性**：测试用例之间相互独立，不依赖执行顺序
3. **可重复性**：测试结果稳定，多次执行结果一致
4. **自描述性**：通过测试方法名和注解清晰表达测试意图

### 3.4 Mock 策略

使用 Mockito 框架模拟 Mapper 层，实现 Service 层的隔离测试：

```java
@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {
    
    @Mock
    private SysUserMapper sysUserMapper;  // 模拟数据访问层
    
    @InjectMocks
    private SysUserServiceImpl sysUserService;  // 注入被测对象
    
    @Test
    void findUserById_UserExists_ReturnsUser() {
        // Arrange - 设置模拟行为
        when(sysUserMapper.findUserById(1L)).thenReturn(testUser);
        
        // Act - 执行被测方法
        SysUser result = sysUserService.findUserById(1L);
        
        // Assert - 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUserName());
    }
}
```

---

## 4. 测试实现

### 4.1 测试环境配置

#### 4.1.1 Maven 依赖配置

在 `pom.xml` 中添加测试相关依赖：

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 内存数据库 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

#### 4.1.2 JaCoCo 插件配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 4.2 测试目录结构

```
src/test/java/com/rabbiter/cm/
├── CinemaManagerApplicationTests.java    # Spring Boot 启动测试
└── service/
    ├── SysUserServiceTest.java           # 用户服务测试 (21个用例)
    ├── SysBillServiceTest.java           # 订单服务测试 (15个用例)
    ├── SysSessionServiceTest.java        # 场次服务测试 (11个用例)
    ├── SysMovieServiceTest.java          # 电影服务测试 (11个用例)
    ├── SysMovieCategoryServiceTest.java  # 电影类别测试 (9个用例)
    ├── SysCinemaServiceTest.java         # 影院服务测试 (8个用例)
    ├── SysHallServiceTest.java           # 影厅服务测试 (10个用例)
    ├── SysRoleServiceTest.java           # 角色服务测试 (17个用例)
    └── SysResourceServiceTest.java       # 资源服务测试 (13个用例)
```


---

## 5. 各模块测试详情

### 5.1 用户模块测试（成员A负责）

#### 5.1.1 被测类分析

`SysUserServiceImpl` 是用户管理的核心服务类，包含以下关键方法：

| 方法 | 功能 | 复杂度 |
|------|------|--------|
| `findUserById(Long id)` | 根据ID查询用户 | 低 |
| `findUserByName(String userName)` | 根据用户名查询 | 低 |
| `addUser(SysUser user)` | 用户注册（含密码加密） | 高 |
| `updateUser(SysUser user)` | 更新用户信息 | 高 |
| `deleteUser(Long[] ids)` | 批量删除用户 | 中 |
| `login(SysUserVo vo)` | 用户登录验证 | 高 |
| `isUserNameUnique(String name, Long id)` | 用户名唯一性校验 | 中 |

#### 5.1.2 测试用例清单

| 编号 | 测试用例 | 测试类型 | 预期结果 |
|------|----------|----------|----------|
| U01 | 根据ID查询用户-用户存在 | 正常流程 | 返回用户对象 |
| U02 | 根据ID查询用户-用户不存在 | 边界条件 | 返回null |
| U03 | 根据用户名查询用户 | 正常流程 | 返回用户对象 |
| U04 | 查询所有用户 | 正常流程 | 返回用户列表 |
| U05 | 删除用户-单个删除 | 正常流程 | 返回删除数量1 |
| U06 | 删除用户-批量删除 | 正常流程 | 返回删除数量N |
| U07 | 用户名唯一性校验-可用 | 正常流程 | 返回true |
| U08 | 用户名唯一性校验-已存在 | 异常流程 | 返回false |
| U09 | 用户名唯一性校验-属于自己 | 边界条件 | 返回true |
| U10 | 添加用户-用户名重复 | 异常流程 | 抛出异常 |
| U11 | 更新用户-用户不存在 | 异常流程 | 抛出异常 |
| U12 | 更新用户-用户名被占用 | 异常流程 | 抛出异常 |
| U13 | 登录-用户名不存在 | 异常流程 | 抛出异常 |
| U14 | 登录-密码错误 | 异常流程 | 抛出异常 |
| U15 | 删除用户-空数组 | 边界条件 | 返回0 |
| U16 | 更新用户-密码未修改 | 边界条件 | 不重新加密 |
| U17 | 更新用户-密码已修改 | 正常流程 | 重新加密 |
| U18 | 删除用户-null数组 | 异常流程 | 抛出NPE |
| U19 | 添加用户-用户名为null | 边界条件 | 正常处理 |
| U20 | 用户名校验-多用户同名(Bug) | 缺陷探测 | 验证Bug存在 |
| U21 | 更新用户-salt处理(Bug) | 缺陷探测 | 验证Bug存在 |

#### 5.1.3 关键测试代码示例

```java
@Test
@DisplayName("登录 - 密码错误抛出异常")
void login_WrongPassword_ThrowsException() {
    // Arrange
    SysUserVo loginVo = new SysUserVo();
    loginVo.setUserName("testuser");
    loginVo.setPassword("wrongpassword");
    
    SysUser dbUser = new SysUser();
    dbUser.setUserName("testuser");
    dbUser.setPassword("hashedpassword");
    dbUser.setSalt("randomsalt");
    
    when(sysUserMapper.findUserByName("testuser")).thenReturn(dbUser);

    // Act & Assert
    AuthenticationException exception = assertThrows(
        AuthenticationException.class,
        () -> sysUserService.login(loginVo)
    );
    assertEquals("用户名或密码错误", exception.getMessage());
}
```

---

### 5.2 订单与场次模块测试（成员B负责）

#### 5.2.1 订单服务测试

`SysBillServiceImpl` 负责订单的全生命周期管理：

| 方法 | 功能 | 测试重点 |
|------|------|----------|
| `findAllBills(SysBill bill)` | 查询订单列表 | 条件查询 |
| `findBillById(Long id)` | 查询单个订单 | 存在/不存在 |
| `addBill(SysBill bill)` | 创建订单 | 返回值类型判断 |
| `updateBill(SysBill bill)` | 更新订单状态 | 支付/取消 |
| `deleteBill(Long[] ids)` | 删除订单 | 批量操作 |
| `findTimeoutBill()` | 查询超时订单 | 定时任务场景 |

#### 5.2.2 订单测试用例

| 编号 | 测试用例 | 测试类型 |
|------|----------|----------|
| B01 | 查询所有订单 | 正常流程 |
| B02 | 根据ID查询订单-存在 | 正常流程 |
| B03 | 根据ID查询订单-不存在 | 边界条件 |
| B04 | 添加订单-成功 | 正常流程 |
| B05 | 添加订单-失败 | 异常流程 |
| B06 | 更新订单 | 正常流程 |
| B07 | 删除订单-单个 | 正常流程 |
| B08 | 删除订单-批量 | 正常流程 |
| B09 | 查询超时订单-有数据 | 正常流程 |
| B10 | 查询超时订单-无数据 | 边界条件 |
| B11 | 删除订单-空数组 | 边界条件 |
| B12 | 删除订单-部分失败 | 异常流程 |
| B13 | 添加订单-验证返回信息 | 数据校验 |
| B14 | 删除订单-null数组 | 异常流程 |
| B15 | 查询订单-null参数 | 边界条件 |

#### 5.2.3 场次服务测试

`SysSessionServiceImpl` 管理电影排片信息：

| 编号 | 测试用例 | 测试类型 |
|------|----------|----------|
| S01 | 根据条件查询场次 | 正常流程 |
| S02 | 根据电影/影厅ID查询 | 正常流程 |
| S03 | 根据ID查询场次-存在 | 正常流程 |
| S04 | 根据ID查询场次-不存在 | 边界条件 |
| S05 | 查询单个场次详情 | 正常流程 |
| S06 | 添加场次 | 正常流程 |
| S07 | 更新场次 | 正常流程 |
| S08 | 删除场次-单个 | 正常流程 |
| S09 | 删除场次-批量 | 正常流程 |
| S10 | 根据电影ID查询场次 | 正常流程 |
| S11 | 根据电影ID查询-无场次 | 边界条件 |

---

### 5.3 电影模块测试（成员C负责）

#### 5.3.1 电影服务测试

`SysMovieServiceImpl` 包含电影信息管理和票房统计功能：

| 方法 | 功能 | 业务特点 |
|------|------|----------|
| `findAllMovies(SysMovieVo vo)` | 电影列表查询 | 支持多条件筛选 |
| `findMovieById(Long id)` | 电影详情 | 基础查询 |
| `addMovie(SysMovie movie)` | 添加电影 | 含海报上传 |
| `updateMovie(SysMovie movie)` | 更新电影 | 信息维护 |
| `deleteMovie(Long[] ids)` | 删除电影 | 软删除 |
| `totalBoxOfficeList()` | 总票房榜 | 统计排序 |
| `domesticBoxOfficeList()` | 国内票房榜 | 区域筛选 |
| `foreignBoxOfficeList()` | 国外票房榜 | 区域筛选 |

#### 5.3.2 电影测试用例

| 编号 | 测试用例 | 覆盖场景 |
|------|----------|----------|
| M01 | 查询所有电影 | 列表查询 |
| M02 | 根据ID查询-存在 | 详情查询 |
| M03 | 根据ID查询-不存在 | 空结果处理 |
| M04 | 查询单个电影详情 | 关联查询 |
| M05 | 添加电影 | 数据插入 |
| M06 | 更新电影 | 数据更新 |
| M07 | 删除电影-单个 | 单条删除 |
| M08 | 删除电影-批量 | 批量删除 |
| M09 | 总票房榜 | 统计功能 |
| M10 | 国内票房榜 | 条件统计 |
| M11 | 国外票房榜 | 空结果处理 |

#### 5.3.3 电影类别测试

| 编号 | 测试用例 | 覆盖场景 |
|------|----------|----------|
| C01 | 查询所有类别 | 列表查询 |
| C02 | 根据ID查询-存在 | 详情查询 |
| C03 | 根据ID查询-不存在 | 空结果 |
| C04 | 添加类别 | 数据插入 |
| C05 | 更新类别 | 数据更新 |
| C06 | 删除类别-单个 | 单条删除 |
| C07 | 删除类别-批量 | 批量删除 |
| C08 | 添加电影类别关联 | 多对多关系 |
| C09 | 删除电影类别关联 | 关系解除 |

---

### 5.4 影院与影厅模块测试（成员D负责）

#### 5.4.1 影院服务测试

`SysCinemaServiceImpl` 管理影院基本信息：

| 方法 | 功能 | 说明 |
|------|------|------|
| `findCinema()` | 查询影院信息 | 单例模式，系统只有一个影院 |
| `findCinemaById(Long id)` | 根据ID查询 | 支持多影院扩展 |
| `updateCinema(SysCinema cinema)` | 更新影院信息 | 基本信息维护 |

#### 5.4.2 影院测试用例

| 编号 | 测试用例 | 测试目的 |
|------|----------|----------|
| CI01 | 查询影院信息 | 正常查询 |
| CI02 | 查询影院-无数据 | 空结果处理 |
| CI03 | 更新影院-成功 | 正常更新 |
| CI04 | 更新影院-失败 | 失败处理 |
| CI05 | 根据ID查询-存在 | ID查询 |
| CI06 | 根据ID查询-不存在 | 空结果 |
| CI07 | 验证电话格式 | 数据校验 |
| CI08 | 验证地址不为空 | 数据校验 |

#### 5.4.3 影厅服务测试

`SysHallServiceImpl` 管理影厅和座位信息：

| 编号 | 测试用例 | 测试目的 |
|------|----------|----------|
| H01 | 查询所有影厅 | 列表查询 |
| H02 | 查询影厅-无数据 | 空列表 |
| H03 | 根据ID查询-存在 | 详情查询 |
| H04 | 根据ID查询-不存在 | 空结果 |
| H05 | 添加影厅 | 数据插入 |
| H06 | 更新影厅 | 数据更新 |
| H07 | 删除影厅-单个 | 单条删除 |
| H08 | 删除影厅-批量 | 批量删除 |
| H09 | 验证座位数计算 | 业务逻辑 |
| H10 | 验证影厅类别 | 数据校验 |

---

### 5.5 角色权限模块测试（成员E负责）

#### 5.5.1 角色服务测试

`SysRoleServiceImpl` 实现角色管理和权限分配：

| 方法 | 功能 | 复杂度 |
|------|------|--------|
| `findAllRoles()` | 查询所有角色 | 低 |
| `findRoleById(Long id)` | 查询单个角色 | 低 |
| `addRole(SysRole role)` | 添加角色 | 低 |
| `updateRole(SysRole role)` | 更新角色 | 低 |
| `deleteRole(Long[] ids)` | 删除角色 | 中 |
| `allotRight(Long roleId, Long[] keys)` | 分配权限 | **高** |

`allotRight` 方法是本模块的核心，实现了权限的增量更新：

```java
public int allotRight(Long roleId, Long[] keys) {
    HashSet<Long> originResources = new HashSet<>(sysRoleMapper.findAllRights(roleId));
    int rows = 0;
    
    for (Long id : keys) {
        if (originResources.contains(id)) {
            originResources.remove(id);  // 保留的权限
        } else {
            rows += sysRoleMapper.addRight(roleId, id);  // 新增的权限
        }
    }
    for (Long id : originResources) {
        rows += sysRoleMapper.deleteRight(roleId, id);  // 删除的权限
    }
    return rows;
}
```

#### 5.5.2 角色测试用例

| 编号 | 测试用例 | 测试场景 |
|------|----------|----------|
| R01 | 查询所有角色 | 正常查询 |
| R02 | 查询角色-无数据 | 空列表 |
| R03 | 根据ID查询-存在 | 详情查询 |
| R04 | 根据ID查询-不存在 | 空结果 |
| R05 | 添加角色 | 数据插入 |
| R06 | 更新角色 | 数据更新 |
| R07 | 删除角色-单个 | 单条删除 |
| R08 | 删除角色-批量 | 批量删除 |
| R09 | 分配权限-新增 | 权限增加 |
| R10 | 分配权限-删除 | 权限移除 |
| R11 | 分配权限-混合 | 部分新增部分删除 |
| R12 | 分配权限-不变 | 无变化场景 |
| R13 | 分配权限-大量变更 | 性能场景 |
| R14 | 分配权限-清空 | 全部移除 |
| R15 | 删除角色-空数组 | 边界条件 |
| R16 | 分配权限-null数组 | 异常处理 |
| R17 | 删除角色-null数组 | 异常处理 |

#### 5.5.3 资源服务测试

`SysResourceServiceImpl` 管理系统菜单资源，支持树形结构：

| 编号 | 测试用例 | 测试场景 |
|------|----------|----------|
| RS01 | 查询所有资源 | 列表查询 |
| RS02 | 查询资源-无数据 | 空列表 |
| RS03 | 查询带子资源 | 树形查询 |
| RS04 | 查询所有含子资源 | 完整树 |
| RS05 | 根据ID查询-存在 | 详情查询 |
| RS06 | 根据ID查询-不存在 | 空结果 |
| RS07 | 添加资源-顶级 | level=1 |
| RS08 | 添加资源-子级 | level=parent+1 |
| RS09 | 更新资源-顶级 | level重算 |
| RS10 | 更新资源-子级 | level重算 |
| RS11 | 删除资源-单个 | 单条删除 |
| RS12 | 删除资源-批量 | 批量删除 |
| RS13 | 验证层级关系 | 数据一致性 |

---

## 6. 安全测试

安全测试是本次实验的重点内容。针对电影院售票系统的特点，我们设计了一系列安全测试用例，覆盖认证安全、密码安全、输入验证、授权安全等多个方面。

### 6.1 安全测试概述

#### 6.1.1 安全测试范围

| 测试类别 | 测试内容 | 风险等级 |
|----------|----------|----------|
| 认证安全 | 登录验证、暴力破解防护 | 高 |
| 密码安全 | 加密存储、盐值处理 | 高 |
| 输入验证 | SQL注入、XSS攻击防护 | 高 |
| 授权安全 | 权限控制、越权访问 | 中 |
| 数据安全 | 批量操作、空值处理 | 中 |

#### 6.1.2 安全测试方法

本次安全测试采用以下方法：

1. **黑盒测试**：模拟攻击者视角，使用常见攻击载荷进行测试
2. **白盒测试**：分析源代码，验证安全机制的实现
3. **边界测试**：测试安全边界条件的处理

### 6.2 认证安全测试

#### 6.2.1 测试目标

验证系统的身份认证机制能够有效防止未授权访问。

#### 6.2.2 测试用例

| 编号 | 测试场景 | 预期结果 | 实际结果 |
|------|----------|----------|----------|
| SEC-AUTH-001 | 使用不存在的用户名登录 | 拒绝登录，提示"用户名不存在" | 通过 |
| SEC-AUTH-002 | 使用错误密码登录 | 拒绝登录，提示"用户名或密码错误" | 通过 |
| SEC-AUTH-003 | 暴力破解尝试（多次失败登录） | 所有尝试均失败 | 通过 |

#### 6.2.3 测试代码示例

```java
@Test
@DisplayName("SEC-AUTH-002: 密码错误时应拒绝登录")
void login_WrongPassword_ShouldReject() {
    SysUserVo loginVo = new SysUserVo();
    loginVo.setUserName("admin");
    loginVo.setPassword("wrong_password");
    
    SysUser dbUser = new SysUser();
    dbUser.setUserName("admin");
    dbUser.setPassword("5f4dcc3b5aa765d61d8327deb882cf99");
    dbUser.setSalt("random_salt");
    
    when(sysUserMapper.findUserByName("admin")).thenReturn(dbUser);

    AuthenticationException exception = assertThrows(
        AuthenticationException.class,
        () -> sysUserService.login(loginVo)
    );
    
    assertEquals("用户名或密码错误", exception.getMessage());
}
```

#### 6.2.4 安全建议

1. 实现账户锁定机制，连续失败N次后锁定账户
2. 添加登录日志，记录失败的登录尝试
3. 考虑引入验证码机制防止自动化攻击

### 6.3 密码安全测试

#### 6.3.1 测试目标

验证系统的密码存储和处理机制符合安全最佳实践。

#### 6.3.2 测试用例

| 编号 | 测试场景 | 预期结果 | 实际结果 |
|------|----------|----------|----------|
| SEC-PWD-001 | 新用户注册时密码加密 | 密码使用MD5+盐值加密存储 | 通过 |
| SEC-PWD-002 | 不同用户使用相同密码 | 加密后的密码不同（因盐值不同） | 通过 |
| SEC-PWD-003 | 修改密码时重新生成盐值 | 新盐值与旧盐值不同 | 通过 |

#### 6.3.3 密码加密机制分析

系统采用 MD5 + Salt + 1024次迭代 的方式存储密码：

```java
String salt = SaltUtils.getSalt(8);  // 生成8位随机盐值
Md5Hash md5Hash = new Md5Hash(password, salt, 1024);  // 1024次哈希迭代
user.setPassword(md5Hash.toHex());
user.setSalt(salt);
```

#### 6.3.4 安全建议

1. 考虑使用更安全的哈希算法（如 bcrypt、scrypt、Argon2）
2. 增加密码复杂度校验
3. 实现密码历史记录，防止重复使用旧密码

### 6.4 输入验证安全测试（SQL注入/XSS）

#### 6.4.1 测试目标

验证系统能够有效防御 SQL 注入和 XSS 攻击。

#### 6.4.2 SQL注入测试用例

| 编号 | 攻击载荷 | 测试位置 | 预期结果 | 实际结果 |
|------|----------|----------|----------|----------|
| SEC-INPUT-001 | `admin'; DROP TABLE sys_user; --` | 用户名 | 查询失败，不执行恶意SQL | 通过 |
| SEC-INPUT-002 | `' OR '1'='1` | 密码 | 登录失败 | 通过 |

#### 6.4.3 SQL注入防护分析

系统使用 MyBatis 框架，通过参数化查询（`#{}` 占位符）防止 SQL 注入：

```xml
<!-- MyBatis Mapper 配置 -->
<select id="findUserByName" resultType="SysUser">
    SELECT * FROM sys_user WHERE user_name = #{userName}
</select>
```

`#{userName}` 会被 MyBatis 处理为预编译参数，攻击载荷会被当作普通字符串处理，而非 SQL 代码。

#### 6.4.4 XSS测试用例

| 编号 | 攻击载荷 | 测试位置 | 说明 |
|------|----------|----------|------|
| SEC-INPUT-003 | `<script>alert('XSS')</script>` | 用户名 | Service层原样存储 |
| SEC-INPUT-004 | 特殊字符 `< > " ' ; --` | 各输入字段 | 正常处理不报错 |

#### 6.4.5 安全建议

1. 在展示层对输出进行 HTML 转义
2. 实现输入白名单校验
3. 使用 Content-Security-Policy 头防止 XSS

### 6.5 授权安全测试

#### 6.5.1 测试目标

验证系统的授权机制能够防止越权访问。

#### 6.5.2 测试用例

| 编号 | 测试场景 | 预期结果 | 实际结果 |
|------|----------|----------|----------|
| SEC-AUTHZ-001 | 注册时使用已存在的用户名 | 拒绝注册 | 通过 |
| SEC-AUTHZ-002 | 更新时使用他人用户名 | 拒绝更新 | 通过 |
| SEC-AUTHZ-003 | 更新不存在的用户 | 拒绝更新 | 通过 |

### 6.6 OWASP Top 10 高级安全测试

本节基于 OWASP Top 10 2021 安全风险清单，对系统进行深度安全漏洞检测。

#### 6.6.1 OWASP Top 10 测试覆盖

| OWASP编号 | 风险类别 | 测试用例数 | 发现风险 |
|-----------|----------|------------|----------|
| A01:2021 | 访问控制失效 (Broken Access Control) | 3 | 水平越权、垂直越权、IDOR |
| A02:2021 | 加密机制失效 (Cryptographic Failures) | 3 | 密码哈希强度验证通过 |
| A03:2021 | 注入攻击 (Injection) | 9 | SQL注入防护有效 |
| A04:2021 | 不安全设计 (Insecure Design) | 2 | 业务逻辑漏洞、竞态条件 |
| A07:2021 | 身份认证失效 (Authentication Failures) | 3 | 暴力破解、并发攻击、用户枚举 |

#### 6.6.2 SQL注入攻击向量测试（参数化测试）

使用 JUnit 5 参数化测试，批量验证多种 SQL 注入攻击向量：

```java
@ParameterizedTest
@CsvSource({
    "admin' OR '1'='1, SQL注入-万能密码",
    "admin'; DROP TABLE sys_user;--, SQL注入-删表攻击",
    "admin' UNION SELECT * FROM sys_user--, SQL注入-联合查询",
    "admin' AND SLEEP(5)--, SQL注入-时间盲注"
})
void sqlInjectionVectors_ShouldBePrevented(String payload, String attackType)
```

#### 6.6.3 并发安全测试

使用多线程模拟并发攻击场景：

```java
@Test
void concurrentLoginAttack_ThreadSafety() {
    int threadCount = 50;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    // 50个线程同时发起登录攻击
    // 验证系统在高并发下的安全性
}
```

#### 6.6.4 时间侧信道攻击检测

检测用户枚举漏洞（通过响应时间差异）：

```
存在用户响应时间: 160583ns
不存在用户响应时间: 48334ns
风险：响应时间差异可被利用进行用户枚举
```

### 6.7 安全测试总结

#### 6.7.1 测试结果统计

| 测试类别 | 用例数 | 通过 | 失败 | 通过率 |
|----------|--------|------|------|--------|
| 基础安全测试 | 16 | 16 | 0 | 100% |
| OWASP Top 10 测试 | 30 | 30 | 0 | 100% |
| **合计** | **46** | **46** | **0** | **100%** |

#### 6.7.2 发现的安全风险（基于OWASP标准）

| 风险编号 | OWASP分类 | 风险描述 | 严重程度 | 建议措施 |
|----------|-----------|----------|----------|----------|
| RISK-001 | A01 | 水平越权漏洞 | 高 | Service层验证用户身份 |
| RISK-002 | A01 | 垂直越权漏洞 | 高 | 验证操作者角色权限 |
| RISK-003 | A01 | IDOR漏洞 | 高 | 验证资源归属 |
| RISK-004 | A02 | MD5算法安全性较低 | 中 | 升级为bcrypt/Argon2 |
| RISK-005 | A03 | XSS输出未转义 | 中 | 前端展示时HTML转义 |
| RISK-006 | A04 | 业务逻辑漏洞 | 中 | 添加座位数量校验 |
| RISK-007 | A04 | 竞态条件(超卖) | 高 | 实现分布式锁 |
| RISK-008 | A07 | 无账户锁定机制 | 中 | 连续失败后锁定账户 |
| RISK-009 | A07 | 用户枚举漏洞 | 低 | 统一响应时间 |
| RISK-010 | A07 | 无登录审计日志 | 低 | 添加安全审计 |
| RISK-011 | A07 | 弱密码允许注册 | 中 | 实现密码复杂度校验 |
| RISK-012 | A09 | 无安全日志监控 | 低 | 集成SIEM系统 |

---

## 7. 测试执行结果

### 6.1 测试用例执行统计

#### 6.1.1 按模块统计

| 测试类 | 用例数 | 通过 | 失败 | 跳过 | 执行时间 |
|--------|--------|------|------|------|----------|
| **AdvancedSecurityTest (OWASP Top 10)** | **30** | **30** | **0** | **0** | **0.211s** |
| **SecurityTest (基础安全测试)** | **16** | **16** | **0** | **0** | **0.012s** |
| SysUserServiceTest | 21 | 21 | 0 | 0 | 0.010s |
| SysBillServiceTest | 15 | 15 | 0 | 0 | 0.007s |
| SysSessionServiceTest | 11 | 11 | 0 | 0 | 0.015s |
| SysMovieServiceTest | 11 | 11 | 0 | 0 | 0.010s |
| SysMovieCategoryServiceTest | 9 | 9 | 0 | 0 | 0.009s |
| SysCinemaServiceTest | 8 | 8 | 0 | 0 | 0.007s |
| SysHallServiceTest | 10 | 10 | 0 | 0 | 0.010s |
| SysRoleServiceTest | 17 | 17 | 0 | 0 | 0.012s |
| SysResourceServiceTest | 13 | 13 | 0 | 0 | 0.013s |
| CinemaManagerApplicationTests | 1 | 1 | 0 | 0 | 6.948s |
| **合计** | **162** | **162** | **0** | **0** | **7.3s** |

#### 7.1.2 按测试类型统计

| 测试类型 | 用例数 | 占比 |
|----------|--------|------|
| **OWASP安全测试** | **30** | **19%** |
| **基础安全测试** | **16** | **10%** |
| 正常流程测试 | 58 | 36% |
| 异常流程测试 | 24 | 15% |
| 边界条件测试 | 28 | 17% |
| 缺陷探测测试 | 6 | 3% |

### 6.2 覆盖率统计

#### 6.2.1 整体覆盖率

| 度量指标 | 已覆盖 | 未覆盖 | 覆盖率 |
|----------|--------|--------|--------|
| 指令 (Instructions) | 1,510 | 5,093 | 22% |
| 分支 (Branches) | 214 | 551 | 11% |
| 行 (Lines) | 387 | 891 | 30% |
| 方法 (Methods) | 192 | 350 | 35% |
| 类 (Classes) | 48 | 18 | 73% |

#### 6.2.2 分包覆盖率

| 包名 | 指令覆盖 | 分支覆盖 | 说明 |
|------|----------|----------|------|
| com.rabbiter.cm.service.impl | **97%** | **93%** | 测试目标，覆盖率最高 |
| com.rabbiter.cm.domain | 11% | 0% | 实体类，getter/setter |
| com.rabbiter.cm.common.utils | 13% | 10% | 工具类 |
| com.rabbiter.cm.common.config | 90% | n/a | 配置类 |
| com.rabbiter.cm.controller | 4% | 0% | 控制器层，未测试 |
| com.rabbiter.cm.shiro | 8% | 0% | 安全框架，未测试 |

#### 6.2.3 Service 层详细覆盖率

| 服务类 | 指令覆盖 | 分支覆盖 | 方法覆盖 |
|--------|----------|----------|----------|
| SysUserServiceImpl | 98% | 100% | 100% |
| SysBillServiceImpl | 100% | 100% | 100% |
| SysSessionServiceImpl | 100% | 100% | 100% |
| SysMovieServiceImpl | 100% | n/a | 100% |
| SysMovieCategoryServiceImpl | 100% | 100% | 100% |
| SysCinemaServiceImpl | 100% | n/a | 100% |
| SysHallServiceImpl | 100% | 100% | 100% |
| SysRoleServiceImpl | 100% | 100% | 100% |
| SysResourceServiceImpl | 85% | 75% | 100% |

### 6.3 覆盖率分析

#### 6.3.1 高覆盖率原因

Service 层达到 97% 指令覆盖和 93% 分支覆盖的原因：

1. **测试用例设计全面**：覆盖了正常流程、异常流程、边界条件
2. **Mock 技术应用得当**：隔离了外部依赖，专注于业务逻辑测试
3. **分支覆盖意识强**：针对 if-else 分支设计了对应的测试用例

#### 6.3.2 未覆盖代码分析

部分代码未被覆盖的原因：

| 未覆盖代码 | 原因 | 影响 |
|------------|------|------|
| Controller 层 | 本次测试聚焦 Service 层 | 可通过集成测试覆盖 |
| Shiro 安全模块 | 需要完整的认证上下文 | 建议单独测试 |
| 实体类 getter/setter | 简单方法，测试价值低 | 可忽略 |

---

## 8. 缺陷分析

### 8.1 缺陷概述

在测试过程中，通过精心设计的测试用例，发现了 2 个代码缺陷：

| 缺陷编号 | 严重程度 | 所在模块 | 状态 |
|----------|----------|----------|------|
| BUG-001 | 中 | 用户管理 | 已确认 |
| BUG-002 | 低 | 用户管理 | 已确认 |

### 8.2 缺陷 #1：用户名唯一性校验逻辑缺陷

#### 8.2.1 缺陷信息

- **缺陷编号**：BUG-001
- **严重程度**：中
- **所在类**：`SysUserServiceImpl.java`
- **所在方法**：`isUserNameUnique(String userName, Long userId)`
- **发现方式**：边界条件测试

#### 8.2.2 缺陷描述

当数据库中存在多个同名用户（脏数据场景）时，该方法的判断逻辑存在缺陷。方法遍历所有匹配的用户ID，当找到第一个与传入 userId 相等的ID时立即返回 true，而未考虑可能存在其他用户也使用该用户名的情况。

#### 8.2.3 问题代码

```java
public boolean isUserNameUnique(String userName, Long userId) {
    List<Long> userIds = sysUserMapper.checkUserNameUnique(userName);
    for (Long id : userIds) {
        if (id.equals(userId)) {
            return true;  // 缺陷点：提前返回
        }
    }
    return userIds.isEmpty();
}
```

#### 8.2.4 复现场景

```
数据库状态：
- 用户1：userName = "admin"
- 用户3：userName = "admin"  (脏数据)

调用：isUserNameUnique("admin", 1L)

期望结果：false（因为用户3也在使用该用户名）
实际结果：true（方法在检查到用户1时就返回了）
```

#### 8.2.5 根因分析

该方法的设计意图是：
1. 如果用户名没有被任何人使用 → 返回 true
2. 如果用户名只被当前用户使用 → 返回 true（修改自己的信息）
3. 如果用户名被其他用户使用 → 返回 false

但当前实现只满足了场景1和部分场景2，未能正确处理脏数据场景。

#### 8.2.6 修复建议

```java
public boolean isUserNameUnique(String userName, Long userId) {
    List<Long> userIds = sysUserMapper.checkUserNameUnique(userName);
    if (userIds.isEmpty()) {
        return true;  // 没有人使用该用户名
    }
    // 只有一个用户使用该用户名，且是当前用户自己
    return userIds.size() == 1 && userIds.get(0).equals(userId);
}
```

#### 8.2.7 预防措施

1. 在数据库层面添加唯一约束，从根本上防止脏数据
2. 代码审查时关注边界条件处理
3. 增加数据一致性检查的定时任务

---

### 8.3 缺陷 #2：用户更新时密码处理逻辑问题

#### 8.3.1 缺陷信息

- **缺陷编号**：BUG-002
- **严重程度**：低
- **所在类**：`SysUserServiceImpl.java`
- **所在方法**：`updateUser(SysUser sysUser)`
- **发现方式**：数据一致性测试

#### 8.3.2 缺陷描述

当用户更新信息但未修改密码时，由于代码未显式保留原有的 salt 值，导致更新后的用户对象 salt 字段为 null。虽然在当前实现中可能不会直接导致问题（取决于 MyBatis 的更新策略），但存在数据不一致的风险。

#### 8.3.3 问题代码

```java
public int updateUser(SysUser sysUser) {
    // ... 省略校验逻辑
    SysUser originUser = sysUserMapper.findUserById(sysUser.getUserId());
    
    if (!originUser.getPassword().equals(sysUser.getPassword())) {
        // 密码修改时重新加密
        String salt = SaltUtils.getSalt(8);
        Md5Hash md5Hash = new Md5Hash(sysUser.getPassword(), salt, 1024);
        sysUser.setPassword(md5Hash.toHex());
        sysUser.setSalt(salt);
    }
    // 缺陷：else 分支未处理，sysUser.salt 保持为 null
    return sysUserMapper.updateUser(sysUser);
}
```

#### 8.3.4 根因分析

代码仅在密码修改时设置新的 salt 值，但在密码未修改的分支中，未将原有的 salt 值赋给更新对象。这可能导致：

1. 如果 MyBatis 更新时包含 salt 字段 → 数据库中 salt 被置为 null
2. 用户下次登录时，密码验证可能失败

#### 8.3.5 修复建议

```java
if (!originUser.getPassword().equals(sysUser.getPassword())) {
    // 密码修改，重新加密
    String salt = SaltUtils.getSalt(8);
    Md5Hash md5Hash = new Md5Hash(sysUser.getPassword(), salt, 1024);
    sysUser.setPassword(md5Hash.toHex());
    sysUser.setSalt(salt);
} else {
    // 密码未修改，保留原有值
    sysUser.setPassword(originUser.getPassword());
    sysUser.setSalt(originUser.getSalt());
}
```

---

## 9. 结论与展望

### 9.1 实验成果

1. **测试用例设计**：完成 162 个测试用例的设计与实现，其中包含 46 个基于 OWASP Top 10 标准的专业安全测试用例，覆盖功能测试、安全测试、边界条件等多种场景

2. **覆盖率达标**：Service 层指令覆盖率达到 97%，分支覆盖率达到 93%，达到了企业级的测试充分性标准

3. **安全风险发现**：基于 OWASP Top 10 标准发现 12 个安全风险点，涵盖访问控制、加密机制、注入攻击、不安全设计、身份认证等 5 大类别

4. **缺陷发现**：通过测试发现 2 个代码缺陷，并进行了详细的根因分析，提出了修复建议

5. **技术创新**：采用参数化测试、多线程并发测试、时间侧信道分析等高级测试技术，提升了测试的专业性和有效性

6. **团队协作**：5 名成员分工明确，各自负责不同模块的测试，最终整合形成完整的测试报告

### 9.2 经验总结

1. **OWASP 标准的指导价值**：基于 OWASP Top 10 进行安全测试设计，能够系统性地发现安全风险，避免遗漏关键安全问题

2. **Mock 技术的价值**：通过 Mockito 框架隔离外部依赖，使得单元测试快速、稳定、可重复

3. **高级测试技术的效果**：参数化测试、并发测试等技术能够高效验证多种攻击场景，提升测试效率

4. **覆盖率与安全性的关系**：高覆盖率是必要条件但非充分条件，需要结合安全测试才能全面评估系统质量

5. **边界条件的重要性**：本次发现的缺陷和安全风险多数通过边界条件和异常场景测试发现

6. **测试即文档**：良好的测试用例命名和注释，可以作为代码行为和安全要求的文档

### 9.3 改进建议

1. **扩展安全测试覆盖**：补充 OWASP Top 10 中其余 5 项风险的测试用例，实现完整覆盖

2. **引入自动化安全扫描**：集成 OWASP ZAP、SonarQube 等工具进行静态和动态安全分析

3. **扩展测试范围**：增加 Controller 层的集成测试和端到端的安全测试

4. **持续安全测试**：将安全测试集成到 CI/CD 流程中，实现 DevSecOps

5. **性能与安全结合**：针对高并发场景进行性能测试，同时验证安全机制在高负载下的有效性

6. **威胁建模**：基于 STRIDE 等方法进行系统性的威胁建模，指导安全测试设计

---

## 附录

### A. 团队分工

| 成员 | 负责模块 | 工作内容 |
|------|----------|----------|
| **张春冉** | **OWASP安全测试 + 用户模块 + 项目统筹** | AdvancedSecurityTest (30个OWASP用例) + SecurityTest (16个用例) + SysUserServiceTest (21个用例) + 测试框架搭建 + 报告第5.1节 + 报告第6章(安全测试) + 缺陷分析 + 报告整合 |
| 成员B | 订单/场次模块 | SysBillServiceTest + SysSessionServiceTest (26个用例) + 报告第5.2节 |
| 成员C | 电影模块 | SysMovieServiceTest + SysMovieCategoryServiceTest (20个用例) + 报告第5.3节 |
| 成员D | 影院/影厅模块 | SysCinemaServiceTest + SysHallServiceTest (18个用例) + 报告第5.4节 |
| 成员E | 角色权限模块 | SysRoleServiceTest + SysResourceServiceTest (30个用例) + 报告第5.5节 |

### B. 项目结构

```
CinemaManagerApi/
├── pom.xml                                    # Maven 配置
├── src/
│   ├── main/java/com/rabbiter/cm/
│   │   ├── CinemaManagerApplication.java     # 启动类
│   │   ├── controller/                       # 控制器层
│   │   ├── service/                          # 服务层接口
│   │   │   └── impl/                         # 服务层实现
│   │   ├── mapper/                           # 数据访问层
│   │   ├── domain/                           # 实体类
│   │   ├── common/                           # 公共组件
│   │   └── shiro/                            # 安全框架
│   └── test/java/com/rabbiter/cm/
│       ├── CinemaManagerApplicationTests.java
│       └── service/                          # 单元测试
│           ├── SysUserServiceTest.java
│           ├── SysBillServiceTest.java
│           ├── SysSessionServiceTest.java
│           ├── SysMovieServiceTest.java
│           ├── SysMovieCategoryServiceTest.java
│           ├── SysCinemaServiceTest.java
│           ├── SysHallServiceTest.java
│           ├── SysRoleServiceTest.java
│           └── SysResourceServiceTest.java
└── target/
    └── site/jacoco/                          # 覆盖率报告
```

### C. 运行指南

```bash
# 1. 进入项目目录
cd code/CinemaManagerApi

# 2. 执行测试
mvn clean test

# 3. 查看覆盖率报告
open target/site/jacoco/index.html

# 4. 生成测试报告
mvn surefire-report:report
```

### D. 参考资料

1. JUnit 5 User Guide. https://junit.org/junit5/docs/current/user-guide/
2. Mockito Documentation. https://site.mockito.org/
3. JaCoCo Documentation. https://www.jacoco.org/jacoco/trunk/doc/
4. Spring Boot Testing. https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
5. 软件测试（第2版）. 朱少民. 清华大学出版社, 2019.

---

*报告完成日期：2025年12月*
