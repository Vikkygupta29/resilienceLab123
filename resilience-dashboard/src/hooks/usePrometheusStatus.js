import { useEffect, useState } from "react";

export default function usePrometheusStatus() {
    const [connected, setConnected] = useState(false);

    const checkPrometheus = async () => {
        try {
            const response = await fetch("/prometheus/api/v1/query?query=up");

            if (!response.ok) {
                throw new Error("Prometheus unavailable");
            }

            const data = await response.json();

            setConnected(data.status === "success");
        } catch (error) {
            setConnected(false);
        }
    };

    useEffect(() => {
        checkPrometheus();

        const interval = setInterval(checkPrometheus, 5000);

        return () => clearInterval(interval);
    }, []);

    return connected;
}