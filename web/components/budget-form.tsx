"use client";

import { useActionState } from "react";

import { setBudgetAction, type BudgetFormState } from "@/app/actions/budget";

export function BudgetForm() {
  const [state, formAction, pending] = useActionState<BudgetFormState, FormData>(
    setBudgetAction,
    {},
  );

  return (
    <div className="card form-card">
      <h2>Monthly budget</h2>
      <form action={formAction} className="stack">
        <label className="field">
          <span className="field-label">Budget amount (RM)</span>
          <input
            className="input"
            name="monthlyAmount"
            inputMode="decimal"
            placeholder="2000.00"
            required
          />
        </label>
        {state.error ? <p className="status-danger">{state.error}</p> : null}
        {state.success ? <p className="status-success">{state.success}</p> : null}
        <button className="button button-secondary" type="submit" disabled={pending}>
          {pending ? "Saving…" : "Save budget"}
        </button>
      </form>
    </div>
  );
}
