import { useCallback, useEffect, useState } from "react";
import { getP95Latency } from "../services/metrics";

export function useP95Latency() {
    const [p95Latency, setP95Latency] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadP95Latency = useCallback(async () => {
        try {
            const data = await getP95Latency();
            setP95Latency(data);
        } catch (error) {
            console.error("Failed to load P95 latency:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadP95Latency();

        const interval = setInterval(loadP95Latency, 5000);

        return () => clearInterval(interval);
    }, [loadP95Latency]);

    return {
        p95Latency,
        loading,
        refresh: loadP95Latency,
    };
}