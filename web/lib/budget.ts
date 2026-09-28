export type BudgetStatus =
  | "No budget set"
  | "On track"
  | "Approaching limit"
  | "Over budget";

export function budgetStatus(budget: number, spent: number): BudgetStatus {
  if (!Number.isFinite(budget) || budget <= 0) {
    return "No budget set";
  }
  const ratio = spent / budget;
  if (ratio < 0.8) {
    return "On track";
  }
  if (ratio <= 1.0) {
    return "Approaching limit";
  }
  return "Over budget";
}
