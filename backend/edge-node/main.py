from fastapi import FastAPI, Request
import httpx
import os
import time

app = FastAPI(title="Distributed Edge Processing Node")

# Configuration
EDGE_NODE_ENABLED = os.getenv("EDGE_NODE_ENABLED", "true").lower() == "true"
TARGET_INGESTION_URL = os.getenv("INGESTION_URL", "http://ingestion:8081/api/ingest/batch")

# Simple duplicate filter (LRU simulation)
processed_ids = set()

@app.post("/api/ingest/batch")
async def process_edge_data(data: list):
    """
    Middleware that performs edge-side preprocessing before forwarding to main ingestion.
    """
    if not EDGE_NODE_ENABLED:
        # Transparent proxy
        async with httpx.AsyncClient() as client:
            resp = await client.post(TARGET_INGESTION_URL, json=data)
            return resp.json()

    start_time = time.time()
    
    # 1. Duplicate Filtering & Basic Validation
    unique_data = []
    for event in data:
        eid = event.get("eventId")
        if eid not in processed_ids:
            unique_data.append(event)
            processed_ids.add(eid)
            # Keep set size manageable
            if len(processed_ids) > 10000:
                processed_ids.clear()

    # 2. Regional Aggregation Simulation
    # Logic: Only forward data if it meets a batch threshold or just forward immediately for low latency
    # In a real scenario, we would aggregate multiple small sensors here.

    # 3. Forward to Main Ingestion Service
    async with httpx.AsyncClient() as client:
        try:
            resp = await client.post(TARGET_INGESTION_URL, json=unique_data)
            forward_latency = (time.time() - start_time) * 1000
            
            return {
                "status": "processed_at_edge",
                "original_count": len(data),
                "processed_count": len(unique_data),
                "edge_latency_ms": forward_latency,
                "ingestion_response": resp.status_code
            }
        except Exception as e:
            return {"error": f"Failed to forward to ingestion: {str(e)}"}

@app.get("/health")
async def health():
    return {"status": "edge_node_up", "enabled": EDGE_NODE_ENABLED}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8085)
