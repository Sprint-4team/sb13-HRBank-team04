export const buildProfileUrl = (
  fileId: number | null | undefined,
): string | undefined => {
  if (!fileId) return undefined;

  const isProduction = import.meta.env.MODE === "production";
  const productionUrl = import.meta.env.VITE_APP_API_URL;

  if (isProduction && productionUrl) {
    let baseUrl = productionUrl.endsWith("/")
      ? productionUrl.slice(0, -1)
      : productionUrl;

    if (baseUrl.endsWith("/api")) {
      baseUrl = baseUrl.slice(0, -4);
    }

    return `${baseUrl}/api/files/${fileId}/download`;
  }

  return `/api/files/${fileId}/download`;
};
