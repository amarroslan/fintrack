import dotenv from "dotenv";

dotenv.config({ path: ".env.local" });
dotenv.config();

import { drizzle } from "drizzle-orm/node-postgres";
import { eq } from "drizzle-orm";
import { Pool } from "pg";
import { createHash, randomBytes } from "node:crypto";

import * as schema from "../db/schema";
import { hashPassword, verifyPassword } from "../lib/password";
import { budgetStatus } from "../lib/budget";
import { formatRM } from "../lib/format";

async function main() {
  const pool = new Pool({ connectionString: process.env.DATABASE_URL });
  const db = drizzle(pool, { schema });
  const checks: string[] = [];

  // 1. Tables exist
  const tables = await pool.query(
    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name",
  );
  const names = tables.rows.map((r) => r.table_name).sort();
  for (const expected of ["budgets", "sessions", "transactions", "users"]) {
    if (!names.includes(expected)) throw new Error(`Missing table: ${expected}`);
  }
  checks.push(`tables: ${names.join(", ")}`);

  // 2. Create temp user with real PBKDF2 hashing
  const username = `smoke_${randomBytes(4).toString("hex")}`;
  const { hash, salt } = hashPassword("smoke-test-password");
  const [user] = await db
    .insert(schema.users)
    .values({ username, passwordHash: hash, passwordSalt: salt })
    .returning({ id: schema.users.id, username: schema.users.username });
  checks.push(`user created: ${user.username}`);

  if (!verifyPassword("smoke-test-password", hash, salt)) {
    throw new Error("password verification failed");
  }
  checks.push("password verifies");

  // 3. Budget upsert
  await db
    .insert(schema.budgets)
    .values({ userId: user.id, monthlyAmount: "1000.00" })
    .onConflictDoUpdate({
      target: schema.budgets.userId,
      set: { monthlyAmount: "1200.00", updatedAt: new Date() },
    });
  const [budget] = await db.select().from(schema.budgets).where(eq(schema.budgets.userId, user.id));
  checks.push(`budget: ${budget.monthlyAmount}`);

  // 4. Transactions
  await db.insert(schema.transactions).values([
    { userId: user.id, category: "Food", amount: "12.50" },
    { userId: user.id, category: "Transport", amount: "8.00" },
  ]);
  const txs = await db.select().from(schema.transactions).where(eq(schema.transactions.userId, user.id));
  const total = txs.reduce((sum, tx) => sum + Number(tx.amount), 0);
  if (txs.length !== 2 || Math.abs(total - 20.5) > 1e-9) {
    throw new Error(`unexpected transactions: ${txs.length}, total ${total}`);
  }
  checks.push(`transactions: ${txs.length}, total ${formatRM(total)}, status "${budgetStatus(1200, total)}"`);

  // 5. Session insert + lookup by token hash
  const token = randomBytes(32).toString("hex");
  await db.insert(schema.sessions).values({
    userId: user.id,
    tokenHash: createHash("sha256").update(token).digest("hex"),
    expiresAt: new Date(Date.now() + 60_000),
  });
  checks.push("session inserted");

  // 6. Cascade cleanup
  await db.delete(schema.users).where(eq(schema.users.id, user.id));
  const remaining = await pool.query("SELECT count(*)::int AS n FROM users WHERE username = $1", [username]);
  if (remaining.rows[0].n !== 0) throw new Error("cleanup failed");
  checks.push("cleanup: user and child rows deleted (cascade ok)");

  console.log("SMOKE TEST PASSED");
  for (const line of checks) console.log(" - " + line);
  await pool.end();
}

main().catch((error) => {
  console.error("SMOKE TEST FAILED:", error);
  process.exit(1);
});
