import { useCallback, useEffect, useState } from "react";
import { queryPrometheus } from "../services/prometheus";

export function usePrometheusMetrics() {
    const [metrics, setMetrics] = useState({
        ordersConfirmed: 0,
        faultsInjected: 0,
        errorRate: 0,
    });

    const [loading, setLoading] = useState(true);

    const loadMetrics = useCallback(async () => {
        try {
            setLoading(true);

            const [
                ordersResult,
                faultsResult,
                errorsResult,
                totalRequestsResult,
            ] = await Promise.all([
                queryPrometheus("orders_confirmed_total"),

                queryPrometheus("faults_injected_total"),

                queryPrometheus(
                    'sum(rate(http_server_requests_seconds_count{status=~"5..|429"}[5m]))'
                ),

                queryPrometheus(
                    "sum(rate(http_server_requests_seconds_count[5m]))"
                ),
            ]);

            const ordersConfirmed =
                Number(ordersResult.data.result?.[0]?.value?.[1]) || 0;

            const faultsInjected =
                Number(faultsResult.data.result?.[0]?.value?.[1]) || 0;

            const failedRequests =
                Number(errorsResult.data.result?.[0]?.value?.[1]) || 0;

            const totalRequests =
                Number(totalRequestsResult.data.result?.[0]?.value?.[1]) || 0;

            const errorRate =
                totalRequests > 0
                    ? (failedRequests / totalRequests) * 100
                    : 0;

            setMetrics({
                ordersConfirmed,
                faultsInjected,
                errorRate,
            });
        } catch (error) {
            console.error("Failed to load Prometheus metrics:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadMetrics();

        const interval = setInterval(loadMetrics, 5000);

        return () => clearInterval(interval);
    }, [loadMetrics]);

    return {
        metrics,
        loading,
        refresh: loadMetrics,
    };
}