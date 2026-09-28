"use client";

export default function DashboardError({ reset }: { error: Error; reset: () => void }) {
  return (
    <main className="dashboard">
      <div className="card">
        <h1>Something went wrong</h1>
        <p className="muted">FinTrack could not load your data. Please try again.</p>
        <button className="button button-primary" onClick={reset}>
          Try again
        </button>
      </div>
    </main>
  );
}
