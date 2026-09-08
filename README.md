# 行李寄存系统

本项目采用 Spring Boot 3、MyBatis-Plus、MySQL 8、Vue 3、Element Plus 实现。

## 一、初始化数据库

在项目根目录打开 PowerShell，进入 MySQL：

```powershell
mysql -u root -p
```

输入本机 MySQL 密码后，在 MySQL 控制台依次执行：

```sql
source database/01_schema_member_a.sql;
source database/02_schema_member_b.sql;
```

如果当前目录或 MySQL 客户端无法识别相对路径，请将脚本拖入终端，使用生成的完整路径执行。

## 二、启动后端

在项目根目录执行（密码只在当前 PowerShell 窗口生效）：

```powershell
$env:DB_PASSWORD = "你的MySQL密码"
.\mvnw.cmd spring-boot:run
```

后端默认地址为 `http://localhost:8080`。

## 三、启动前端

另开一个 PowerShell 窗口，在项目根目录执行：

```powershell
npm.cmd --prefix .\luggage-frontend install
npm.cmd --prefix .\luggage-frontend run dev
```

根据终端提示访问前端地址，通常为 `http://localhost:5173`。

## 四、创建演示管理员

先通过注册页创建一个普通账号，例如用户名 `admin`，再进入 MySQL 执行：

```sql
USE luggage_system;
UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin';
```

退出后重新登录，该账号即可进入管理员页面。

## 五、提交前检查

```powershell
$env:DB_PASSWORD = "你的MySQL密码"
.\mvnw.cmd clean test
npm.cmd --prefix .\luggage-frontend run build
git status
```

不要将 MySQL 密码写入 `application.yaml` 或提交到 GitHub。
