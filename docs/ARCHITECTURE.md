# Distributed Architecture for Smart City Traffic Management

## Overview
This system implements a geo-distributed architecture designed for high availability and low-latency processing of traffic telemetry. It leverages microservices, a distributed message broker (Kafka), and regional storage nodes to provide a robust monitoring solution.

## Core Components
### 1. Ingestion Layer
Data entry point that validates and accepts telemetry batches from thousands of simulated vehicles.
### 2. Geo-Distributed Storage
Data is partitioned across regional storage nodes (North, South, West) to ensure data is processed and stored near its geographical source.
### 3. Asymmetric Replication
Replicates traffic events across nodes in different regions asynchronously to survive regional datacenter outages.

## Edge Preprocessing (Optional Phase)
The system supports an experimental Edge Preprocessing layer. This service sits at the network edge, performing deduplication and regional aggregation before data reaches the core ingestion pipeline. This reduces backbone network bandwidth usage and improves overall system responsiveness in high-density traffic scenarios.

## Scalability
The architecture is horizontally scalable. New regional nodes and ingestion instances can be added dynamically based on traffic volume per city.
