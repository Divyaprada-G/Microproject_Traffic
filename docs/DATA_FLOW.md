# Data Flow in a Distributed Traffic System

## 1. Traffic Ingestion Pipeline
1.  **Traffic Simulator** (Producer) generates JSON telemetry for 100+ vehicles.
2.  **Ingestion Service** receives batches of events via HTTP POST.
3.  **Kafka Message Broker** decouples ingestion from storage, providing a 5-second buffer.
4.  **Regional Storage Nodes** (Server-BLR, Server-DEL, Server-MUM) consume from regional Kafka topics (`traffic-south`, `traffic-north`, `traffic-west`).

## 2. Global Replication Flow
1.  After storing a message locally, a **Storage Node** sends a replication task to the **Replication-Manager**.
2.  The **Replication-Manager** identifies the healthy nodes in other regions.
3.  The task is dispatched to the target region's replication topic.
4.  The remote node consumes the replication task and updates its local data store with a `replicated` flag.

## 3. Real-Time Dashboard Flow
1.  **API-Gateway** provides a unified WebSocket/REST API for the frontend.
2.  **Analytics-Engine** runs periodic Spark-like aggregations (simulated) on the traffic stream.
3.  **Frontend (Vite + React)** pulls data from the Gateway every 5 seconds.
4.  **Leaflet Maps** visualize the moving vehicle markers and congestion heatmaps.

## 4. Edge Preprocessing (Pre-Ingestion)
*(Optional Optional Module)*
1.  Incoming data hits the **Edge Node** before the ingestion service.
2.  The Edge Node filters duplicates and sends a compressed batch to the main ingestion pipeline to save bandwidth.
