// Browser E2E using the locally installed Chrome (playwright-core, no browser download).
// Usage: node scripts/e2e-ui-test.mjs [base-url]
import { chromium } from "playwright-core";

const base = process.argv[2] ?? "http://localhost:3111";
const username = "e2e_ui_" + Date.now();
const logs = [];

function chromeCandidates() {
  if (process.platform === "win32") {
    return [
      "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
      "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
      process.env.LOCALAPPDATA + "\\Google\\Chrome\\Application\\chrome.exe",
    ];
  }
  return ["/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser"];
}

async function main() {
  const fs = await import("node:fs");
  const exe = chromeCandidates().find((p) => p && fs.existsSync(p));
  if (!exe) throw new Error("No installed Chrome found");
  logs.push("chrome: " + exe);
  logs.push("target: " + base);

  const browser = await chromium.launch({
    executablePath: exe,
    headless: true,
  });
  const page = await (await browser.newContext()).newPage();

  // 1. Register
  await page.goto(base + "/register");
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', "test-password-123");
  await page.click('button[type="submit"]');
  await page.waitForURL("**/dashboard", { timeout: 30000 });
  logs.push("registered " + username + " -> dashboard");

  // 2. Dashboard renders summary + forms
  await page.waitForSelector("text=Total spent");
  await page.waitForSelector("text=Add transaction");
  logs.push("dashboard renders summary cards and forms");

  // 3. Add a transaction via the form
  await page.fill('input[name="amount"]', "42.75");
  await page.selectOption('select[name="category"]', "Food");
  await page.click('button:has-text("Add transaction")');
  await page.waitForSelector("text=Transaction added.");
  logs.push("transaction added");

  // 4. Row appears in history table
  await page.waitForSelector("td:has-text('Food')");
  await page.waitForSelector("text=RM 42.75");
  logs.push("history shows Food / RM 42.75");

  // 5. Delete it with confirmation
  page.once("dialog", (dialog) => dialog.accept());
  await page.click('button:has-text("Delete")');
  await page.waitForSelector("text=No transactions yet", { timeout: 20000 });
  logs.push("transaction deleted after confirm");

  // 6. Logout
  await page.click('button:has-text("Log out")');
  await page.waitForURL("**/login", { timeout: 20000 });
  logs.push("logout -> /login");

  // 7. Session really gone: dashboard redirects to login
  await page.goto(base + "/dashboard");
  await page.waitForURL("**/login", { timeout: 20000 });
  logs.push("dashboard after logout redirects to /login");

  await browser.close();
  console.log("E2E UI TEST PASSED");
  for (const line of logs) console.log(" - " + line);
  process.exit(0);
}

main().catch(async (error) => {
  console.error("E2E UI TEST FAILED:", error?.message ?? error);
  for (const line of logs) console.error(" - " + line);
  process.exit(1);
});
