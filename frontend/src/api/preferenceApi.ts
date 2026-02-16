import type { Liquor } from "../types/liquor";
import type { FlavorVector, PreferenceResult } from "../types/preference";

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

interface RecommendationItemApiResponse {
  liquor: LiquorApiResponse;
  similarity: number;
  reason: string;
}

interface RecommendationApiResponse {
  typeName: string;
  flavorVector: FlavorVector;
  recommendations: RecommendationItemApiResponse[];
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function toFrontendLiquor(item: LiquorApiResponse): Liquor {
  return {
    id: item.id,
    product_code: item.productCode,
    name: item.name,
    brand: item.brand,
    category: item.category,
    volume: item.volume,
    alcohol_percent: item.alcoholPercent,
    country: item.country,
    current_price: item.currentPrice,
    original_price: item.originalPrice,
    image_url: item.imageUrl,
    product_url: item.productUrl,
    source: item.source,
  };
}

export async function fetchRecommendations(
  vector: FlavorVector,
  signal?: AbortSignal,
): Promise<PreferenceResult> {
  const response = await fetch(`${API_BASE_URL}/api/liquors/recommendations`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(vector),
    signal,
  });

  if (!response.ok) {
    throw new Error(`추천 결과 조회 실패: ${response.status}`);
  }

  const data = (await response.json()) as RecommendationApiResponse;

  return {
    type_name: data.typeName,
    flavor_vector: data.flavorVector,
    recommendations: data.recommendations.map((item) => ({
      liquor: toFrontendLiquor(item.liquor),
      similarity: item.similarity,
      reason: item.reason,
    })),
  };
}
