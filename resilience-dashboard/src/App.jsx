import {
  Activity,
  AlertTriangle,
  Bell,
  CheckCircle2,
  ChevronDown,
  CircleDot,
  Gauge,
  LayoutDashboard,
  Radio,
  RefreshCw,
  Server,
  Settings,
  ShieldCheck,
  Zap,
} from "lucide-react";

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { useState ,useEffect } from "react";

import { useServiceHealth } from "./hooks/useServiceHealth";
import { services } from "./config/services";
import { usePrometheusMetrics } from "./hooks/usePrometheusMetrics";
import { useRequestRate } from "./hooks/useRequestRate";
import { useP95Latency } from "./hooks/useP95Latency";
import { useErrorRate } from "./hooks/useErrorRate";
import { useCircuitBreaker } from "./hooks/useCircuitBreaker";
import { useKafkaConsumerLag } from "./hooks/useKafkaConsumerLag";
import usePrometheusStatus from "./hooks/usePrometheusStatus";
import { useRetryMetrics } from "./hooks/useRetryMetrics";
import { useCircuitBreakerFailedCalls } from "./hooks/useCircuitBreakerFailedCalls";


const stats = [
  {
    label: "Orders Confirmed",
    key: "ordersConfirmed",
    detail: "+ Real-time Prometheus metric",
    icon: CheckCircle2,
  },
  {
    label: "Faults Injected",
    key: "faultsInjected",
    detail: "Inventory service",
    icon: Zap,
  },
  {
    label: "Error Rate",
    key: "errorRate",
    detail: "Last 5 minutes",
    icon: AlertTriangle,
  },
];


function FaultCard({ service, servicePath }) {
  const [mode, setMode] = useState("NORMAL");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");



  useEffect(() => {
  const loadFaultMode = async () => {
    try {
      const response = await fetch(
        `/service/order/admin/faults/${servicePath}?t=${Date.now()}`,
        {
          cache: "no-store",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to load fault mode");
      }

      const currentMode = await response.text();
      setMode(currentMode);
    } catch (error) {
      console.error(`Failed to load ${service} fault mode:`, error);
    }
  };

  loadFaultMode();
}, [servicePath, service]);

  const faultModes = [
    "NORMAL",
    "LATENCY",
    "FAIL",
    "TIMEOUT",
    "RATE_LIMITED",
  ];

  const injectFault = async () => {
    setLoading(true);
    setMessage("");

    try {
      const response = await fetch(
        `/service/order/admin/faults/${servicePath}?mode=${mode}`,
        {
          method: "POST",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to inject fault");
      }

      setMessage(`${mode} fault injected successfully`);
      setTimeout(() => {
        setMessage("");
      }, 2000);

    } catch (error) {
      setMessage("Failed to inject fault");
    } finally {
      setLoading(false);
    }
  };

  const clearFault = async () => {
    setLoading(true);

    try {
      const response = await fetch(
        `/service/order/admin/faults/${servicePath}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to clear fault");
      }

      setMode("NORMAL");
      setMessage("Fault cleared successfully");
      setTimeout(() => {
        setMessage("");
      }, 2000);
    } catch (error) {
      setMessage("Failed to clear fault");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="rounded-xl border border-white/10 bg-[#111827] p-5">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-white">
            {service}
          </p>

          <p className="mt-1 text-xs text-slate-500">
            Controlled failure simulation
          </p>
        </div>

        <Zap className="h-5 w-5 text-indigo-400" />
      </div>

      <div className="mt-5">
        <label className="text-xs font-medium text-slate-500">
          Fault Mode
        </label>

        <select
          value={mode}
          onChange={(event) => setMode(event.target.value)}
          className="mt-2 w-full rounded-xl border border-white/10 bg-[#0f141c] px-3 py-2.5 text-sm text-slate-300 outline-none focus:border-indigo-400/40"
        >
          {faultModes.map((faultMode) => (
            <option key={faultMode} value={faultMode}>
              {faultMode}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-4 flex gap-3">
        <button
          onClick={injectFault}
          disabled={loading}
          className="flex-1 rounded-xl bg-indigo-500/10 px-4 py-2.5 text-xs font-medium text-indigo-300 ring-1 ring-indigo-400/20 transition hover:bg-indigo-500/20 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {loading ? "Processing..." : "Inject Fault"}
        </button>

        <button
          onClick={clearFault}
          disabled={loading}
          className="rounded-xl border border-white/10 bg-white/[0.03] px-4 py-2.5 text-xs font-medium text-slate-400 transition hover:bg-white/[0.07] hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
        >
          Clear
        </button>
      </div>

      {message && (
        <p className="mt-3 text-xs text-slate-500">
          {message}
        </p>
      )}
    </div>
  );
}


function App() {
  const {
    health,
    loading,
    lastUpdated,
    refresh,
  } = useServiceHealth();

  const [activePage, setActivePage] = useState("overview");

  const { metrics } = usePrometheusMetrics();
  const { requestRate } = useRequestRate();
  const { p95Latency } = useP95Latency();
  const { errorRate } = useErrorRate();
  const { circuitBreakers } = useCircuitBreaker();
  const { consumerLag } = useKafkaConsumerLag();
  const prometheusConnected = usePrometheusStatus();
  const { retryMetrics } = useRetryMetrics();
  const { failedCalls } = useCircuitBreakerFailedCalls();



  const healthyCount = services.filter(
    (service) => health[service.id]?.status === "UP"
  ).length;

  const downCount = services.length - healthyCount;

  return (
    <div className="h-screen overflow-hidden bg-[#0a0d12] text-slate-200">
      <div className="flex h-screen overflow-hidden">

        {/* Sidebar */}
        <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 shrink-0 border-r border-white/10 bg-[#0d1117] lg:flex lg:flex-col">

          <div className="flex h-20 shrink-0 items-center gap-3 border-b border-white/10 px-6">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-500/15 ring-1 ring-indigo-400/20">
              <ShieldCheck className="h-5 w-5 text-indigo-400" />
            </div>

            <div>
              <h1 className="text-sm font-semibold tracking-wide text-white">
                ResilienceLab
              </h1>

              <p className="text-xs text-slate-500">
                Operations Console
              </p>
            </div>
          </div>

          <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-6">
            <p className="px-3 pb-3 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-600">
              Monitor
            </p>


            <button
              onClick={() => setActivePage("overview")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "overview"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <LayoutDashboard className="h-4 w-4" />
              Overview
            </button>



            <button
              onClick={() => setActivePage("metrics")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "metrics"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <Activity className="h-4 w-4" />
              Metrics
            </button>

            <button
              onClick={() => setActivePage("kafka")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "kafka"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <Radio className="h-4 w-4" />
              Kafka
            </button>


            <button
              onClick={() => setActivePage("resilience")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "resilience"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <Gauge className="h-4 w-4" />
              Resilience
            </button>

            <p className="px-3 pb-3 pt-7 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-600">
              Operations
            </p>


            <button
              onClick={() => setActivePage("fault-injection")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "fault-injection"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <Zap className="h-4 w-4" />
              Fault Injection
            </button>


            <button
              onClick={() => setActivePage("settings")}
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${activePage === "settings"
                ? "bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-400/10"
                : "text-slate-400 transition hover:bg-white/5 hover:text-white"
                }`}
            >
              <Settings className="h-4 w-4" />
              Settings
            </button>


          </nav>

          <div className="shrink-0 border-t border-white/10 p-4">
            <div className="rounded-xl bg-white/[0.03] p-3">
              <div className="flex items-center gap-2">
                <span
                  className={`h-2 w-2 rounded-full ${downCount === 0
                    ? "bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.7)]"
                    : "bg-red-400 shadow-[0_0_8px_rgba(248,113,113,0.7)]"
                    }`}
                />

                <span className="text-xs font-medium text-slate-300">
                  {downCount === 0
                    ? "Platform operational"
                    : `${downCount} service${downCount > 1 ? "s" : ""
                    } unavailable`}
                </span>
              </div>

              <p className="mt-1 pl-4 text-[11px] text-slate-600">
                {lastUpdated
                  ? `Checked ${lastUpdated.toLocaleTimeString()}`
                  : "Checking services..."}
              </p>
            </div>
          </div>
        </aside>

        {/* Main */}
        <main className="ml-0 min-w-0 flex-1 lg:ml-64">

          {/* Header */}
          <header className="sticky top-0 z-20 flex h-20 shrink-0 items-center justify-between border-b border-white/10 bg-[#0a0d12]/90 px-6 backdrop-blur-xl lg:px-8">


            <div>
              <div className="flex items-center gap-2 text-xs text-slate-500">
                <span>Operations</span>
                <span>/</span>
                <span className="capitalize text-slate-300">
                  {activePage.replace("-", " ")}
                </span>
              </div>

              <h2 className="mt-1 text-xl font-semibold capitalize text-white">
                {activePage.replace("-", " ")}
              </h2>
            </div>

            <div className="flex items-center gap-3">
              <button className="relative rounded-xl border border-white/10 bg-white/[0.03] p-2.5 text-slate-400 transition hover:bg-white/[0.07] hover:text-white">
                <Bell className="h-4 w-4" />

                <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-amber-400" />
              </button>

              <button className="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-2 text-sm text-slate-300">
                <div className="flex h-6 w-6 items-center justify-center rounded-full bg-indigo-500/20 text-[10px] font-semibold text-indigo-300">
                  OP
                </div>

                Operator

                <ChevronDown className="h-3.5 w-3.5 text-slate-500" />
              </button>
            </div>
          </header>

          {/* Scrollable Dashboard Content */}
          <div className="h-[calc(100vh-5rem)] overflow-y-auto p-6 lg:p-8">

            {activePage === "overview" && (
              <>

                {/* Page intro */}
                <div className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-end">
                  <div>
                    <p className="text-sm text-slate-500">
                      Real-time reliability and failure monitoring
                    </p>
                  </div>

                  <div className="flex items-center gap-3">
                    <span
                      className={`flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs ${downCount === 0
                        ? "border-emerald-400/10 bg-emerald-400/5 text-emerald-300"
                        : "border-red-400/10 bg-red-400/5 text-red-300"
                        }`}
                    >
                      <CircleDot className="h-3 w-3" />

                      {downCount === 0
                        ? "All systems operational"
                        : `${downCount} service${downCount > 1 ? "s" : ""
                        } down`}
                    </span>

                    <button
                      onClick={refresh}
                      disabled={loading}
                      className="flex items-center gap-2 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-2 text-xs text-slate-400 transition hover:bg-white/[0.07] hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      <RefreshCw
                        className={`h-3.5 w-3.5 ${loading ? "animate-spin" : ""
                          }`}
                      />

                      Refresh
                    </button>
                  </div>
                </div>

                {/* Stats */}
                <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">

                  {/* Services Online */}
                  <div className="group rounded-2xl border border-white/10 bg-[#0f141c] p-5 transition hover:border-white/15 hover:bg-[#111720]">
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="text-xs font-medium text-slate-500">
                          Services Online
                        </p>

                        <p className="mt-3 text-2xl font-semibold tracking-tight text-white">
                          {healthyCount} / {services.length}
                        </p>
                      </div>

                      <div className="rounded-xl bg-indigo-500/10 p-2.5 text-indigo-400">
                        <Server className="h-5 w-5" />
                      </div>
                    </div>

                    <p className="mt-4 text-xs text-slate-600">
                      {downCount === 0
                        ? "All systems operational"
                        : `${downCount} service${downCount > 1 ? "s" : ""
                        } down`}
                    </p>
                  </div>

                  {/* Prometheus Stats */}
                  {stats.map((stat) => {
                    const Icon = stat.icon;

                    return (
                      <div
                        key={stat.label}
                        className="group rounded-2xl border border-white/10 bg-[#0f141c] p-5 transition hover:border-white/15 hover:bg-[#111720]"
                      >
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="text-xs font-medium text-slate-500">
                              {stat.label}
                            </p>

                            <p className="mt-3 text-2xl font-semibold tracking-tight text-white">
                              {stat.key === "errorRate"
                                ? `${metrics.errorRate.toFixed(2)}%`
                                : metrics[stat.key]}
                            </p>
                          </div>

                          <div className="rounded-xl bg-indigo-500/10 p-2.5 text-indigo-400">
                            <Icon className="h-5 w-5" />
                          </div>
                        </div>

                        <p className="mt-4 text-xs text-slate-600">
                          {stat.detail}
                        </p>
                      </div>
                    );
                  })}
                </section>

                {/* Service Health */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="flex items-center justify-between border-b border-white/10 px-5 py-4">
                    <div>
                      <h3 className="text-sm font-semibold text-white">
                        Service Health
                      </h3>

                      <p className="mt-1 text-xs text-slate-600">
                        Current status of ResilienceLab services
                      </p>
                    </div>

                    <span
                      className={`flex items-center gap-2 text-xs ${downCount === 0
                        ? "text-emerald-400"
                        : "text-red-400"
                        }`}
                    >
                      <span
                        className={`h-1.5 w-1.5 rounded-full ${downCount === 0
                          ? "bg-emerald-400"
                          : "bg-red-400"
                          }`}
                      />

                      {healthyCount} healthy
                    </span>
                  </div>

                  <div className="divide-y divide-white/5">
                    {services.map((service) => {
                      const status = health[service.id]?.status;

                      const isHealthy = status === "UP";

                      const isChecking = loading && !status;

                      return (
                        <div
                          key={service.id}
                          className="flex items-center justify-between px-5 py-4 transition hover:bg-white/[0.02]"
                        >
                          <div className="flex items-center gap-3">
                            <div
                              className={`flex h-9 w-9 items-center justify-center rounded-lg ${isHealthy
                                ? "bg-emerald-400/5"
                                : isChecking
                                  ? "bg-amber-400/5"
                                  : "bg-red-400/5"
                                }`}
                            >
                              <Server
                                className={`h-4 w-4 ${isHealthy
                                  ? "text-emerald-400"
                                  : isChecking
                                    ? "text-amber-400"
                                    : "text-red-400"
                                  }`}
                              />
                            </div>

                            <div>
                              <p className="text-sm font-medium text-slate-200">
                                {service.name}
                              </p>

                              <p className="text-xs text-slate-600">
                                localhost:{service.port}
                              </p>
                            </div>
                          </div>

                          <div className="flex items-center gap-8">
                            <div className="hidden text-right sm:block">
                              <p className="text-xs text-slate-600">
                                Health endpoint
                              </p>

                              <p className="mt-1 text-xs text-slate-500">
                                Actuator
                              </p>
                            </div>

                            <div
                              className={`flex min-w-[75px] items-center justify-center gap-2 rounded-full px-2.5 py-1.5 text-xs font-medium ${isHealthy
                                ? "bg-emerald-400/5 text-emerald-400"
                                : isChecking
                                  ? "bg-amber-400/5 text-amber-400"
                                  : "bg-red-400/5 text-red-400"
                                }`}
                            >
                              <span
                                className={`h-1.5 w-1.5 rounded-full ${isHealthy
                                  ? "bg-emerald-400"
                                  : isChecking
                                    ? "bg-amber-400"
                                    : "bg-red-400"
                                  }`}
                              />

                              {isChecking
                                ? "CHECKING"
                                : isHealthy
                                  ? "UP"
                                  : "DOWN"}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  <div className="border-t border-white/5 px-5 py-3">
                    <p className="text-[11px] text-slate-600">
                      {lastUpdated
                        ? `Last checked ${lastUpdated.toLocaleTimeString()}`
                        : "Checking services..."}
                    </p>
                  </div>
                </section>

              </>
            )}


            {activePage === "metrics" && (
              <>

                {/* Request Rate */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      Request Rate
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Requests per second across services
                    </p>
                  </div>

                  <div className="h-72 p-5">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={requestRate}>
                        <CartesianGrid
                          strokeDasharray="3 3"
                          stroke="#1e293b"
                        />

                        <XAxis
                          dataKey="service"
                          stroke="#64748b"
                          tick={{ fontSize: 11 }}
                        />

                        <YAxis
                          stroke="#64748b"
                          tick={{ fontSize: 11 }}
                        />

                        <Tooltip />

                        <Line
                          type="monotone"
                          dataKey="value"
                          stroke="#818cf8"
                          strokeWidth={2}
                          dot={false}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </section>

                {/* P95 Request Latency */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      P95 Request Latency
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      95th percentile latency for order requests
                    </p>
                  </div>

                  <div className="h-72 p-5">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={p95Latency}>
                        <CartesianGrid
                          strokeDasharray="3 3"
                          stroke="#1e293b"
                        />

                        <XAxis
                          dataKey="service"
                          stroke="#64748b"
                          tick={{ fontSize: 11 }}
                        />

                        <YAxis
                          stroke="#64748b"
                          tick={{ fontSize: 11 }}
                          tickFormatter={(value) => `${value.toFixed(2)}s`}
                        />

                        <Tooltip
                          formatter={(value) => [
                            `${Number(value).toFixed(3)}s`,
                            "P95",
                          ]}
                        />

                        <Line
                          type="monotone"
                          dataKey="value"
                          stroke="#818cf8"
                          strokeWidth={2}
                          dot={false}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </section>

                {/* 5xx / 429 Error Rate */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      5xx / 429 Error Rate
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Server and rate-limit errors across services
                    </p>
                  </div>

                  <div className="h-72 p-5">
                    {errorRate.length === 0 ? (
                      <div className="flex h-full items-center justify-center">
                        <div className="text-center">
                          <CheckCircle2 className="mx-auto h-8 w-8 text-emerald-400" />

                          <p className="mt-3 text-sm font-medium text-slate-300">
                            No 5xx / 429 errors detected
                          </p>

                          <p className="mt-1 text-xs text-slate-600">
                            No server or rate-limit errors in the last 5 minutes
                          </p>
                        </div>
                      </div>
                    ) : (
                      <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={errorRate}>
                          <CartesianGrid
                            strokeDasharray="3 3"
                            stroke="#1e293b"
                          />

                          <XAxis
                            dataKey="service"
                            stroke="#64748b"
                            tick={{ fontSize: 11 }}
                          />

                          <YAxis
                            stroke="#64748b"
                            tick={{ fontSize: 11 }}
                          />

                          <Tooltip
                            formatter={(value, name, props) => [
                              `${Number(value).toFixed(4)} req/s`,
                              props.payload.status,
                            ]}
                          />

                          <Line
                            type="monotone"
                            dataKey="value"
                            stroke="#818cf8"
                            strokeWidth={2}
                            dot={false}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    )}
                  </div>
                </section>

              </>
            )}

            {activePage === "resilience" && (
              <>

                {/* Circuit Breaker State */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      Circuit Breaker State
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Current Resilience4j circuit breaker state
                    </p>
                  </div>

                  <div className="p-5">
                    {circuitBreakers.length === 0 ? (
                      <div className="flex h-40 items-center justify-center">
                        <p className="text-sm text-slate-500">
                          No circuit breaker data available
                        </p>
                      </div>
                    ) : (
                      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        {circuitBreakers.map((circuit, index) => (
                          <div
                            key={`${circuit.name}-${circuit.state}-${index}`}
                            className="rounded-xl border border-white/10 bg-[#111827] p-4"
                          >
                            <div className="flex items-center justify-between">
                              <div>
                                <p className="text-sm font-medium text-white">
                                  {circuit.name}
                                </p>

                                <p className="mt-1 text-xs text-slate-500">
                                  Resilience4j Circuit Breaker
                                </p>
                              </div>

                              <span className="rounded-full border border-emerald-400/20 bg-emerald-400/10 px-3 py-1 text-xs font-semibold text-emerald-400">
                                {circuit.state.toUpperCase()}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>



                </section>

                <div className="rounded-2xl border  border-white/10 bg-[#0f141c] p-5">
                  <div className="mb-4">
                    <h3 className="text-sm font-semibold text-white">
                      Retry Metrics
                    </h3>
                    <p className="mt-1 text-xs text-slate-500">
                      Resilience4j retry call statistics
                    </p>
                  </div>

                  {retryMetrics.length === 0 ? (
                    <p className="text-sm text-slate-500">
                      No retry metrics available
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {retryMetrics.map((metric, index) => (
                        <div
                          key={`${metric.name}-${metric.kind}-${index}`}
                          className="flex items-center justify-between rounded-xl border border-white/5 bg-black/10 px-4 py-3"
                        >
                          <div>
                            <p className="text-sm font-medium text-slate-200">
                              {metric.name}
                            </p>
                            <p className="text-xs text-slate-500">
                              {metric.kind}
                            </p>
                          </div>

                          <span className="text-lg font-semibold text-white">
                            {metric.value}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="rounded-xl border border-white/10 bg-[#0f141c] p-5">
                  <div className="mb-4">
                    <h3 className="text-sm font-semibold text-white">
                      Circuit Breaker Failed Calls
                    </h3>
                    <p className="mt-1 text-xs text-slate-500">
                      Failed calls recorded by Resilience4j
                    </p>
                  </div>

                  {failedCalls.length === 0 ? (
                    <p className="text-sm text-slate-500">
                      No failed calls recorded
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {failedCalls.map((metric, index) => (
                        <div
                          key={`${metric.name}-${metric.kind}-${index}`}
                          className="flex items-center justify-between rounded-xl border border-white/5 bg-black/10 px-4 py-3"
                        >
                          <div>
                            <p className="text-sm font-medium text-slate-200">
                              {metric.name}
                            </p>
                            <p className="text-xs text-slate-500">
                              {metric.kind}
                            </p>
                          </div>

                          <span className="text-lg font-semibold text-white">
                            {metric.value}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>



              </>
            )}

            {/* Fault Injection */}
            {activePage === "fault-injection" && (
              <>
                <div className="mb-8">
                  <p className="text-sm text-slate-500">
                    Inject controlled failures to test service resilience
                  </p>
                </div>

                <section className="rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      Fault Injection
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Simulate controlled failures in ResilienceLab services
                    </p>
                  </div>

                  <div className="grid gap-5 p-5 md:grid-cols-2">

                    {/* Inventory Service */}
                    <FaultCard
                      service="Inventory Service"
                      servicePath="inventory"
                    />

                    {/* Payment Service */}
                    <FaultCard
                      service="Payment Service"
                      servicePath="payment"
                    />

                  </div>
                </section>
              </>
            )}


            {/* Settings */}
            {activePage === "settings" && (
              <>
                <div className="mb-8">
                  <p className="text-sm text-slate-500">
                    Dashboard and monitoring configuration
                  </p>
                </div>

                <section className="rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      Dashboard Settings
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Current ResilienceLab dashboard configuration
                    </p>
                  </div>

                  <div className="divide-y divide-white/5">

                    <div className="flex items-center justify-between px-5 py-4">
                      <div>
                        <p className="text-sm font-medium text-slate-200">
                          Metrics Refresh
                        </p>

                        <p className="mt-1 text-xs text-slate-600">
                          Prometheus metrics refresh automatically every 5 seconds
                        </p>
                      </div>

                      <span className="rounded-full bg-emerald-400/5 px-3 py-1.5 text-xs font-medium text-emerald-400">
                        ACTIVE
                      </span>
                    </div>

                    <div className="flex items-center justify-between px-5 py-4">
                      <div>
                        <p className="text-sm font-medium text-slate-200">
                          Prometheus
                        </p>

                        <p className="mt-1 text-xs text-slate-600">
                          Monitoring metrics source
                        </p>
                      </div>

                      <span
                        className={`rounded-full px-3 py-1.5 text-xs font-medium ${prometheusConnected
                          ? "bg-emerald-400/5 text-emerald-400"
                          : "bg-red-400/5 text-red-400"
                          }`}
                      >
                        {prometheusConnected ? "CONNECTED" : "DISCONNECTED"}
                      </span>
                    </div>

                    <div className="flex items-center justify-between px-5 py-4">
                      <div>
                        <p className="text-sm font-medium text-slate-200">
                          Dashboard Version
                        </p>

                        <p className="mt-1 text-xs text-slate-600">
                          ResilienceLab Operations Console
                        </p>
                      </div>

                      <span className="text-xs font-medium text-slate-400">
                        v1.0
                      </span>
                    </div>

                  </div>
                </section>
              </>
            )}

            {activePage === "kafka" && (
              <>

                {/* Kafka Consumer Lag */}
                <section className="mt-6 rounded-2xl border border-white/10 bg-[#0f141c]">
                  <div className="border-b border-white/10 px-5 py-4">
                    <h3 className="text-sm font-semibold text-white">
                      Kafka Consumer Lag
                    </h3>

                    <p className="mt-1 text-xs text-slate-600">
                      Maximum records waiting for Kafka consumers
                    </p>
                  </div>

                  <div className="p-5">
                    {consumerLag.length === 0 ? (
                      <div className="flex h-40 items-center justify-center">
                        <div className="text-center">
                          <Radio className="mx-auto h-8 w-8 text-slate-500" />

                          <p className="mt-3 text-sm font-medium text-slate-300">
                            No Kafka consumer lag data available
                          </p>

                          <p className="mt-1 text-xs text-slate-600">
                            Kafka consumers may be disconnected or have no assigned
                            partitions
                          </p>
                        </div>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        {consumerLag.map((consumer) => (
                          <div
                            key={consumer.clientId}
                            className="flex items-center justify-between rounded-xl border border-white/10 bg-[#111827] px-4 py-3"
                          >
                            <div>
                              <p className="text-sm font-medium text-white">
                                {consumer.clientId}
                              </p>

                              <p className="mt-1 text-xs text-slate-500">
                                Kafka consumer
                              </p>
                            </div>

                            <span className="text-sm font-semibold text-slate-300">
                              {Number.isFinite(consumer.value)
                                ? consumer.value.toFixed(0)
                                : "N/A"}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </section>
              </>
            )}


          </div>
        </main>
      </div>
    </div>
  );
}

export default App;