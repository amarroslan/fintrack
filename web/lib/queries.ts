import { and, desc, eq, gte, sql } from "drizzle-orm";

import { db } from "@/db";
import { budgets, transactions } from "@/db/schema";
import { budgetStatus, type BudgetStatus } from "@/lib/budget";

export type TransactionRow = {
  id: string;
  category: string;
  amount: number;
  occurredAt: Date;
};

export type CategoryTotal = {
  category: string;
  total: number;
};

export type DashboardSummary = {
  totalSpent: number;
  budget: number | null;
  remaining: number | null;
  status: BudgetStatus;
};

export type DashboardData = {
  summary: DashboardSummary;
  transactions: TransactionRow[];
  categoryTotals: CategoryTotal[];
  monthCategoryTotals: CategoryTotal[];
};

function monthStart(): Date {
  const now = new Date();
  return new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1));
}

export async function getTransactions(userId: string): Promise<TransactionRow[]> {
  const rows = await db
    .select({
      id: transactions.id,
      category: transactions.category,
      amount: transactions.amount,
      occurredAt: transactions.occurredAt,
    })
    .from(transactions)
    .where(eq(transactions.userId, userId))
    .orderBy(desc(transactions.occurredAt), desc(transactions.createdAt));

  return rows.map((row) => ({
    id: row.id,
    category: row.category,
    amount: Number(row.amount),
    occurredAt: row.occurredAt,
  }));
}

export async function getBudget(userId: string): Promise<number | null> {
  const rows = await db
    .select({ monthlyAmount: budgets.monthlyAmount })
    .from(budgets)
    .where(eq(budgets.userId, userId))
    .limit(1);
  return rows.length > 0 ? Number(rows[0].monthlyAmount) : null;
}

export async function getCategoryTotals(
  userId: string,
  since?: Date,
): Promise<CategoryTotal[]> {
  const conditions = since
    ? and(eq(transactions.userId, userId), gte(transactions.occurredAt, since))
    : eq(transactions.userId, userId);

  const rows = await db
    .select({
      category: transactions.category,
      total: sql<string>`sum(${transactions.amount})`,
    })
    .from(transactions)
    .where(conditions)
    .groupBy(transactions.category)
    .orderBy(desc(sql`sum(${transactions.amount})`));

  return rows.map((row) => ({ category: row.category, total: Number(row.total) }));
}

export async function getDashboardData(userId: string): Promise<DashboardData> {
  const [txRows, budget, categoryTotals, monthCategoryTotals] = await Promise.all([
    getTransactions(userId),
    getBudget(userId),
    getCategoryTotals(userId),
    getCategoryTotals(userId, monthStart()),
  ]);

  const totalSpent = txRows.reduce((sum, tx) => sum + tx.amount, 0);
  const monthSpent = monthCategoryTotals.reduce((sum, row) => sum + row.total, 0);
  const effectiveBudget = budget ?? 0;

  return {
    summary: {
      totalSpent,
      budget,
      remaining: budget === null ? null : budget - monthSpent,
      status: budgetStatus(effectiveBudget, monthSpent),
    },
    transactions: txRows,
    categoryTotals,
    monthCategoryTotals,
  };
}
