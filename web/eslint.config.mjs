import nextPlugin from "@next/eslint-plugin-next";

export default [
  nextPlugin.flatConfig.recommended,
  nextPlugin.flatConfig.coreWebVitals,
  {
    ignores: [".next/**", "node_modules/**", "drizzle/**"],
  },
  {
    rules: {
      "react/no-unescaped-entities": "off",
    },
  },
];
