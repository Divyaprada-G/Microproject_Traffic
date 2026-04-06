FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install fastapi uvicorn httpx
EXPOSE 8085
# CMD starts the uvicorn worker
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8085"]
