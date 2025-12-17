# Software-Testing-Experiment

软件测试课程实验 - 基于电影院售票系统的单元测试与覆盖率分析

---

## 免责声明

**本仓库仅用于软件测试课程实验学习目的。**

- `code/` 目录下的电影院售票管理系统并非本人原创开发，来源于开源项目
- 本仓库的贡献仅限于：测试用例编写、覆盖率分析、缺陷发现与实验报告
- 原项目版权归原作者所有，本仓库不对原项目代码负责
- 如有侵权，请联系删除

---

## 仓库说明

本仓库是软件测试课程实验的完整交付物，包含：

| 文件/目录 | 说明 |
|-----------|------|
| `REPORT.md` | 实验报告（完整版，含安全测试章节） |
| `TEST_PLAN.md` | 测试分工方案 |
| `REQUIREMENTS.md` | 实验要求 |
| `code/CinemaManagerApi/src/test/` | 测试代码（132个测试用例，含16个安全测试） |
| `code/CinemaManagerApi/target/site/jacoco/` | 覆盖率报告 |

---

## 实验成果

| 指标 | 数值 |
|------|------|
| 测试用例总数 | 132 |
| 其中安全测试用例 | 16 |
| 测试通过率 | 100% |
| Service层指令覆盖率 | 97% |
| Service层分支覆盖率 | 93% |
| 发现的代码缺陷 | 2 |
| 发现的安全风险 | 4 |

---

## 快速开始

```bash
# 克隆仓库
git clone <repo-url>

# 进入后端项目
cd code/CinemaManagerApi

# 运行测试
mvn clean test

# 查看覆盖率报告
open target/site/jacoco/index.html
```

---

## 项目结构

```
.
├── README.md                 # 本文件
├── REPORT.md                 # 实验报告
├── TEST_PLAN.md              # 测试分工
├── REQUIREMENTS.md           # 实验要求
└── code/
    ├── CinemaManagerApi/     # 后端项目 + 测试代码
    ├── CinemaManagerAdminVue/  # 管理端前端（未测试）
    ├── CinemaManagerUserVue/   # 用户端前端（未测试）
    └── cinema_manager.sql    # 数据库脚本
```

---

## 团队分工

| 成员 | 负责模块 | 测试用例数 |
|------|----------|-----------|
| 成员A | 用户模块 | 21 |
| 成员B | 订单/场次模块 | 26 |
| 成员C | 电影模块 | 20 |
| 成员D | 影院/影厅模块 | 18 |
| 成员E | 角色权限模块 | 30 |

---

## License

- 测试代码和实验报告：MIT License
- 原项目代码：遵循原项目许可证

---

*软件测试课程实验 | 2025年12月*
