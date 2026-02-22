FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
# To avoid OpenMP related crashes with LightFM
ENV OMP_NUM_THREADS=1

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    python3-dev \
    libopenblas-dev \
    libomp-dev \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip && pip install fastapi uvicorn[standard] sqlmodel lightfm numpy dotenv psycopg2-binary pandas


COPY . .

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
