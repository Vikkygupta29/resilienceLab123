import { useCallback, useEffect, useState } from "react";
import { getRequestRate } from "../services/metrics";

export function useRequestRate() {
    const [requestRate, setRequestRate] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadRequestRate = useCallback(async () => {
        try {
            const data = await getRequestRate();
            setRequestRate(data);
        } catch (error) {
            console.error("Failed to load request rate:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadRequestRate();

        const interval = setInterval(loadRequestRate, 5000);

        return () => clearInterval(interval);
    }, [loadRequestRate]);

    return {
        requestRate,
        loading,
        refresh: loadRequestRate,
    };
}