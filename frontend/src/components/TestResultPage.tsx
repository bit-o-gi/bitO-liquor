import type { PreferenceResult } from "../types/preference";
import FlavorRadarChart from "./FlavorRadarChart";

interface TestResultPageProps {
  result: PreferenceResult;
  onRetry: () => void;
  onBackHome: () => void;
}

function formatPrice(price: number) {
  return price.toLocaleString("ko-KR") + "원";
}

export default function TestResultPage({ result, onRetry, onBackHome }: TestResultPageProps) {
  return (
    <section className="space-y-8">
      <div className="rounded-3xl p-6 sm:p-8 bg-gradient-to-br from-amber-50 to-orange-100 border border-amber-100 shadow-lg">
        <p className="text-xs uppercase tracking-wide font-bold text-amber-700">Taste Type</p>
        <h2 className="text-2xl sm:text-3xl font-black text-gray-900 mt-1">{result.type_name}</h2>
        <p className="text-sm text-gray-600 mt-2">당신의 상위 향미 축을 기준으로 추천 결과를 만들었어요.</p>

        <div className="mt-6 grid md:grid-cols-[360px_1fr] gap-6 items-center">
          <FlavorRadarChart vector={result.flavor_vector} />
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {Object.entries(result.flavor_vector).map(([key, value]) => (
              <div key={key} className="rounded-xl bg-white px-3 py-2 border border-amber-100">
                <p className="text-xs uppercase text-gray-400">{key}</p>
                <p className="text-lg font-extrabold text-gray-900">{value.toFixed(1)}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={onRetry}
            className="px-4 py-2.5 rounded-xl bg-gray-900 text-white font-semibold hover:bg-gray-800 transition-colors"
          >
            테스트 다시 하기
          </button>
          <button
            type="button"
            onClick={onBackHome}
            className="px-4 py-2.5 rounded-xl border border-gray-200 bg-white text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          >
            홈으로 가기
          </button>
        </div>
      </div>

      <div>
        <h3 className="text-xl font-black text-gray-900">당신에게 맞는 위스키 Top 5</h3>
        <p className="text-sm text-gray-500 mt-1">유사도와 향미 조합을 기준으로 추천했어요.</p>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {result.recommendations.map((item) => (
            <article key={`${item.liquor.product_code}-${item.liquor.source}`} className="rounded-2xl overflow-hidden bg-white shadow-md border border-gray-100">
              <div className="aspect-[4/3] bg-gray-100">
                <img src={item.liquor.image_url} alt={item.liquor.name} className="w-full h-full object-cover" />
              </div>
              <div className="p-4">
                <p className="text-xs text-gray-500">{item.liquor.brand} · {item.liquor.category}</p>
                <h4 className="mt-1 text-lg font-extrabold text-gray-900 leading-tight">{item.liquor.name}</h4>
                <p className="mt-2 text-sm text-gray-600 min-h-[44px]">{item.reason}</p>

                <div className="mt-3 flex items-center justify-between">
                  <div>
                    <p className="text-xs text-gray-400">최저가</p>
                    <p className="text-xl font-black text-amber-600">{formatPrice(item.liquor.current_price)}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-xs text-gray-400">유사도</p>
                    <p className="text-base font-bold text-gray-900">{Math.round(item.similarity * 100)}%</p>
                  </div>
                </div>

                <a
                  href={item.liquor.product_url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="mt-4 inline-flex items-center justify-center w-full rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold py-2.5 transition-colors"
                >
                  {item.liquor.source}에서 보기
                </a>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
