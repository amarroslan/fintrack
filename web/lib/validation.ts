export function parseAmount(input: string): number | null {
  const trimmed = input.trim();
  if (trimmed === "") {
    return null;
  }
  const value = Number(trimmed);
  if (!Number.isFinite(value) || value <= 0) {
    return null;
  }
  return value;
}

export function parseCategory(input: string): string | null {
  const trimmed = input.trim();
  if (trimmed === "" || trimmed.length > 60) {
    return null;
  }
  return trimmed;
}
