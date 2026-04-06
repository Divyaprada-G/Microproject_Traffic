# Fault Tolerance & Reliability Strategy

## Failure Detection Mechanisms
The system includes a dedicated `Fault-Detector` service that continuously monitors all regional storage nodes. It handles heartbeat timeouts and maintains real-time consensus on the UP/DOWN status of each regional node.

## Automated Failover Workflow
When a regional node is detected as unresponsive:
1.  **Redirection**: Data for the region is temporarily redirected to the next nearest healthy neighbor.
2.  **Replica Promotion**: The system begins serving regional queries from historical replicas stored in other regions.
3.  **Automatic Recovery**: When the node regains consensus, it is automatically reintegrated into the active node pool and synchronizes missing data.

## Resilience to Data Bursts
Highly elastic Kafka message bus handles ingestion spikes, preventing data loss during peak traffic hours by providing a 5-second buffer.
Persistence is maintained via PostgreSQL with Docker volume mapping to survive container restarts.
