"use client";

import { deleteTransactionAction } from "@/app/actions/transactions";
import { formatRM } from "@/lib/format";
import type { TransactionRow } from "@/lib/queries";

function formatDate(date: Date): string {
  return new Intl.DateTimeFormat("en-MY", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(date));
}

export function TransactionHistory({ transactions }: { transactions: TransactionRow[] }) {
  if (transactions.length === 0) {
    return (
      <div className="card">
        <h2>Transaction history</h2>
        <p className="muted">No transactions yet — add your first one from the form.</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h2>Transaction history</h2>
      <div className="table-wrap">
        <table className="transaction-table">
          <thead>
            <tr>
              <th scope="col">Date</th>
              <th scope="col">Category</th>
              <th scope="col" className="num">
                Amount
              </th>
              <th scope="col">
                <span className="visually-hidden">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((tx) => (
              <tr key={tx.id}>
                <td>{formatDate(tx.occurredAt)}</td>
                <td>{tx.category}</td>
                <td className="num">{formatRM(tx.amount)}</td>
                <td>
                  <form
                    action={deleteTransactionAction}
                    onSubmit={(event) => {
                      const confirmed = window.confirm(
                        `Delete the ${tx.category} transaction for ${formatRM(tx.amount)}?`,
                      );
                      if (!confirmed) {
                        event.preventDefault();
                      }
                    }}
                  >
                    <input type="hidden" name="id" value={tx.id} />
                    <button className="button button-danger button-small" type="submit">
                      Delete
                    </button>
                  </form>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="muted table-note">
        {transactions.length} transaction{transactions.length === 1 ? "" : "s"}, newest first.
      </p>
    </div>
  );
}
