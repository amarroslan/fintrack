"use client";

import { useActionState } from "react";

import { type AuthState, loginAction, registerAction } from "@/app/actions/auth";

export function AuthForm({ mode }: { mode: "login" | "register" }) {
  const action = mode === "login" ? loginAction : registerAction;
  const [state, formAction, pending] = useActionState<AuthState, FormData>(action, {});

  return (
    <form action={formAction} className="auth-form">
      <label className="field">
        <span className="field-label">Username</span>
        <input className="input" name="username" autoComplete="username" required maxLength={60} />
      </label>
      <label className="field">
        <span className="field-label">Password</span>
        <input
          className="input"
          name="password"
          type="password"
          autoComplete={mode === "login" ? "current-password" : "new-password"}
          required
          minLength={mode === "register" ? 8 : undefined}
        />
      </label>
      {state.error ? <p className="status-danger">{state.error}</p> : null}
      <button className="button button-primary" type="submit" disabled={pending}>
        {pending ? "Please wait…" : mode === "login" ? "Log in" : "Create account"}
      </button>
    </form>
  );
}
