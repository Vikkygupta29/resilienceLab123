import { useCallback, useEffect, useState } from "react";
import { getCircuitBreakerState } from "../services/metrics";

export function useCircuitBreaker() {
    const [circuitBreakers, setCircuitBreakers] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadCircuitBreakers = useCallback(async () => {
        try {
            const data = await getCircuitBreakerState();
            setCircuitBreakers(data);
        } catch (error) {
            console.error("Failed to load circuit breaker state:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadCircuitBreakers();

        const interval = setInterval(loadCircuitBreakers, 5000);

        return () => clearInterval(interval);
    }, [loadCircuitBreakers]);

    return {
        circuitBreakers,
        loading,
        refresh: loadCircuitBreakers,
    };
}