import { useCallback, useEffect, useState } from "react";
import { getKafkaConsumerLag } from "../services/metrics";

export function useKafkaConsumerLag() {
    const [consumerLag, setConsumerLag] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadConsumerLag = useCallback(async () => {
        try {
            const data = await getKafkaConsumerLag();
            setConsumerLag(data);
        } catch (error) {
            console.error("Failed to load Kafka consumer lag:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadConsumerLag();

        const interval = setInterval(loadConsumerLag, 5000);

        return () => clearInterval(interval);
    }, [loadConsumerLag]);

    return {
        consumerLag,
        loading,
        refresh: loadConsumerLag,
    };
}