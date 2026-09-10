import axios from "axios";

const prometheusClient = axios.create({
    baseURL: "/prometheus/api/v1",
    timeout: 5000,
});

export async function queryPrometheus(query) {
    const response = await prometheusClient.get("/query", {
        params: {
            query,
        },
    });

    return response.data;
}