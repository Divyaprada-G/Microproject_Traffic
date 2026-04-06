# Smart City Traffic System - Execution Guide

This project is a full-stack, microservices-based traffic monitoring system.

## Components
1. **Backend (Java Spring Boot)**:
   - `api-gateway`: Port 8080 (WebSocket + REST)
   - `producer-service`: Traffic simulation data generator
   - `analytics-engine`: Spark-like processing
   - `fault-detector`: Health monitoring
2. **Frontend (React + Vite)**:
   - Dashboard: Port 3000
3. **Infrastructure**:
   - `Kafka`: Message broker
   - `Redis`: Fast cache
   - `Regional Nodes`: Storage

## How to Run

### 1. Prerequisite: Start Docker Desktop
Ensure Docker Desktop is running on your machine. If it is not running, the backend services will not be able to connect to Kafka and Redis.

### 2. Run the total project
Open a PowerShell terminal in the root directory and run:
```powershell
./run_total_project.ps1
```

### 3. Access the Dashboard
Once the services are up, open:
[http://localhost:3000](http://localhost:3000)

## Features Included
- **Real-time Vehicle Simulation Map**: Leaflet-based map with moving vehicles in Bengaluru.
- **5-Second Auto Refresh**: All metrics and charts update every 5 seconds.
- **Analytics Filters**: Today, Week, Month, and Custom filters are now functional (simulated with historical-skewed data).
- **Fault-Tolerant Architecture**: High availability via geo-distributed storage nodes.
