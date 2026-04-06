# Optional Edge Preprocessing Extension

## Purpose
The `edge-node-service` provides an optional preprocessing layer to evaluate the performance impact of edge computing in smart cities. Its primary goal is to reduce network traffic by filtering telemetry at its source.

## Functionality
### 1. In-Memory Deduplication
Removes duplicate vehicle event reports based on a unique `eventId` and `vehicleId` combination.
### 2. Regional Aggregation Simulation
Groups events from the same region for a small time window to simulate batching efficiency.
### 3. Latency Mitigation
By filtering data before it hits the ingestion service, the edge node significantly reduces the computational load on the core ingestion cluster.

## Deployment Strategy
Configuration is toggled via the `EDGE_NODE_ENABLED` flag.
- **ENABLED: true** → Simulated vehicles send data directly to the edge node.
- **ENABLED: false** (Default) → Data is sent to the existing ingestion endpoint, ensuring zero impact on primary system testing.
