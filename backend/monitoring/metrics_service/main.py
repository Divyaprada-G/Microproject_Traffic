from fastapi import FastAPI
import random
import time

app = FastAPI(title="Smart City Traffic Performance Monitoring")

# Predefined static/mock nodes
nodes = ["server-blr", "server-del", "server-mum"]

@app.get("/metrics")
async def get_metrics():
    """
    Returns mock real-time performance metrics for the geo-distributed system.
    """
    return {
        "status": "healthy",
        "timestamp": time.time(),
        "performance": {
            "avg_latency_ms": random.uniform(15.0, 45.0),
            "messages_per_second": random.randint(120, 450),
            "replication_events": random.randint(50, 200),
            "failover_count": random.randint(0, 2),
            "active_storage_nodes": random.randint(2, 3),
            "cpu_utilization": random.uniform(5.5, 12.0),
            "memory_utilization": random.uniform(25.0, 45.0)
        },
        "node_status": [
            {"id": node, "status": "UP", "latency": random.uniform(5.0, 15.0)}
            for node in nodes
        ]
    }

@app.get("/health")
async def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8086)
