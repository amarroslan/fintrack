import { describe, expect, it } from "vitest";

import { budgetStatus } from "@/lib/budget";
import { formatRM } from "@/lib/format";
import { parseAmount, parseCategory } from "@/lib/validation";

describe("parseAmount", () => {
  it("accepts positive numeric input and trims whitespace", () => {
    expect(parseAmount(" 12.50 ")).toBe(12.5);
    expect(parseAmount("1000")).toBe(1000);
  });

  it("rejects blank, zero, negative, non-numeric, NaN, and infinite input", () => {
    for (const value of ["", "   ", "0", "-1", "abc", "NaN", "Infinity", "12,50", "1e400"]) {
      expect(parseAmount(value)).toBeNull();
    }
  });
});

describe("parseCategory", () => {
  it("accepts and trims 1-60 character categories", () => {
    expect(parseCategory("  Food ")).toBe("Food");
    expect(parseCategory("x")).toBe("x");
    expect(parseCategory("x".repeat(60))).toBe("x".repeat(60));
  });

  it("rejects empty or over-long categories", () => {
    expect(parseCategory("")).toBeNull();
    expect(parseCategory("   ")).toBeNull();
    expect(parseCategory("x".repeat(61))).toBeNull();
  });
});

describe("formatRM", () => {
  it("formats amounts as RM with two decimals and thousands separators", () => {
    expect(formatRM(1250.5)).toBe("RM 1,250.50");
    expect(formatRM(0)).toBe("RM 0.00");
    expect(formatRM(-35)).toBe("-RM 35.00");
  });
});

describe("budgetStatus", () => {
  it("returns the four spec states", () => {
    expect(budgetStatus(0, 0)).toBe("No budget set");
    expect(budgetStatus(-5, 10)).toBe("No budget set");
    expect(budgetStatus(1000, 500)).toBe("On track");
    expect(budgetStatus(1000, 799)).toBe("On track");
    expect(budgetStatus(1000, 800)).toBe("Approaching limit");
    expect(budgetStatus(1000, 1000)).toBe("Approaching limit");
    expect(budgetStatus(1000, 1200)).toBe("Over budget");
  });
});
