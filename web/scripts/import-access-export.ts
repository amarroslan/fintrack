import "dotenv/config";
import { readFileSync } from "node:fs";
import { eq } from "drizzle-orm";
import { Pool } from "pg";

import { db, pool } from "@/db";
import { budgets, transactions, users } from "@/db/schema";
import { hashPassword } from "@/lib/password";
import { parseExport } from "@/lib/import-parse";

function usage(): never {
  console.error("Usage: npm run import-access -- path/to/export.json");
  process.exit(1);
}

async function main() {
  const path = process.argv[2];
  if (!path) usage();

  if (!process.env.DATABASE_URL) {
    console.error("DATABASE_URL is not set. Copy .env.example to .env.local first.");
    process.exit(1);
  }

  const raw = readFileSync(path, "utf8");
  const { valid, rejected } = parseExport(raw);

  if (valid.length === 0) {
    console.error("Nothing to import. Rejections:");
    for (const line of rejected) console.error(" - " + line);
    process.exit(1);
  }

  let importedUsers = 0;
  let importedBudgets = 0;
  let importedTransactions = 0;

  for (const unit of valid) {
    const user = unit.users[0];
    const { hash, salt } = hashPassword(user.password);

    const inserted = await db
      .insert(users)
      .values({ username: user.username, passwordHash: hash, passwordSalt: salt })
      .onConflictDoNothing({ target: users.username })
      .returning({ id: users.id });

    let userId = inserted[0]?.id;
    if (!userId) {
      const existing = await db
        .select({ id: users.id })
        .from(users)
        .where(eq(users.username, user.username));
      userId = existing[0]?.id;
    }
    if (!userId) {
      rejected.push(`Could not create or find user ${user.username}; its rows were skipped.`);
      continue;
    }
    importedUsers++;

    for (const budget of unit.budgets) {
      await db
        .insert(budgets)
        .values({ userId, monthlyAmount: budget.monthlyAmount.toFixed(2), updatedAt: new Date() })
        .onConflictDoUpdate({
          target: budgets.userId,
          set: { monthlyAmount: budget.monthlyAmount.toFixed(2), updatedAt: new Date() },
        });
      importedBudgets++;
    }

    if (unit.transactions.length > 0) {
      await db.insert(transactions).values(
        unit.transactions.map((tx) => ({
          userId,
          category: tx.category,
          amount: tx.amount.toFixed(2),
          occurredAt: tx.occurredAt,
        })),
      );
      importedTransactions += unit.transactions.length;
    }
  }

  console.log(`Imported users: ${importedUsers}`);
  console.log(`Imported budgets: ${importedBudgets}`);
  console.log(`Imported transactions: ${importedTransactions}`);
  if (rejected.length > 0) {
    console.log("Rejections:");
    for (const line of rejected) console.log(" - " + line);
  }
}

main()
  .catch((error) => {
    console.error("Import failed:", error);
    process.exit(1);
  })
  .finally(() => {
    void pool.end();
  });
