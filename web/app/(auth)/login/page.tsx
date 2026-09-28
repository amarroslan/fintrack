import Link from "next/link";
import { redirect } from "next/navigation";

import { AuthForm } from "@/components/auth-form";
import { getCurrentUser } from "@/lib/auth";

export default async function LoginPage() {
  if (await getCurrentUser()) {
    redirect("/dashboard");
  }

  return (
    <main className="auth-page">
      <div className="card auth-card">
        <h1>FinTrack</h1>
        <p className="muted">A clearer view of your everyday spending.</p>
        <AuthForm mode="login" />
        <p className="muted">
          New here? <Link href="/register">Create an account</Link>
        </p>
      </div>
    </main>
  );
}
