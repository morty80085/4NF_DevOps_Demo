# Spring Boot + MySQL CI/CD Demo

本仓库用于完成 7 人协作的 CI/CD 作业，核心工作流文件为 [ci-cd-demo.yml](.github/workflows/ci-cd-demo.yml)。

![CI/CD Workflow](https://github.com/BUAA2026SE-404NotFound/4NF_DevOps_Demo/actions/workflows/ci-cd-demo.yml/badge.svg)

![CI/CD Workflow Main](https://github.com/BUAA2026SE-404NotFound/4NF_DevOps_Demo/actions/workflows/ci-cd-demo.yml/badge.svg?branch=main)

## 单次 CI/CD 触发全过程

一次完整触发（例如 `push main` 或 `workflow_dispatch`）按以下顺序执行：

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
    - 访问 `/actuator/health`。
    - 调用 `/api/tasks` 的 POST/GET 做端到端冒烟校验。
4. `deploy` 阶段（非 PR）：
    - 下载 Jar 并统一命名为 `app.jar`。
    - 使用 `SERVER_HOST`、`SERVER_USER`、`SERVER_PASSWORD` 上传到腾讯云。
    - SSH 登录服务器，停止旧进程并后台启动新版本。

## 工作流配置说明

- 触发条件：
    - `pull_request` 到 `main`：执行 `ci`。
    - `push` 到 `main`：执行 `ci -> cd-smoke-test -> deploy`。
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

## `ci-cd-demo.yml` 文件内容

```yaml
name: CI/CD Demo

on:
  pull_request:
    branches: [ "main" ]
  push:
    branches: [ "main" ]
  workflow_dispatch:
    inputs:
      member:
        description: "Select your member slot (replace labels with real GitHub IDs if needed)"
        required: true
        type: choice
        options:
          - member-1
          - member-2
          - member-3
          - member-4
          - member-5
          - member-6
          - member-7

jobs:
  ci:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.4
        ports:
          - 3306:3306
        env:
          MYSQL_ROOT_PASSWORD: 123456
          MYSQL_DATABASE: devops_demo
        options: >-
          --health-cmd="mysqladmin ping -h 127.0.0.1 -uroot -p123456"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=10

    env:
      RUN_CI_MYSQL_TEST: true
      SPRING_PROFILES_ACTIVE: ci

    steps:
      - name: Checkout source
        uses: actions/checkout@v4

      - name: Setup JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Print trigger info
        run: |
          echo "GitHub actor: ${{ github.actor }}"
          echo "Member slot input: ${{ github.event.inputs.member || 'N/A (push or pull_request)' }}"

      - name: Wait for MySQL
        run: |
          for i in {1..30}; do
            if mysql -h127.0.0.1 -P3306 -uroot -p123456 -e "SELECT 1" devops_demo; then
              echo "MySQL is ready"
              exit 0
            fi
            echo "Waiting for MySQL..."
            sleep 2
          done
          echo "MySQL did not become ready in time"
          exit 1

      - name: Run tests
        run: |
          chmod +x mvnw
          ./mvnw -B clean test

      - name: Build jar
        run: ./mvnw -B -DskipTests package

      - name: Upload jar artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar
          if-no-files-found: error

  cd-smoke-test:
    if: github.event_name != 'pull_request'
    needs: ci
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.4
        ports:
          - 3306:3306
        env:
          MYSQL_ROOT_PASSWORD: 123456
          MYSQL_DATABASE: devops_demo
        options: >-
          --health-cmd="mysqladmin ping -h 127.0.0.1 -uroot -p123456"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=10

    env:
      SPRING_PROFILES_ACTIVE: ci

    steps:
      - name: Download jar artifact
        uses: actions/download-artifact@v4
        with:
          name: app-jar
          path: dist

      - name: Select runnable jar
        run: |
          APP_JAR=$(find dist -name "*.jar" | grep -v "original" | head -n 1)
          if [ -z "$APP_JAR" ]; then
            echo "No runnable jar found"
            exit 1
          fi
          echo "APP_JAR=$APP_JAR" >> $GITHUB_ENV

      - name: Wait for MySQL
        run: |
          for i in {1..30}; do
            if mysql -h127.0.0.1 -P3306 -uroot -p123456 -e "SELECT 1" devops_demo; then
              echo "MySQL is ready"
              exit 0
            fi
            sleep 2
          done
          echo "MySQL did not become ready in time"
          exit 1

      - name: Start app
        run: |
          java -jar "$APP_JAR" > app.log 2>&1 &
          echo $! > app.pid

      - name: Health and API smoke test
        run: |
          for i in {1..30}; do
            if curl -fsS http://127.0.0.1:8080/actuator/health > /dev/null; then
              break
            fi
            sleep 2
          done
          curl -fsS http://127.0.0.1:8080/actuator/health
          curl -fsS -X POST http://127.0.0.1:8080/api/tasks \
            -H "Content-Type: application/json" \
            -d '{"title":"cd smoke"}'
          curl -fsS http://127.0.0.1:8080/api/tasks

      - name: Stop app and print logs
        if: always()
        run: |
          if [ -f app.pid ]; then kill $(cat app.pid) || true; fi
          echo "----- app.log -----"
          cat app.log || true

  deploy:
    if: github.event_name != 'pull_request'
    needs: cd-smoke-test
    runs-on: ubuntu-latest

    steps:
      - name: Download jar artifact
        uses: actions/download-artifact@v4
        with:
          name: app-jar
          path: dist

      - name: Prepare deployment jar
        run: |
          APP_JAR=$(find dist -name "*.jar" | grep -v "original" | head -n 1)
          if [ -z "$APP_JAR" ]; then
            echo "No runnable jar found"
            exit 1
          fi
          cp "$APP_JAR" dist/app.jar

      - name: Upload jar to Tencent Cloud server
        if: ${{ secrets.SERVER_HOST != '' && secrets.SERVER_USER != '' && secrets.SERVER_PASSWORD != '' }}
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          password: ${{ secrets.SERVER_PASSWORD }}
          source: "dist/app.jar"
          target: "~/apps/devops-demo"

      - name: Restart service on Tencent Cloud server
        if: ${{ secrets.SERVER_HOST != '' && secrets.SERVER_USER != '' && secrets.SERVER_PASSWORD != '' }}
        uses: appleboy/ssh-action@v1.2.0
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          password: ${{ secrets.SERVER_PASSWORD }}
          script: |
            mkdir -p ~/apps/devops-demo
            cd ~/apps/devops-demo
            if [ -f app.pid ] && kill -0 "$(cat app.pid)" 2>/dev/null; then
              kill "$(cat app.pid)" || true
              sleep 2
            fi
            nohup java -jar app.jar > app.log 2>&1 &
            echo $! > app.pid
            sleep 5
            ps -p "$(cat app.pid)" -f || true

      - name: Deploy skipped reminder
        if: ${{ !(secrets.SERVER_HOST != '' && secrets.SERVER_USER != '' && secrets.SERVER_PASSWORD != '') }}
        run: |
          echo "SERVER_HOST / SERVER_USER / SERVER_PASSWORD 未配置，已跳过自动部署。"
```

## 7 人触发 CI/CD 记录表

| 序号 | 成员 GitHub ID | 触发方式（push/dispatch） | Actions Run 链接 | CI 结果 | CD 结果 | 部署结果 | 备注 |
|----|--------------|---------------------|----------------|-------|-------|------|----|
| 1  |              |                     |                |       |       |      |    |
| 2  |              |                     |                |       |       |      |    |
| 3  |              |                     |                |       |       |      |    |
| 4  |              |                     |                |       |       |      |    |
| 5  |              |                     |                |       |       |      |    |
| 6  |              |                     |                |       |       |      |    |
| 7  |              |                     |                |       |       |      |    |

