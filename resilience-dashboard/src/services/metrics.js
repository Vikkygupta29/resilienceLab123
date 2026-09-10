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