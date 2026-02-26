import { Link } from 'react-router-dom'
import { 
  CheckCircle, XCircle, ArrowLeft, 
  HelpCircle, Sparkles, AlertTriangle, ChevronRight
} from 'lucide-react'
import { useScholarshipStore } from '@/stores/scholarshipStore'

export default function ResultPage() {
  const result = useScholarshipStore((state) => state.result)

  if (!result) {
    return (
      <div className="max-w-xl mx-auto text-center py-16">
        <p className="text-gray-600 mb-4">결과 데이터가 없습니다.</p>
        <Link to="/check" className="text-primary-600 hover:underline">자격 확인하러 가기</Link>
      </div>
    )
  }

  const { results, summary } = result

  // 결과 분류
  const eligibleResults = results.filter(r => r.is_eligible === true)
  const notEligibleResults = results.filter(r => r.is_eligible === false)
  const unknownResults = results.filter(r => r.is_eligible === null)

  return (
    <div className="max-w-3xl mx-auto">
      <Link to="/check" className="inline-flex items-center gap-2 text-gray-600 hover:text-primary-600 mb-6">
        <ArrowLeft className="w-4 h-4" /> 다시 확인하기
      </Link>

      {/* 요약 카드 */}
      <div className="bg-white rounded-2xl shadow-sm border p-6 mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">자격 확인 결과</h1>
        
        <div className="grid grid-cols-3 gap-4 mb-4">
          <div className="bg-green-50 rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-green-600">{summary.eligible_count}</p>
            <p className="text-sm text-gray-600">신청 가능</p>
          </div>
          <div className="bg-red-50 rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-red-400">{notEligibleResults.length}</p>
            <p className="text-sm text-gray-600">신청 불가</p>
          </div>
          <div className="bg-yellow-50 rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-yellow-600">{unknownResults.length}</p>
            <p className="text-sm text-gray-600">확인 필요</p>
          </div>
        </div>

        {/* 안내 문구 */}
        <div className="bg-blue-50 rounded-lg p-3 text-sm text-blue-700">
          💡 실제 신청은 <strong>한국장학재단</strong> 또는 해당 기관 공식 홈페이지에서 진행해주세요.
        </div>
      </div>

      {/* 신청 가능 장학금 */}
      {eligibleResults.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-green-500" />
            신청 가능한 장학금 ({eligibleResults.length}개)
          </h2>
          <div className="space-y-4">
            {eligibleResults.map((item, index) => (
              <ScholarshipCard key={index} item={item} status="eligible" />
            ))}
          </div>
        </div>
      )}

      {/* 확인 필요 장학금 */}
      {unknownResults.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-yellow-500" />
            직접 확인이 필요한 장학금 ({unknownResults.length}개)
          </h2>
          <div className="space-y-4">
            {unknownResults.map((item, index) => (
              <ScholarshipCard key={index} item={item} status="unknown" />
            ))}
          </div>
        </div>
      )}

      {/* 신청 불가 장학금 */}
      {notEligibleResults.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <XCircle className="w-5 h-5 text-gray-400" />
            신청 불가한 장학금 ({notEligibleResults.length}개)
          </h2>
          <div className="space-y-4">
            {notEligibleResults.map((item, index) => (
              <ScholarshipCard key={index} item={item} status="not_eligible" />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

interface ScholarshipCardProps {
  item: {
    scholarship: {
      id: string
      name: string
      description?: string
      external_url?: string
      organization?: string
    }
    is_eligible: boolean | null
    eligibility_detail: {
      satisfied: string[]
      not_satisfied: string[]
      unknown?: string[]
    }
    apply_period?: string
    ai_info?: {
      is_ai_analyzed: boolean
      requires_manual_check: boolean
    }
  }
  status: 'eligible' | 'not_eligible' | 'unknown'
}

function ScholarshipCard({ item, status }: ScholarshipCardProps) {
  const statusStyles = {
    eligible: 'border-l-4 border-l-green-500',
    not_eligible: 'border-l-4 border-l-gray-300',
    unknown: 'border-l-4 border-l-yellow-500',
  }

  const statusBadge = {
    eligible: { bg: 'bg-green-100', text: 'text-green-600', label: '신청 가능' },
    not_eligible: { bg: 'bg-gray-100', text: 'text-gray-500', label: '신청 불가' },
    unknown: { bg: 'bg-yellow-100', text: 'text-yellow-600', label: '확인 필요' },
  }

  const badge = statusBadge[status]

  return (
    <div className={`bg-white rounded-xl shadow-sm border p-5 ${statusStyles[status]} hover:shadow-md transition-shadow`}>
      <div className="flex items-start justify-between mb-3">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="font-semibold text-lg text-gray-900">{item.scholarship.name}</h3>
            {item.ai_info?.is_ai_analyzed && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-purple-100 text-purple-600 text-xs rounded-full">
                <Sparkles className="w-3 h-3" /> AI 분석
              </span>
            )}
          </div>
          {item.scholarship.organization && (
            <p className="text-sm text-gray-500">{item.scholarship.organization}</p>
          )}
          {item.scholarship.description && (
            <p className="text-sm text-gray-600 mt-1 line-clamp-2">{item.scholarship.description}</p>
          )}
        </div>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${badge.bg} ${badge.text} flex-shrink-0 ml-4`}>
          {badge.label}
        </span>
      </div>

      {/* 충족 조건 */}
      {item.eligibility_detail.satisfied.length > 0 && (
        <div className="mb-2">
          {item.eligibility_detail.satisfied.map((s, i) => (
            <p key={i} className="text-sm text-green-600 flex items-center gap-1.5 py-0.5">
              <CheckCircle className="w-4 h-4 flex-shrink-0" /> {s}
            </p>
          ))}
        </div>
      )}

      {/* 미충족 조건 */}
      {item.eligibility_detail.not_satisfied.length > 0 && (
        <div className="mb-2">
          {item.eligibility_detail.not_satisfied.map((s, i) => (
            <p key={i} className="text-sm text-red-500 flex items-center gap-1.5 py-0.5">
              <XCircle className="w-4 h-4 flex-shrink-0" /> {s}
            </p>
          ))}
        </div>
      )}

      {/* 확인 필요 조건 */}
      {item.eligibility_detail.unknown && item.eligibility_detail.unknown.length > 0 && (
        <div className="mb-2">
          {item.eligibility_detail.unknown.map((s, i) => (
            <p key={i} className="text-sm text-yellow-600 flex items-center gap-1.5 py-0.5">
              <AlertTriangle className="w-4 h-4 flex-shrink-0" /> {s}
            </p>
          ))}
        </div>
      )}

      {/* 신청 기간 및 자세히 보기 */}
      <div className="flex items-center justify-between mt-4 pt-3 border-t">
        {item.apply_period ? (
          <p className="text-sm text-gray-500">신청 기간: {item.apply_period}</p>
        ) : (
          <span></span>
        )}
        <Link
          to={`/scholarship/${item.scholarship.id}`}
          className="inline-flex items-center gap-1 text-primary-600 hover:text-primary-700 text-sm font-medium"
        >
          자세히 보기
          <ChevronRight className="w-4 h-4" />
        </Link>
      </div>
    </div>
  )
}
