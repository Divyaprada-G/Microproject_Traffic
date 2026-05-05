# Fault-Tolerant Geo-Distributed Traffic Data Management System

This project simulates a highly scalable, geo-distributed traffic data management infrastructure for Indian Smart Cities. It uses a modern microservices architecture with Spring Boot, Kafka, Redis, and Docker.

## Technology Stack

**Frontend (Dashboard):**
- React 18 & TypeScript
- Vite
- Tailwind CSS
- Material UI (MUI) & Radix UI
- Recharts (Analytics Data Visualization)
- React Leaflet (Live Interactive Map) 

**Backend (Microservices):**
- Java & Spring Boot (Web, Data JPA)
- PostgreSQL (Database)
- Apache Kafka & Zookeeper (Event Streaming)
- Redis (Caching)

**Infrastructure & Deployment:**
- Docker & Docker Compose

## Architecture & Components

- **Producer Service:** Simulates 10,000+ vehicles generating real-time traffic data, heavily favoring the Karnataka/Bengaluru region.
- **Ingestion Service:** Entry point for traffic data. Validates and pushes events into the `traffic-events` Kafka topic.
- **Storage Nodes:** Regional storage containers (`server-blr`, `server-del`, `server-mum`). Consumes from Kafka based on the specified region and stores JSON chunks.
- **Replication Manager:** Reads replication queues from the primary node and propagates records across regions (BLR -> DEL -> MUM) for fault tolerance.
- **Fault Detector:** Acts as a central health monitor. All nodes send a heartbeat every 5s. Triggers failures and aids failover routing.
- **Analytics Engine:** Processes the Kafka stream to build live real-time trends for state-wise statistics and congestion levels.
- **API Gateway:** Centralized Gateway exposing REST APIs and WebSockets. Uses Redis to cache expensive DB lookups and handles dynamic failover when a region goes offline.
- **Frontend / UI:** Connects via REST/Websockets to monitor the entire system on a React Leaflet map.

## Technological Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5** & **Spring Cloud**
- **Maven** (Build Tool)

### Messaging & Event Streaming
- **Apache Kafka 3.5** (Message Broker)
- **Apache Zookeeper 3.8** (Coordination)

### Data Storage & Caching
- **PostgreSQL 15** (Relational Database)
- **Redis 7** (In-Memory Data Structure Store / Cache)

### Frontend (Smart City Traffic Dashboard)
- **React 18** & **TypeScript**
- **Vite 6** (Build Tool)
- **Tailwind CSS 4** (Styling)
- **Material UI (MUI)** & **Radix UI** (Component Libraries)
- **Recharts** (Data Visualization)
- **Leaflet & React Leaflet** (Mapping)
- **React Router 7** (Routing)

### Infrastructure & Deployment
- **Docker** & **Docker Compose**
- **Microservices Architecture**

## Project Structure
```text
backend/
  ├── common-models/       (Shared POJOs)
  ├── producer-service/    (Generates data)
  ├── ingestion-service/   (Kafka Publisher)
  ├── storage-node/        (Local DB writer & heartbeats)
  ├── replication-manager/ (Propagates data)
  ├── fault-detector/      (Health monitor)
  ├── analytics-engine/    (Insights engine)
  ├── api-gateway/         (REST API + WebSockets + Failover)
docker-compose.yml
```

## How to Run

1. Open a terminal in the root directory.
2. Build and run containers using Docker Compose.

```bash
docker-compose up -d --build
```

Wait around 1-2 minutes for Kafka, Zookeeper, and Redis to become fully healthy before the Spring Boot microservices start processing events.

## API Documentation & Examples

Base URL: `http://localhost:8080`

### 1. Dashboard Overview
**GET /api/dashboard/overview**
Returns metrics for dashboard summary cards.
```json
{
  "activeServers": 3,
  "failures": 0,
  "systemHealth": "100%",
  "alerts": "All Systems Normal",
  "trafficLoad": 10540
}
```

### 2. Live Traffic WebSocket
**WS ws://localhost:8080/live-updates**
Topic: `/topic/traffic`
Streams live data for React Map.
```json
{
  "eventId": "123e4567-e89b-12d3...",
  "vehicleId": "KA-01-4456",
  "state": "Karnataka",
  "city": "Bengaluru",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "speed": 45,
  "congestionLevel": 35,
  "timestamp": 1709230584
}
```

### 3. Server Status
**GET /api/servers/status**
```json
[
  {
    "serverName": "server-blr",
    "region": "SOUTH",
    "status": "ACTIVE",
    "lastHeartbeat": 1709230584
  }
]
```

### 4. Fetch Active Faults
**GET /api/faults**

### 5. State Data (with Failover)
**GET /api/traffic/state/{state}**
Fetches state data. If the regional server goes down, the Gateway automatically reroutes the request.

## Testing & Failure Simulation

### Simulate a Server Crash
Kill the Bengaluru Server (Primary):
```bash
docker stop server-blr
```

**Expected Behavior:**
1. The `fault-detector` will notice a missing heartbeat.
2. `server-blr` status will change to `FAILED`.
3. The Dashboard API (`GET /api/dashboard/overview`) will drop System Health and raise "Server Down! Tracking Failover" alert.
4. Calling `GET /api/traffic/state/Karnataka` will now silently reroute via `server-del`.

### Logs Example Showing Failover

Check the API Gateway logs to observe the dynamic failover kick in:
```bash
docker logs -f api-gateway
```

*Sample Log Output:*
```text
WARN 1 --- [nio-8080-exec-1] c.d.api.controller.GatewayController   : Failed to fetch state data from http://server-blr:8080 - failover initiating...
INFO 1 --- [nio-8080-exec-1] c.d.api.controller.GatewayController   : Successfully fetched data from fallback server: server-del
```

Check the Fault Detector logs to see the tracker declare a failure:
```bash
docker logs -f fault-detector
```
*Sample Log Output:*
```text
WARN 1 --- [   scheduling-1] c.d.f.service.HealthMonitorService     : Server server-blr has FAILED (heartbeat timeout). Failover should be triggered.
```

### Recover the Server
Start it back up:
```bash
docker start server-blr
```

*Sample Recover Output:*
```text
INFO 1 --- [   scheduling-1] c.d.f.service.HealthMonitorService     : Server server-blr has RECOVERED.
```
