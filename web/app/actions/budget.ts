"use server";

import { revalidatePath } from "next/cache";

import { db } from "@/db";
import { budgets } from "@/db/schema";
import { requireUser } from "@/lib/auth";
import { parseAmount } from "@/lib/validation";

export type BudgetFormState = { error?: string; success?: string };

export async function setBudgetAction(
  _prev: BudgetFormState,
  formData: FormData,
): Promise<BudgetFormState> {
  const user = await requireUser();
  const amount = parseAmount(String(formData.get("monthlyAmount") ?? ""));

  if (amount === null) {
    return { error: "Enter a positive monthly budget." };
  }

  try {
    await db
      .insert(budgets)
      .values({ userId: user.id, monthlyAmount: amount.toFixed(2), updatedAt: new Date() })
      .onConflictDoUpdate({
        target: budgets.userId,
        set: { monthlyAmount: amount.toFixed(2), updatedAt: new Date() },
      });
  } catch {
    return { error: "The budget could not be saved. Please try again." };
  }

  revalidatePath("/dashboard");
  return { success: "Budget updated." };
}
