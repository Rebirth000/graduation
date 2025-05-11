import dayjs from "dayjs";

export function formatDateTime(dateTime) {
  return dayjs(dateTime).format("YYYY-MM-DD HH:mm:ss");
}
