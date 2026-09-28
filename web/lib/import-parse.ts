import { parseAmount, parseCategory } from "@/lib/validation";

export type ExportUser = { username: string; password: string };
export type ExportBudget = { username: string; monthlyAmount: number };
export type ExportTransaction = {
  username: string;
  category: string;
  amount: number;
  occurredAt: Date;
};

export type ValidExport = {
  users: ExportUser[];
  budgets: ExportBudget[];
  transactions: ExportTransaction[];
};

export type ParseResult = {
  valid: ValidExport[];
  rejected: string[];
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function field(row: Record<string, unknown>, key: string): unknown {
  return row[key];
}

export function parseExport(raw: string): ParseResult {
  const rejected: string[] = [];
  let data: unknown;

  try {
    data = JSON.parse(raw);
  } catch {
    return { valid: [], rejected: ["Export file is not valid JSON."] };
  }

  if (!isRecord(data)) {
    return { valid: [], rejected: ["Export root must be an object."] };
  }

  // Group rows per username so each user becomes one self-contained import unit.
  const usersByUsername = new Map<string, ExportUser>();
  const budgetsByUsername = new Map<string, ExportBudget>();
  const transactionsByUsername = new Map<string, ExportTransaction[]>();

  const userRows: unknown[] = Array.isArray(field(data, "users")) ? (field(data, "users") as unknown[]) : [];
  for (const entry of userRows) {
    if (!isRecord(entry)) {
      rejected.push("Malformed user row.");
      continue;
    }
    const username = typeof field(entry, "username") === "string" ? String(field(entry, "username")).trim() : "";
    const password = typeof field(entry, "password") === "string" ? String(field(entry, "password")) : "";
    if (!username || username.length > 60) {
      rejected.push(`User row rejected: invalid username.`);
      continue;
    }
    if (password.length < 1) {
      rejected.push(`User row rejected: ${username} has an empty password.`);
      continue;
    }
    usersByUsername.set(username, { username, password });
  }

  const budgetRows: unknown[] = Array.isArray(field(data, "budgets")) ? (field(data, "budgets") as unknown[]) : [];
  for (const entry of budgetRows) {
    if (!isRecord(entry)) {
      rejected.push("Malformed budget row.");
      continue;
    }
    const username = typeof field(entry, "username") === "string" ? String(field(entry, "username")).trim() : "";
    const amount = parseAmount(String(field(entry, "monthlyAmount") ?? ""));
    if (!username || amount === null) {
      rejected.push(`Budget row rejected for ${username || "unknown user"}: invalid amount.`);
      continue;
    }
    budgetsByUsername.set(username, { username, monthlyAmount: amount });
  }

  const transactionRows: unknown[] = Array.isArray(field(data, "transactions")) ? (field(data, "transactions") as unknown[]) : [];
  for (const entry of transactionRows) {
    if (!isRecord(entry)) {
      rejected.push("Malformed transaction row.");
      continue;
    }
    const username = typeof field(entry, "username") === "string" ? String(field(entry, "username")).trim() : "";
    const category = parseCategory(String(field(entry, "category") ?? ""));
    const amount = parseAmount(String(field(entry, "amount") ?? ""));
    const occurredRaw = field(entry, "occurredAt");
    const occurredAt =
      typeof occurredRaw === "string" && !Number.isNaN(Date.parse(occurredRaw))
        ? new Date(occurredRaw)
        : null;

    if (!username || category === null || amount === null || occurredAt === null) {
      rejected.push(
        `Transaction row rejected for ${username || "unknown user"}: invalid category, amount, or date.`,
      );
      continue;
    }
    const list = transactionsByUsername.get(username) ?? [];
    list.push({ username, category, amount, occurredAt });
    transactionsByUsername.set(username, list);
  }

  const usernames = new Set<string>([
    ...usersByUsername.keys(),
    ...budgetsByUsername.keys(),
    ...transactionsByUsername.keys(),
  ]);
  const valid: ValidExport[] = [];
  for (const username of usernames) {
    const user = usersByUsername.get(username);
    if (!user) {
      rejected.push(
        `Budget/transaction rows for "${username}" skipped: no matching user row to establish credentials.`,
      );
      continue;
    }
    valid.push({
      users: [user],
      budgets: budgetsByUsername.has(username) ? [budgetsByUsername.get(username)!] : [],
      transactions: transactionsByUsername.get(username) ?? [],
    });
  }

  return { valid, rejected };
}
