"use server";

import { and, eq } from "drizzle-orm";
import { revalidatePath } from "next/cache";

import { db } from "@/db";
import { transactions } from "@/db/schema";
import { requireUser } from "@/lib/auth";
import { parseAmount, parseCategory } from "@/lib/validation";

export type TransactionFormState = { error?: string; success?: string };

export async function addTransactionAction(
  _prev: TransactionFormState,
  formData: FormData,
): Promise<TransactionFormState> {
  const user = await requireUser();

  const category = parseCategory(String(formData.get("category") ?? ""));
  const amount = parseAmount(String(formData.get("amount") ?? ""));

  if (!category) {
    return { error: "Choose or enter a category (1-60 characters)." };
  }
  if (amount === null) {
    return { error: "Enter a positive amount, such as 25.50." };
  }

  try {
    await db.insert(transactions).values({
      userId: user.id,
      category,
      amount: amount.toFixed(2),
    });
  } catch {
    return { error: "The transaction could not be saved. Please try again." };
  }

  revalidatePath("/dashboard");
  return { success: "Transaction added." };
}

export async function deleteTransactionAction(formData: FormData): Promise<void> {
  const user = await requireUser();
  const id = String(formData.get("id") ?? "");
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) {
    return;
  }

  await db
    .delete(transactions)
    .where(and(eq(transactions.id, id), eq(transactions.userId, user.id)));

  revalidatePath("/dashboard");
}
