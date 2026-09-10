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

import { useServiceHealth } from "./hooks/useServiceHealth";
import { services } from "./config/services";
import { usePrometheusMetrics } from "./hooks/usePrometheusMetrics";
import { useRequestRate } from "./hooks/useRequestRate";

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

function App() {
  const { health, loading, lastUpdated, refresh } = useServiceHealth();
  const { metrics } = usePrometheusMetrics();
  const { requestRate } = useRequestRate();

  const healthyCount = services.filter(
    (service) => health[service.id]?.status === "UP"
  ).length;

  const downCount = services.length - healthyCount;

  return (
    <div className="min-h-screen bg-[#0a0d12] text-slate-200">
      <div className="flex min-h-screen">

        {/* Sidebar */}
        <aside className="hidden w-64 shrink-0 border-r border-white/10 bg-[#0d1117] lg:flex lg:flex-col">
          <div className="flex h-20 items-center gap-3 border-b border-white/10 px-6">
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

          <nav className="flex-1 space-y-1 px-3 py-6">
            <p className="px-3 pb-3 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-600">
              Monitor
            </p>

            <button className="flex w-full items-center gap-3 rounded-xl bg-indigo-500/10 px-3 py-2.5 text-sm font-medium text-indigo-300 ring-1 ring-indigo-400/10">
              <LayoutDashboard className="h-4 w-4" />
              Overview
            </button>

            <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
              <Activity className="h-4 w-4" />
              Metrics
            </button>

            <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
              <Radio className="h-4 w-4" />
              Kafka
            </button>

            <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
              <Gauge className="h-4 w-4" />
              Resilience
            </button>

            <p className="px-3 pb-3 pt-7 text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-600">
              Operations
            </p>

            <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
              <Zap className="h-4 w-4" />
              Fault Injection
            </button>

            <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
              <Settings className="h-4 w-4" />
              Settings
            </button>
          </nav>

          <div className="border-t border-white/10 p-4">
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
        <main className="min-w-0 flex-1">

          {/* Header */}
          <header className="flex h-20 items-center justify-between border-b border-white/10 bg-[#0a0d12]/90 px-6 backdrop-blur-xl lg:px-8">
            <div>
              <div className="flex items-center gap-2 text-xs text-slate-500">
                <span>Operations</span>
                <span>/</span>

                <span className="text-slate-300">
                  Overview
                </span>
              </div>

              <h2 className="mt-1 text-xl font-semibold text-white">
                System Overview
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

          <div className="p-6 lg:p-8">

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
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />

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
          </div>
        </main>
      </div>
    </div>
  );
}

export default App;