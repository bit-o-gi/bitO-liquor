import type { GroupedLiquor } from "../types/liquor";
import LiquorCard from "./LiquorCard";

interface LiquorGridProps {
  searchQuery: string;
  liquors: GroupedLiquor[];
  loading: boolean;
  error: string | null;
}

export default function LiquorGrid({ searchQuery, liquors, loading, error }: LiquorGridProps) {
  if (loading) {
    return (
      <div className="py-20 text-center text-gray-500">
        <p className="text-lg font-medium">데이터를 불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-20 text-center text-red-500">
        <p className="text-lg font-medium">{error}</p>
      </div>
    );
  }

  if (liquors.length === 0) {
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
          "{searchQuery}" 검색 결과: <span className="font-semibold text-gray-700">{liquors.length}개</span>
        </p>
      )}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {liquors.map((l) => (
          <LiquorCard key={l.name} liquor={l} />
        ))}
      </div>
    </>
  );
}
