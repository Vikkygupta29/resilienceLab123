import { useCallback, useEffect, useState } from "react";
import { getRetryMetrics } from "../services/metrics";

export function useRetryMetrics() {
    const [retryMetrics, setRetryMetrics] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadRetryMetrics = useCallback(async () => {
        try {
            const data = await getRetryMetrics();
            setRetryMetrics(data);
        } catch (error) {
            console.error("Failed to load retry metrics:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadRetryMetrics();

        const interval = setInterval(loadRetryMetrics, 5000);

        return () => clearInterval(interval);
    }, [loadRetryMetrics]);

    return {
        retryMetrics,
        loading,
        refresh: loadRetryMetrics,
    };
}