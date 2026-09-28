import { describe, expect, it } from "vitest";

import {
  generateSessionToken,
  hashPassword,
  hashToken,
  verifyPassword,
} from "@/lib/password";

describe("password", () => {
  it("hashes with a per-user random salt and never stores plaintext", () => {
    const { hash, salt } = hashPassword("correct horse battery staple");
    expect(salt).toMatch(/^[0-9a-f]{32}$/);
    expect(hash).toMatch(/^[0-9a-f]+$/);
    expect(hash).not.toContain("correct horse");
    expect(hash).not.toContain("battery");
  });

  it("verifies the correct password and rejects wrong ones", () => {
    const { hash, salt } = hashPassword("s3cret!");
    expect(verifyPassword("s3cret!", hash, salt)).toBe(true);
    expect(verifyPassword("S3cret!", hash, salt)).toBe(false);
    expect(verifyPassword("", hash, salt)).toBe(false);
  });

  it("produces a distinct salt per call so equal passwords hash differently", () => {
    const a = hashPassword("same");
    const b = hashPassword("same");
    expect(a.salt).not.toBe(b.salt);
    expect(a.hash).not.toBe(b.hash);
  });

  it("generates 32-byte session tokens and hashes them to sha256 hex", () => {
    const token = generateSessionToken();
    expect(token).toMatch(/^[0-9a-f]{64}$/);
    expect(hashToken(token)).toMatch(/^[0-9a-f]{64}$/);
    expect(hashToken(token)).toBe(hashToken(token));
    expect(hashToken("not-the-token")).not.toBe(hashToken(token));
  });
});
