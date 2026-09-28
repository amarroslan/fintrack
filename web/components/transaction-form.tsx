"use client";

import { useActionState, useRef } from "react";

import { addTransactionAction, type TransactionFormState } from "@/app/actions/transactions";

const CATEGORIES = ["Food", "Transport", "Bills", "Shopping", "Health", "Entertainment", "Other"];

export function TransactionForm() {
  const [state, formAction, pending] = useActionState<TransactionFormState, FormData>(
    addTransactionAction,
    {},
  );
  const formRef = useRef<HTMLFormElement>(null);

  return (
    <div className="card form-card">
      <h2>Add transaction</h2>
      <form
        ref={formRef}
        action={async (formData) => {
          await formAction(formData);
          formRef.current?.reset();
        }}
        className="stack"
      >
        <label className="field">
          <span className="field-label">Category</span>
          <select className="input" name="category" defaultValue="Food">
            {CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span className="field-label">Amount (RM)</span>
          <input
            className="input"
            name="amount"
            inputMode="decimal"
            placeholder="25.50"
            required
          />
        </label>
        {state.error ? <p className="status-danger">{state.error}</p> : null}
        {state.success ? <p className="status-success">{state.success}</p> : null}
        <button className="button button-primary" type="submit" disabled={pending}>
          {pending ? "Saving…" : "Add transaction"}
        </button>
      </form>
    </div>
  );
}
