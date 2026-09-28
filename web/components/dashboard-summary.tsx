import { formatRM } from "@/lib/format";
import type { DashboardSummary } from "@/lib/queries";

const STATUS_CLASS: Record<string, string> = {
  "On track": "status-success",
  "Approaching limit": "status-warning",
  "Over budget": "status-danger",
};

export function DashboardSummaryCards({ summary }: { summary: DashboardSummary }) {
  const statusClass = STATUS_CLASS[summary.status] ?? "muted";

  return (
    <section className="summary-cards" aria-label="Budget summary">
      <div className="card summary-card summary-card-primary">
        <span className="summary-label">Total spent</span>
        <span className="summary-value">{formatRM(summary.totalSpent)}</span>
      </div>
      <div className="card summary-card">
        <span className="summary-label">Monthly budget</span>
        <span className="summary-value">
          {summary.budget === null ? "Not set" : formatRM(summary.budget)}
        </span>
      </div>
      <div className="card summary-card">
        <span className="summary-label">Remaining</span>
        <span className="summary-value">
          {summary.remaining === null ? "—" : formatRM(summary.remaining)}
        </span>
      </div>
      <div className="card summary-card">
        <span className="summary-label">Budget status</span>
        <span className={`summary-value ${statusClass}`}>{summary.status}</span>
      </div>
    </section>
  );
}
