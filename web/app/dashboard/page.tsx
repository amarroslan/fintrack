import Link from "next/link";

import { logoutAction } from "@/app/actions/auth";
import { DashboardSummaryCards } from "@/components/dashboard-summary";
import { TransactionForm } from "@/components/transaction-form";
import { BudgetForm } from "@/components/budget-form";
import { TransactionHistory } from "@/components/transaction-history";
import { InsightsSection } from "@/components/insights-section";
import { requireUser } from "@/lib/auth";
import { getDashboardData } from "@/lib/queries";

export default async function DashboardPage() {
  const user = await requireUser();
  const data = await getDashboardData(user.id);

  return (
    <main className="dashboard">
      <header className="dashboard-header">
        <div>
          <h1>FinTrack</h1>
          <p className="muted">A clearer view of your everyday spending.</p>
        </div>
        <div className="dashboard-user">
          <span>
            Signed in as <strong>{user.username}</strong>
          </span>
          <form action={logoutAction}>
            <button className="button button-secondary" type="submit">
              Log out
            </button>
          </form>
        </div>
      </header>

      <DashboardSummaryCards summary={data.summary} />

      <div className="dashboard-grid">
        <section className="dashboard-forms">
          <TransactionForm />
          <BudgetForm />
        </section>

        <section className="dashboard-history">
          <TransactionHistory transactions={data.transactions} />
          <InsightsSection
            categoryTotals={data.monthCategoryTotals}
            totalSpent={data.summary.totalSpent}
          />
        </section>
      </div>

      <footer className="dashboard-footer">
        <Link href="/dashboard" className="muted">
          Refresh
        </Link>
      </footer>
    </main>
  );
}
