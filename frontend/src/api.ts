import type { Scan, Status } from "./types";

const base = import.meta.env.VITE_API_BASE ?? "";

async function parse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let message = "小砍走神了一下，再试一次";
    try {
      const body = (await res.json()) as { error?: string };
      if (body.error) message = body.error;
    } catch {
      /* ignore */
    }
    throw new Error(message);
  }
  return (await res.json()) as T;
}

export async function fetchStatus(): Promise<Status> {
  return parse(await fetch(`${base}/api/status`));
}

export async function createScan(files: File[]): Promise<Scan> {
  const form = new FormData();
  for (const file of files) {
    form.append("photos", file);
  }
  return parse(
    await fetch(`${base}/api/scans`, {
      method: "POST",
      body: form
    })
  );
}

export async function fetchScan(id: string | number): Promise<Scan> {
  return parse(await fetch(`${base}/api/scans/${id}`));
}

export async function fetchHistory(): Promise<Scan[]> {
  return parse(await fetch(`${base}/api/scans`));
}

export async function recordDeal(
  id: string | number,
  bought: boolean,
  finalPrice?: number
): Promise<Scan> {
  return parse(
    await fetch(`${base}/api/scans/${id}/deal`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        bought,
        final_price: bought ? finalPrice : null
      })
    })
  );
}

export function photoUrl(url: string): string {
  return `${base}${url}`;
}

export function yuan(value?: number | null): string {
  if (value === null || value === undefined) return "—";
  return `¥${Number(value).toFixed(0)}`;
}
