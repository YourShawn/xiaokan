# 小砍 · AI砍价模拟器

拍一下，看看这件衣服多少钱值得拿下。

店里拍衣服 / 价签 / 水洗标（1～3 张）→ 小砍给出开口、目标区间和心理上限 → 去砍价（线下，应用内没有对话）→ 回来记下拿下了没有。

本仓库是独立 MVP，与其它项目无关。

## 功能

- 首页 CTA「这件衣服，能砍到多少？」+ 相机/相册拍 1～3 张
- 价签看不清会轻声提醒补拍，可以跳过
- 结果页：品牌、吊牌价、面料、小砍建议（opening / target min–max / max / bargain_score / 短理由）
- 「去砍价」进入成交记录：拿下了（填最终价，展示省了 ¥X）或没买
- 扫描足迹
- 视觉 API Key 走真实识别；**没有 Key 时自动模拟模式**，不把模拟价当成实价
- 真实视觉路径：**看不清价签就不填写 `list_price`**，绝不编造吊牌价
- 扫描、建议、是否成交、成交价写入 MySQL（砍价数据沉淀）

不做：砍价对话、实时教练、二手、社区、付费、会员、语音、AR。

## 技术栈

Java 21 · Spring Boot 3 MVC · MySQL 8 · React 19 + Vite · docker compose

## 本地运行（推荐先走模拟模式）

MySQL 连 **本机 loopback**，不要把公网 VPS IP 写进仓库。

```bash
cp .env.example .env
# 准备好本机 MySQL，库名/账号与 .env 一致，例如：
#   CREATE DATABASE xiaokan CHARACTER SET utf8mb4;
#   CREATE USER 'xiaokan'@'localhost' IDENTIFIED BY 'xiaokan';
#   GRANT ALL ON xiaokan.* TO 'xiaokan'@'localhost';

# 后端（不要设置 OPENAI_API_KEY → 模拟模式）
cd backend
MYSQL_HOST=127.0.0.1 MYSQL_DATABASE=xiaokan MYSQL_USER=xiaokan MYSQL_PASSWORD=xiaokan \
  mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

打开 http://127.0.0.1:5173 ，拍或选 1～3 张衣服照片，走完「建议 → 去砍价 → 拿下了/没买 → 足迹」。

## Docker Compose

默认 `docker compose up --build` 期望宿主机已有 MySQL（`MYSQL_HOST`，compose 里默认 `host.docker.internal`，只应是私网/本机）。

本机演示把 MySQL 一起拉起（仍只绑定 `127.0.0.1:3306`）：

```bash
cp .env.example .env
# 让后端容器连 compose 网络里的 mysql 服务
MYSQL_HOST=mysql docker compose --profile bundled-mysql up --build
```

浏览器打开 http://127.0.0.1 。

## 环境变量

见 `.env.example`。

| 变量 | 说明 |
| --- | --- |
| `MYSQL_HOST` | 本机用 `127.0.0.1`；compose 连宿主机用 `host.docker.internal`；bundled profile 用 `mysql` |
| `OPENAI_API_KEY` | 为空 = 模拟模式；填写后走 OpenAI 兼容视觉接口 |
| `OPENAI_BASE_URL` / `OPENAI_MODEL` | 兼容网关与模型名 |
| `UPLOAD_DIR` | 照片落地目录 |

## API 摘要

`POST /api/scans` `multipart photos`（1～3）

返回 JSON 字段：`brand`, `list_price`, `opening_offer`, `target_min`, `target_max`, `max_price`, `bargain_score`, `reasons[]`，以及面料、是否需补拍价签、`mock_mode`。

`POST /api/scans/{id}/deal` `{ "bought": true, "final_price": 79 }` 或 `{ "bought": false }`

`GET /api/scans` 足迹 · `GET /api/status` 是否模拟模式

## 许可

MIT
