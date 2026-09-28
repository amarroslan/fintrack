"use client";

import { formatRM } from "@/lib/format";
import type { CategoryTotal } from "@/lib/queries";

export function InsightsSection({
  categoryTotals,
  totalSpent,
}: {
  categoryTotals: CategoryTotal[];
  totalSpent: number;
}) {
  return (
    <div className="card">
      <h2>Spending insights</h2>
      {categoryTotals.length === 0 ? (
        <p className="muted">Add a transaction first to see your spending breakdown.</p>
      ) : (
        <>
          <ul className="insights-list">
            {categoryTotals.map((row) => {
              const share = totalSpent > 0 ? Math.round((row.total / totalSpent) * 100) : 0;
              return (
                <li key={row.category}>
                  <span>{row.category}</span>
                  <span className="muted">
                    {formatRM(row.total)} · {share}%
                  </span>
                </li>
              );
            })}
          </ul>
          <p className="muted table-note">This month, by category.</p>
        </>
      )}
    </div>
  );
}
