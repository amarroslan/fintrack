import { describe, expect, it } from "vitest";

import { parseExport } from "@/lib/import-parse";

const VALID = {
  users: [{ username: "alex", password: "legacy-password" }],
  budgets: [{ username: "alex", monthlyAmount: "1200.00" }],
  transactions: [
    { username: "alex", category: "Food", amount: "12.50", occurredAt: "2026-09-28T12:30:00Z" },
  ],
};

describe("parseExport", () => {
  it("accepts a well-formed export", () => {
    const result = parseExport(JSON.stringify(VALID));
    expect(result.valid).toHaveLength(1);
    expect(result.valid[0].users[0]).toEqual({ username: "alex", password: "legacy-password" });
    expect(result.valid[0].budgets[0]).toEqual({ username: "alex", monthlyAmount: 1200 });
    expect(result.valid[0].transactions[0]).toEqual({
      username: "alex",
      category: "Food",
      amount: 12.5,
      occurredAt: new Date("2026-09-28T12:30:00Z"),
    });
    expect(result.rejected).toHaveLength(0);
  });

  it("rejects malformed JSON", () => {
    const result = parseExport("{not json");
    expect(result.valid).toHaveLength(0);
    expect(result.rejected.length).toBeGreaterThan(0);
  });

  it("rejects rows with invalid usernames, amounts, or dates", () => {
    const bad = {
      users: [{ username: "", password: "x" }, { username: "ok", password: "" }],
      budgets: [{ username: "ghost", monthlyAmount: "-5" }],
      transactions: [
        { username: "alex", category: " ", amount: "0", occurredAt: "2026-09-28T12:30:00Z" },
        { username: "alex", category: "Food", amount: "12.50", occurredAt: "not-a-date" },
      ],
    };
    const result = parseExport(JSON.stringify(bad));
    expect(result.valid).toHaveLength(0);
    expect(result.rejected.length).toBe(5);
  });
});
