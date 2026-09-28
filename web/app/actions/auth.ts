"use server";

import { eq } from "drizzle-orm";
import { redirect } from "next/navigation";

import { db } from "@/db";
import { users } from "@/db/schema";
import { createSession, destroySession } from "@/lib/auth";
import { hashPassword, verifyPassword } from "@/lib/password";
import { parseCategory } from "@/lib/validation";

export type AuthState = { error?: string };

function normalizeUsername(input: FormDataEntryValue | null): string | null {
  const value = typeof input === "string" ? input.trim().toLowerCase() : "";
  return parseCategory(value);
}

export async function registerAction(
  _prev: AuthState,
  formData: FormData,
): Promise<AuthState> {
  const username = normalizeUsername(formData.get("username"));
  const password = typeof formData.get("password") === "string" ? String(formData.get("password")) : "";

  if (!username) {
    return { error: "Enter a username of 1-60 characters." };
  }
  if (password.length < 8) {
    return { error: "Password must be at least 8 characters." };
  }

  const existing = await db.select({ id: users.id }).from(users).where(eq(users.username, username)).limit(1);
  if (existing.length > 0) {
    return { error: "That username is already taken." };
  }

  const { hash, salt } = hashPassword(password);
  const inserted = await db
    .insert(users)
    .values({ username, passwordHash: hash, passwordSalt: salt })
    .returning({ id: users.id });

  await createSession(inserted[0].id);
  redirect("/dashboard");
}

export async function loginAction(
  _prev: AuthState,
  formData: FormData,
): Promise<AuthState> {
  const username = normalizeUsername(formData.get("username"));
  const password = typeof formData.get("password") === "string" ? String(formData.get("password")) : "";

  if (!username || password === "") {
    return { error: "Invalid username or password." };
  }

  const rows = await db
    .select({ id: users.id, passwordHash: users.passwordHash, passwordSalt: users.passwordSalt })
    .from(users)
    .where(eq(users.username, username))
    .limit(1);

  const user = rows[0];
  if (!user || !verifyPassword(password, user.passwordHash, user.passwordSalt)) {
    return { error: "Invalid username or password." };
  }

  await createSession(user.id);
  redirect("/dashboard");
}

export async function logoutAction(): Promise<void> {
  await destroySession();
  redirect("/login");
}
