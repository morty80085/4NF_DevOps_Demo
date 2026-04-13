# Spring Boot + MySQL CI/CD Demo

本仓库用于测试 7 人协作的 CI/CD，核心工作流文件为 [ci-demo.yml](.github/workflows/ci-demo.yml) 和 [cd-demo.yml](.github/workflows/cd-demo.yml)。

![CI Workflow](https://github.com/BUAA2026SE-404NotFound/4NF_DevOps_Demo/actions/workflows/ci-demo.yml/badge.svg)

![CD Workflow](https://github.com/BUAA2026SE-404NotFound/4NF_DevOps_Demo/actions/workflows/cd-demo.yml/badge.svg)

## CI/CD 触发记录

- [ ] CTS-23373
- [x] kurrna
- [ ] morty80085
- [ ] Noel261
- [ ] REROSAMA
- [ ] wangsu2006
- [ ] xx6677-c

## 单次 CI/CD 触发全过程

一次完整触发（例如 `push master` 或 `workflow_dispatch`）按以下顺序执行：

1. 触发工作流：`pull_request` / `push` / `workflow_dispatch`。
2. `ci` 阶段：
    - 启动 MySQL `service`（`mysql:8.4`）。
    - 检查数据库连通性。
    - 执行 `./mvnw -B clean test`（包含 MySQL 冒烟测试）。
    - 执行 `./mvnw -B -DskipTests package` 打包 Jar。
    - 上传 `target/*.jar` 作为 `app-jar` artifact。
3. `cd-smoke-test` 阶段（非 PR）：
    - 下载 `app-jar`。
    - 启动应用并连接 MySQL。
    - 访问 `/api/tasks` 验证服务可用。
    - 调用 `/api/tasks` 的 POST/GET 做端到端冒烟校验。
4. `deploy` 阶段（非 PR）：
    - 下载 Jar 并统一命名为 `app.jar`。
    - 使用 `SERVER_HOST`、`SERVER_USER`、`SERVER_PASSWORD` 上传到腾讯云。
    - SSH 登录服务器，停止旧进程并后台启动新版本。

## 工作流配置说明

- 触发条件：
    - `pull_request` 到 `master`：执行 `ci`。
    - `push` 到 `master`：执行 `ci -> cd-smoke-test -> deploy`。
    - `workflow_dispatch`：可手动选择成员槽位触发。
- 关键环境变量：
    - `RUN_CI_MYSQL_TEST=true`：启用 MySQL CI 冒烟测试。
    - `SPRING_PROFILES_ACTIVE=ci`：在 CI/CD 阶段使用 `application-ci.yaml`。
- 必需 Secrets（部署到腾讯云服务器）：
    - `SERVER_HOST`
    - `SERVER_USER`
    - `SERVER_PASSWORD`
- 产物流转：
    - `ci` 上传 `app-jar`。
    - 后续 job 下载同一 artifact，保证“测试通过的同一个包”被部署。

