import { useCallback, useEffect, useState } from "react";
import { getErrorRate } from "../services/metrics";

export function useErrorRate() {
    const [errorRate, setErrorRate] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadErrorRate = useCallback(async () => {
        try {
            const data = await getErrorRate();
            setErrorRate(data);
        } catch (error) {
            console.error("Failed to load error rate:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadErrorRate();

        const interval = setInterval(loadErrorRate, 5000);

        return () => clearInterval(interval);
    }, [loadErrorRate]);

    return {
        errorRate,
        loading,
        refresh: loadErrorRate,
    };
}