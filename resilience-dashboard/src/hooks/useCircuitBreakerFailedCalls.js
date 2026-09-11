import { useCallback, useEffect, useState } from "react";
import { getCircuitBreakerFailedCalls } from "../services/metrics";

export function useCircuitBreakerFailedCalls() {
    const [failedCalls, setFailedCalls] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadFailedCalls = useCallback(async () => {
        try {
            const data = await getCircuitBreakerFailedCalls();
            setFailedCalls(data);
        } catch (error) {
            console.error("Failed to load circuit breaker failed calls:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadFailedCalls();

        const interval = setInterval(loadFailedCalls, 5000);

        return () => clearInterval(interval);
    }, [loadFailedCalls]);

    return {
        failedCalls,
        loading,
        refresh: loadFailedCalls,
    };
}