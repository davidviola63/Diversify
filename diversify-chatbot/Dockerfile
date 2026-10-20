FROM python:3.11-slim
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
COPY requirements.txt .
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install -r requirements.txt
COPY src ./src
EXPOSE 5000
WORKDIR /app/src/utils
CMD ["python", "chatbot_interface_api.py"]
