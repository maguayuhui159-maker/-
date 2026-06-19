$ErrorActionPreference = "Stop"

Write-Host "[1/5] Checking Docker..."
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  throw "Docker is not installed."
}

try {
  docker info *> $null
} catch {
  throw "Docker daemon is not running. Please start Docker Desktop first."
}

Write-Host "[2/5] Checking AI API key..."
if (-not (Test-Path ".env")) {
  throw ".env not found. Please create .env from .env.example."
}

$envText = Get-Content ".env" -Raw
$hasArkKey = $envText -match "(?m)^ARK_API_KEY=.+$"
$hasOpenAIKey = $envText -match "(?m)^OPENAI_API_KEY=.+$"
$hasDeepSeekKey = $envText -match "(?m)^DEEPSEEK_API_KEY=.+$"
if (-not $hasArkKey -and -not $hasOpenAIKey -and -not $hasDeepSeekKey) {
  throw "None of ARK_API_KEY / OPENAI_API_KEY / DEEPSEEK_API_KEY is set in .env."
}

$mysqlRootPassword = "rootpassword"
foreach ($line in (Get-Content ".env")) {
  if ($line -match "^MYSQL_ROOT_PASSWORD=(.*)$") {
    $mysqlRootPassword = $Matches[1]
    break
  }
}

Write-Host "[3/5] Building and starting containers..."
docker compose up -d --build
docker compose restart nginx

Write-Host "[4/6] Repairing MySQL charset and importing demo seed..."
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

Write-Host "[5/6] Service status:"
docker compose ps

Write-Host "[6/6] Health checks..."
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
  Write-Host "AI health check: OK"
} else {
  Write-Warning "AI health check: FAILED"
}

if (Test-UrlWithRetry -Url "http://localhost/api/health") {
  Write-Host "Backend health check: OK"
} else {
  Write-Warning "Backend health check: FAILED"
}

Write-Host "Deployment finished."
Write-Host "Admin Web:   http://localhost/"
Write-Host "Backend API: http://localhost/api/health"
Write-Host "AI Service:  http://localhost/api/ai/health"
