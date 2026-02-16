import type { Liquor } from "../types/liquor";

interface LiquorApiResponse {
  id: number;
  productCode: string;
  name: string;
  brand: string;
  category: string;
  volume: number;
  alcoholPercent: number;
  country: string;
  currentPrice: number;
  originalPrice: number;
  imageUrl: string;
  productUrl: string;
  source: string;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function toFrontendLiquor(item: LiquorApiResponse): Liquor {
  const currentPrice = typeof item.currentPrice === "number" ? item.currentPrice : 0;
  const originalPrice = typeof item.originalPrice === "number" ? item.originalPrice : currentPrice;

  return {
    id: typeof item.id === "number" ? item.id : 0,
    product_code: item.productCode ?? "",
    name: item.name ?? "Unknown",
    brand: item.brand ?? "Unknown",
    category: item.category ?? "Whisky",
    volume: typeof item.volume === "number" ? item.volume : 700,
    alcohol_percent: typeof item.alcoholPercent === "number" ? item.alcoholPercent : 0,
    country: item.country ?? "Unknown",
    current_price: currentPrice,
    original_price: originalPrice,
    image_url: item.imageUrl ?? "",
    product_url: item.productUrl ?? "",
    source: item.source ?? "WHISKY_DB",
  };
}

export async function fetchLiquors(searchQuery?: string, signal?: AbortSignal): Promise<Liquor[]> {
  const trimmed = searchQuery?.trim() ?? "";
  const endpoint = trimmed
    ? `/api/liquors/search?q=${encodeURIComponent(trimmed)}`
    : "/api/liquors";

  const response = await fetch(`${API_BASE_URL}${endpoint}`, { signal });
  if (!response.ok) {
    throw new Error(`주류 목록 조회 실패: ${response.status}`);
  }

  const data = (await response.json()) as LiquorApiResponse[];
  return data.map(toFrontendLiquor);
}
