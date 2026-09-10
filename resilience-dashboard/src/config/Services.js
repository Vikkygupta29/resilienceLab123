export const services = [
    {
        id: "gateway",
        name: "API Gateway",
        port: 8080,
        healthUrl: "/service/gateway/actuator/health",
    },
    {
        id: "order",
        name: "Order Service",
        port: 8081,
        healthUrl: "/service/order/actuator/health",
    },
    {
        id: "inventory",
        name: "Inventory Service",
        port: 8083,
        healthUrl: "/service/inventory/actuator/health",
    },
    {
        id: "payment",
        name: "Payment Service",
        port: 8082,
        healthUrl: "/service/payment/actuator/health",
    },
];