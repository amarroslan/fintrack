const RM_FORMATTER = new Intl.NumberFormat("en-MY", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function formatRM(amount: number): string {
  const sign = amount < 0 ? "-" : "";
  return `${sign}RM ${RM_FORMATTER.format(Math.abs(amount))}`;
}
