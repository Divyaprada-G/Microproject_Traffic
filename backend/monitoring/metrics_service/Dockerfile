FROM python:3.11-slim
WORKDIR /app
COPY . .
RUN pip install fastapi uvicorn
EXPOSE 8086
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8086"]
