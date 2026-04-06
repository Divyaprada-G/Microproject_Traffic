# Smart City Traffic System - Full Execution Script

Write-Host "===========================================================" -ForegroundColor Green
Write-Host "INITIALIZING SMART CITY TRAFFIC SYSTEM" -ForegroundColor Green
Write-Host "===========================================================" -ForegroundColor Green

# 1. Start Docker Infrastructure
Write-Host "[1/3] Starting Docker Compose infrastructure (Kafka, Redis, Backend)..." -ForegroundColor Yellow
docker compose up -d --build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Could not start Docker infrastructure. Please ensure Docker Desktop is running." -ForegroundColor Red
} else {
    Write-Host "Docker infrastructure started successfully." -ForegroundColor Cyan
}

# 2. Build and Start Development UI
Write-Host "[2/3] Installing frontend dependencies and starting Dashboard..." -ForegroundColor Yellow
Set-Location "./Design Smart City Traffic Dashboard"
npm install
npm run dev

# 3. Done
Write-Host "[3/3] Done!" -ForegroundColor Green
Write-Host "Open: http://localhost:3000 to view the dashboard." -ForegroundColor Green
