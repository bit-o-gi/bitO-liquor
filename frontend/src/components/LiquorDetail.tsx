import type { GroupedLiquor } from "../types/liquor";

function formatPrice(price: number) {
    return price.toLocaleString("ko-KR") + "원";
}

interface LiquorDetailProps {
    liquor: GroupedLiquor;
    onBack: () => void;
}

export default function LiquorDetail({ liquor, onBack }: LiquorDetailProps) {
    // 판매처를 가격 오름차순으로 정렬
    const sortedVendors = [...liquor.vendors].sort(
        (a, b) => a.current_price - b.current_price
    );

    return (
        <div className="max-w-5xl mx-auto bg-white rounded-3xl shadow-sm overflow-hidden border border-gray-100">
            {/* 뒤로 가기 버튼 */}
            <div className="p-4 border-b border-gray-100">
                <button
                    onClick={onBack}
                    className="flex items-center text-gray-500 hover:text-gray-900 transition-colors"
                >
                    <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    목록으로 돌아가기
                </button>
            </div>

            <div className="flex flex-col md:flex-row">
                {/* 왼쪽: 상품 이미지 */}
                <div className="md:w-1/2 bg-gray-50 p-8 flex items-center justify-center">
                    <div className="aspect-[3/4] w-full max-w-sm rounded-2xl overflow-hidden shadow-lg bg-white">
                        <img
                            src={liquor.image_url}
                            alt={liquor.name}
                            className="w-full h-full object-cover"
                        />
                    </div>
                </div>

                {/* 오른쪽: 상품 정보 및 판매처 */}
                <div className="md:w-1/2 p-8 flex flex-col">
                    <div className="mb-6">
                        <p className="text-sm font-semibold text-amber-600 mb-2">
                            {liquor.brand} · {liquor.category}
                        </p>
                        <h2 className="text-3xl font-black text-gray-900 mb-4">{liquor.name}</h2>
                        <div className="flex flex-wrap gap-2 text-sm text-gray-600 bg-gray-50 inline-flex p-3 rounded-xl">
                            <span>🌍 {liquor.country}</span>
                            <span className="text-gray-300">|</span>
                            <span>💧 {liquor.alcohol_percent}%</span>
                            <span className="text-gray-300">|</span>
                            <span>📏 {liquor.volume}ml</span>
                        </div>
                    </div>

                    <div className="flex-1">
                        <div className="flex items-end gap-3 mb-4">
                            <h3 className="text-lg font-bold text-gray-900">판매처 별 가격 비교</h3>
                            <span className="text-sm text-gray-500 mb-0.5">총 {sortedVendors.length}곳</span>
                        </div>

                        <ul className="space-y-3">
                            {sortedVendors.map((v, index) => (
                                <li key={v.source}>
                                    <a
                                        href={v.product_url}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className={`flex items-center justify-between p-4 rounded-2xl border transition-all hover:shadow-md ${
                                            index === 0
                                                ? "border-amber-400 bg-amber-50/30"
                                                : "border-gray-100 bg-white hover:border-amber-200"
                                        }`}
                                    >
                                        <div className="flex items-center gap-3">
                                            {index === 0 && (
                                                <span className="bg-amber-500 text-white text-xs font-bold px-2 py-1 rounded-md">
                          최저가
                        </span>
                                            )}
                                            <span className="font-semibold text-gray-800">{v.source}</span>
                                        </div>
                                        <div className="flex flex-col items-end">
                                            {v.original_price > v.current_price && (
                                                <span className="text-xs text-gray-400 line-through mb-0.5">
                          {formatPrice(v.original_price)}
                        </span>
                                            )}
                                            <span className={`text-lg font-bold ${index === 0 ? "text-amber-600" : "text-gray-900"}`}>
                        {formatPrice(v.current_price)}
                      </span>
                                        </div>
                                    </a>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
}