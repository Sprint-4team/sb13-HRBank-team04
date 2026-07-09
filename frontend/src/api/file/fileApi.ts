import axios from "axios";

/**
 * 파일 다운로드
 * - 서버: GET /api/files/{id}/download
 * - 응답: 바이너리(blob)
 */
export async function downloadFileById(fileId: number): Promise<{ blob: Blob; filename: string }> {
  const response = await axios.get(`/api/files/${fileId}/download`, {
    responseType: "blob",
  });

  const blob = new Blob([response.data]);

  const disposition = response.headers["content-disposition"];
  let filename = "download";

  if (disposition) {
    const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/);
    if (match && match[1]) {
      filename = decodeURIComponent(match[1]);
    }
  }

  return { blob, filename };
}
