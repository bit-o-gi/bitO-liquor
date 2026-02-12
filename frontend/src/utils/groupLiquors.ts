import type { GroupedLiquor, Liquor } from "../types/liquor";

export function groupLiquors(liquors: Liquor[]): GroupedLiquor[] {
  const map = new Map<string, GroupedLiquor>();

  for (const l of liquors) {
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
