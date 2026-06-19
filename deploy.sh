#!/usr/bin/env bash
set -euo pipefail

echo "[1/5] 检查 Docker 环境..."
if ! command -v docker >/dev/null 2>&1; then
  echo "未安装 Docker，请先安装 Docker Desktop。"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker 服务未运行，请先启动 Docker Desktop。"
  exit 1
fi

echo "[2/5] 检查 AI 接口密钥..."
if [ ! -f ".env" ]; then
  echo "未找到 .env 文件，请先复制 .env.example 并填写配置。"
  exit 1
fi

if ! grep -Eq '^ARK_API_KEY=.+$|^OPENAI_API_KEY=.+$|^DEEPSEEK_API_KEY=.+$' .env; then
  echo ".env 中未配置 ARK_API_KEY / OPENAI_API_KEY / DEEPSEEK_API_KEY，请至少填写一个。"
  exit 1
fi

MYSQL_ROOT_PASSWORD_VALUE="$(
  awk -F= '/^MYSQL_ROOT_PASSWORD=/{print substr($0, index($0, "=") + 1); exit}' .env
)"
MYSQL_ROOT_PASSWORD_VALUE="${MYSQL_ROOT_PASSWORD_VALUE:-rootpassword}"

echo "[3/5] 构建并启动容器..."
docker compose up -d --build
docker compose restart nginx

echo "[4/6] 修复 MySQL 字符集并导入演示数据..."
for i in $(seq 1 30); do
  if docker compose exec -T mysql sh -lc 'mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent' >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
docker compose exec -T -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD_VALUE" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db' < fix_charset.sql || echo "警告：fix_charset.sql 导入失败，将继续使用运行时兜底逻辑。"
docker compose exec -T -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD_VALUE" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db' < seed_demo_data.sql || echo "警告：seed_demo_data.sql 导入失败，将继续使用已有演示数据。"

echo "[5/6] 当前服务状态："
docker compose ps

echo "[6/6] 健康检查："
check_with_retry() {
  local url="$1"
  local name="$2"
  local retries=15
  local i=1
  while [ "$i" -le "$retries" ]; do
    if curl -fsS "$url" >/dev/null; then
      echo "$name 健康检查：通过"
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done
  echo "$name 健康检查：失败"
  return 1
}

check_with_retry "http://localhost/api/ai/health" "AI 服务" || true
check_with_retry "http://localhost/api/health" "后端服务" || true

echo "部署流程执行完成。"
echo "前端页面：          http://localhost/"
echo "后端健康检查：      http://localhost/api/health"
echo "AI 服务健康检查：   http://localhost/api/ai/health"
