import Link from "next/link";
import { redirect } from "next/navigation";

import { AuthForm } from "@/components/auth-form";
import { getCurrentUser } from "@/lib/auth";

export default async function RegisterPage() {
  if (await getCurrentUser()) {
    redirect("/dashboard");
  }

  return (
    <main className="auth-page">
      <div className="card auth-card">
        <h1>Create your account</h1>
        <p className="muted">Track spending and budgets with FinTrack.</p>
        <AuthForm mode="register" />
        <p className="muted">
          Already registered? <Link href="/login">Log in</Link>
        </p>
      </div>
    </main>
  );
}
