import { useEffect, useMemo, useState } from "react";
import { fetchLiquors } from "./api/liquorApi";
import FeaturedPick from "./components/FeaturedPick";
import LiquorGrid from "./components/LiquorGrid";
import type { GroupedLiquor, Liquor } from "./types/liquor";
import { groupLiquors } from "./utils/groupLiquors";

export default function App() {
  const [searchQuery, setSearchQuery] = useState("");
  const [liquors, setLiquors] = useState<Liquor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    const timeoutId = setTimeout(async () => {
      try {
        setLoading(true);
        setError(null);

        const data = await fetchLiquors(searchQuery, controller.signal);
        if (!controller.signal.aborted) {
          setLiquors(data);
        }
      } catch {
        if (!controller.signal.aborted) {
          setError("데이터를 불러오지 못했습니다. API 서버 상태를 확인해주세요.");
          setLiquors([]);
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }, 250);

    return () => {
      controller.abort();
      clearTimeout(timeoutId);
    };
  }, [searchQuery]);

  const groupedLiquors: GroupedLiquor[] = useMemo(() => groupLiquors(liquors), [liquors]);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <span className="text-2xl">🥃</span>
              <h1 className="text-xl font-bold text-gray-900">Jururuk</h1>
            </div>
            <div className="relative flex-1 max-w-md">
              <input
                type="text"
                placeholder="위스키 이름, 브랜드로 검색..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-gray-100 border-0 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:bg-white transition-all"
              />
              <svg
                className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery("")}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              )}
            </div>
          </div>
        </div>
      </header>
      <main className="max-w-7xl mx-auto px-4 py-8">
        {!searchQuery && !loading && !error && <FeaturedPick liquors={groupedLiquors} />}
        <LiquorGrid
          searchQuery={searchQuery}
          liquors={groupedLiquors}
          loading={loading}
          error={error}
        />
      </main>
    </div>
  );
}
