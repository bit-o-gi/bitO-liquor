import { useMemo } from "react";
import { mockLiquors } from "../data/mockLiquors";
import type { GroupedLiquor } from "../types/liquor";
import LiquorCard from "./LiquorCard";

interface LiquorGridProps {
  searchQuery: string;
}

function groupLiquors(): GroupedLiquor[] {
  const map = new Map<string, GroupedLiquor>();

  for (const l of mockLiquors) {
    if (!map.has(l.name)) {
      map.set(l.name, {
        name: l.name,
        brand: l.brand,
        category: l.category,
        country: l.country,
        alcohol_percent: l.alcohol_percent,
        volume: l.volume,
        image_url: l.image_url,
        vendors: [],
        lowest_price: Infinity,
      });
    }
    const g = map.get(l.name)!;
    g.vendors.push({
      source: l.source,
      current_price: l.current_price,
      original_price: l.original_price,
      product_url: l.product_url,
    });
    if (l.current_price < g.lowest_price) {
      g.lowest_price = l.current_price;
    }
  }

  return Array.from(map.values());
}

export default function LiquorGrid({ searchQuery }: LiquorGridProps) {
  const grouped = useMemo(() => groupLiquors(), []);

  const filtered = useMemo(() => {
    if (!searchQuery.trim()) return grouped;

    const query = searchQuery.toLowerCase().trim();
    return grouped.filter(
      (l) =>
        l.name.toLowerCase().includes(query) ||
        l.brand.toLowerCase().includes(query) ||
        l.category.toLowerCase().includes(query) ||
        l.country.toLowerCase().includes(query)
    );
  }, [grouped, searchQuery]);

  if (filtered.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-gray-400">
        <svg className="w-16 h-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <p className="text-lg font-medium">"{searchQuery}"에 대한 검색 결과가 없습니다</p>
        <p className="text-sm mt-1">다른 검색어로 시도해보세요</p>
      </div>
    );
  }

  return (
    <>
      {searchQuery && (
        <p className="text-sm text-gray-500 mb-4">
          "{searchQuery}" 검색 결과: <span className="font-semibold text-gray-700">{filtered.length}개</span>
        </p>
      )}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {filtered.map((l) => (
          <LiquorCard key={l.name} liquor={l} />
        ))}
      </div>
    </>
  );
}