$ErrorActionPreference = "Stop"

Write-Host "[1/5] 检查 Docker 环境..."
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  throw "未安装 Docker，请先安装 Docker Desktop。"
}

try {
  docker info *> $null
} catch {
  throw "Docker 服务未运行，请先启动 Docker Desktop。"
}

Write-Host "[2/5] 检查 AI 接口密钥..."
if (-not (Test-Path ".env")) {
  throw "未找到 .env 文件，请先复制 .env.example 并填写配置。"
}

$envText = Get-Content ".env" -Raw
$hasArkKey = $envText -match "(?m)^ARK_API_KEY=.+$"
$hasOpenAIKey = $envText -match "(?m)^OPENAI_API_KEY=.+$"
$hasDeepSeekKey = $envText -match "(?m)^DEEPSEEK_API_KEY=.+$"
if (-not $hasArkKey -and -not $hasOpenAIKey -and -not $hasDeepSeekKey) {
  throw ".env 中未配置 ARK_API_KEY / OPENAI_API_KEY / DEEPSEEK_API_KEY，请至少填写一个。"
}

$mysqlRootPassword = "rootpassword"
foreach ($line in (Get-Content ".env")) {
  if ($line -match "^MYSQL_ROOT_PASSWORD=(.*)$") {
    $mysqlRootPassword = $Matches[1]
    break
  }
}

Write-Host "[3/5] 构建并启动容器..."
docker compose up -d --build
docker compose restart nginx

Write-Host "[4/6] 修复 MySQL 字符集并导入演示数据..."
for ($i = 1; $i -le 30; $i++) {
  docker compose exec -T -e MYSQL_ROOT_PASSWORD="$mysqlRootPassword" mysql sh -lc 'mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent' *> $null
  if ($LASTEXITCODE -eq 0) { break }
  Start-Sleep -Seconds 2
}
try {
  Get-Content -Raw "fix_charset.sql" | docker compose exec -T -e MYSQL_ROOT_PASSWORD="$mysqlRootPassword" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db'
} catch {
  Write-Warning "fix_charset.sql 导入失败，继续使用运行时兜底逻辑。"
}
try {
  Get-Content -Raw "seed_demo_data.sql" | docker compose exec -T -e MYSQL_ROOT_PASSWORD="$mysqlRootPassword" mysql sh -lc 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 jiangmai_db'
} catch {
  Write-Warning "seed_demo_data.sql 导入失败，继续使用现有演示数据。"
}

Write-Host "[5/6] 当前服务状态："
docker compose ps

Write-Host "[6/6] 健康检查..."
function Test-UrlWithRetry {
  param(
    [string]$Url,
    [int]$MaxRetry = 15,
    [int]$SleepSeconds = 2
  )
  for ($i = 1; $i -le $MaxRetry; $i++) {
    try {
      Invoke-WebRequest -UseBasicParsing $Url | Out-Null
      return $true
    } catch {
      Start-Sleep -Seconds $SleepSeconds
    }
  }
  return $false
}

if (Test-UrlWithRetry -Url "http://localhost/api/ai/health") {
  Write-Host "AI 服务健康检查：通过"
} else {
  Write-Warning "AI 服务健康检查：失败"
}

if (Test-UrlWithRetry -Url "http://localhost/api/health") {
  Write-Host "后端服务健康检查：通过"
} else {
  Write-Warning "后端服务健康检查：失败"
}

Write-Host "部署流程执行完成。"
Write-Host "前端页面：          http://localhost/"
Write-Host "后端健康检查：      http://localhost/api/health"
Write-Host "AI 服务健康检查：   http://localhost/api/ai/health"
