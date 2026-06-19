#!/usr/bin/env bash
set -euo pipefail

echo "[1/5] Checking Docker..."
if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed."
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is not running. Please start Docker first."
  exit 1
fi

echo "[2/5] Checking AI API key..."
if [ ! -f ".env" ]; then
  echo ".env not found. Please create .env from .env.example."
  exit 1
fi

if ! grep -Eq '^ARK_API_KEY=.+$|^OPENAI_API_KEY=.+$|^DEEPSEEK_API_KEY=.+$' .env; then
  echo "None of ARK_API_KEY / OPENAI_API_KEY / DEEPSEEK_API_KEY is set in .env."
  exit 1
fi

MYSQL_ROOT_PASSWORD_VALUE="$(
  awk -F= '/^MYSQL_ROOT_PASSWORD=/{print substr($0, index($0, "=") + 1); exit}' .env
)"
MYSQL_ROOT_PASSWORD_VALUE="${MYSQL_ROOT_PASSWORD_VALUE:-rootpassword}"

echo "[3/5] Building and starting containers..."
docker compose up -d --build
docker compose restart nginx

echo "[4/6] Repairing MySQL charset and importing demo seed..."
for i in $(seq 1 30); do
  if docker compose exec -T mysql sh -lc 'mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent' >/dev/null 2>&1; then
    break
  fi
  sleep 2
done
docker compose exec -T -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD_VALUE" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db' < fix_charset.sql || echo "WARN: fix_charset.sql import failed, continue with runtime fallback."
docker compose exec -T -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD_VALUE" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db' < seed_demo_data.sql || echo "WARN: seed_demo_data.sql import failed, continue with existing data."

echo "[5/6] Service status:"
docker compose ps

echo "[6/6] Health checks:"
check_with_retry() {
  local url="$1"
  local name="$2"
  local retries=15
  local i=1
  while [ "$i" -le "$retries" ]; do
    if curl -fsS "$url" >/dev/null; then
      echo "$name health check: OK"
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done
  echo "$name health check: FAILED"
  return 1
}

check_with_retry "http://localhost/api/ai/health" "AI" || true
check_with_retry "http://localhost/api/health" "Backend" || true

echo "Deployment finished."
echo "Admin Web:   http://localhost/"
echo "Backend API: http://localhost/api/health"
echo "AI Service:  http://localhost/api/ai/health"
