import { queryPrometheus } from "./prometheus";

export async function getRequestRate() {
    const result = await queryPrometheus(
        "sum by (job) (rate(http_server_requests_seconds_count[1m]))"
    );

    return result.data.result.map((item) => ({
        service: item.metric.job || "unknown",
        value: Number(item.value[1]),
    }));
}

export async function getP95Latency() {
    const result = await queryPrometheus(`
    histogram_quantile(
      0.95,
      sum by (job, le) (
        rate(http_server_requests_seconds_bucket{uri="/api/orders",method="POST"}[5m])
      )
    )
  `);

    return result.data.result.map((item) => ({
        service: item.metric.job || "unknown",
        value: Number(item.value[1]),
    }));
}

export async function getErrorRate() {
    const result = await queryPrometheus(
        'sum by (job, status) (rate(http_server_requests_seconds_count{status=~"5..|429"}[5m]))'
    );

    return result.data.result.map((item) => ({
        service: item.metric.job || "unknown",
        status: item.metric.status || "unknown",
        value: Number(item.value[1]),
    }));
}

export async function getCircuitBreakerState() {
    const result = await queryPrometheus(
        "resilience4j_circuitbreaker_state"
    );

    const states = {};

    result.data.result.forEach((item) => {
        const name = item.metric.name || "unknown";
        const state = item.metric.state || "unknown";
        const value = Number(item.value[1]);

        if (!states[name] || value > states[name].value) {
            states[name] = {
                name,
                state,
                value,
            };
        }
    });

    return Object.values(states);
}


export async function getKafkaConsumerLag() {
    const result = await queryPrometheus(
        "max by (client_id) (kafka_consumer_fetch_manager_records_lag)"
    );

    return result.data.result.map((item) => ({
        clientId: item.metric.client_id || "unknown",
        value: Number(item.value[1]),
    }));
}