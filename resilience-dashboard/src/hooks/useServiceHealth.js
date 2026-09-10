import { useCallback, useEffect, useState } from "react";
import { services } from "../config/services";

export function useServiceHealth() {
    const [health, setHealth] = useState({});
    const [loading, setLoading] = useState(true);
    const [lastUpdated, setLastUpdated] = useState(null);

    const checkHealth = useCallback(async () => {
        setLoading(true);

        const results = await Promise.all(
            services.map(async (service) => {
                try {
                    const response = await fetch(service.healthUrl);

                    if (!response.ok) {
                        return [service.id, { status: "DOWN" }];
                    }

                    const data = await response.json();

                    return [
                        service.id,
                        {
                            status: data.status === "UP" ? "UP" : "DOWN",
                        },
                    ];
                } catch {
                    return [service.id, { status: "DOWN" }];
                }
            })
        );

        setHealth(Object.fromEntries(results));
        setLastUpdated(new Date());
        setLoading(false);
    }, []);

    useEffect(() => {
        checkHealth();
    }, [checkHealth]);

    return {
        health,
        loading,
        lastUpdated,
        refresh: checkHealth,
    };
}