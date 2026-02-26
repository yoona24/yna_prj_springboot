import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { 
  ArrowLeft, Calendar, Building2, GraduationCap,
  Wallet, FileText, MapPin, Users, Clock, CheckCircle, 
  AlertTriangle, Info, Loader2, BookOpen, Award, Briefcase
} from 'lucide-react'
import { api } from '@/services/api'

interface ScholarshipDetail {
  id: string
  name: string
  organization: string
  organization_type: string | null
  product_type: string | null
  financial_aid_type: string | null
  type: string
  university_category: string | null
  grade_semester: string | null
  major_category: string | null
  grade_criteria: string | null
  income_criteria: string | null
  support_details: string | null
  special_qualification: string | null
  residency_detail: string | null
  selection_method: string | null
  selection_count: string | null
  eligibility_restriction: string | null
  recommendation_required: string | null
  required_documents: string | null
  website_url: string | null
  apply_start: string | null
  apply_end: string | null
}

export default function ScholarshipDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [scholarship, setScholarship] = useState<ScholarshipDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchScholarship = async () => {
      if (!id) return
      
      setLoading(true)
      try {
        const response = await api.get(`/api/v1/scholarships/${id}`)
        setScholarship(response.data)
      } catch (err: any) {
        setError(err.response?.data?.detail || '장학금 정보를 불러올 수 없습니다.')
      } finally {
        setLoading(false)
      }
    }

    fetchScholarship()
  }, [id])

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto flex items-center justify-center py-20">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  if (error || !scholarship) {
    return (
      <div className="max-w-3xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-xl p-8 text-center">
          <AlertTriangle className="w-12 h-12 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-red-800 mb-2">정보를 불러올 수 없습니다</h2>
          <p className="text-red-600 mb-6">{error}</p>
          <button
            onClick={() => navigate(-1)}
            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
          >
            뒤로 가기
          </button>
        </div>
      </div>
    )
  }

  const today = new Date()
  const startDate = scholarship.apply_start ? new Date(scholarship.apply_start) : null
  const endDate = scholarship.apply_end ? new Date(scholarship.apply_end) : null
  
  let periodStatus: 'upcoming' | 'ongoing' | 'ended' | 'unknown' = 'unknown'
  if (startDate && endDate) {
    if (today < startDate) periodStatus = 'upcoming'
    else if (today <= endDate) periodStatus = 'ongoing'
    else periodStatus = 'ended'
  }

  const periodStatusConfig = {
    upcoming: { label: '신청 예정', color: 'bg-blue-100 text-blue-700', icon: Clock },
    ongoing: { label: '신청 가능', color: 'bg-green-100 text-green-700', icon: CheckCircle },
    ended: { label: '신청 마감', color: 'bg-gray-100 text-gray-500', icon: AlertTriangle },
    unknown: { label: '기간 미정', color: 'bg-yellow-100 text-yellow-700', icon: Info },
  }

  const statusConfig = periodStatusConfig[periodStatus]
  const StatusIcon = statusConfig.icon

  return (
    <div className="max-w-3xl mx-auto">
      <button
        onClick={() => navigate(-1)}
        className="inline-flex items-center gap-2 text-gray-600 hover:text-blue-600 mb-6 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        뒤로 가기
      </button>

      {/* 헤더 */}
      <div className="bg-gradient-to-br from-blue-600 to-blue-700 rounded-2xl p-8 text-white mb-6">
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center">
              <Award className="w-6 h-6" />
            </div>
            <div>
              <p className="text-blue-100 text-sm">{scholarship.organization}</p>
              <h1 className="text-2xl font-bold">{scholarship.name}</h1>
            </div>
          </div>
          <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium ${statusConfig.color}`}>
            <StatusIcon className="w-4 h-4" />
            {statusConfig.label}
          </span>
        </div>

        {(scholarship.apply_start || scholarship.apply_end) && (
          <div className="flex items-center gap-2 text-blue-100">
            <Calendar className="w-4 h-4" />
            <span>
              신청 기간: {scholarship.apply_start || '?'} ~ {scholarship.apply_end || '?'}
            </span>
          </div>
        )}
        
        {scholarship.product_type && (
          <div className="mt-2 inline-block px-3 py-1 bg-white/20 rounded-full text-sm">
            {scholarship.product_type}
          </div>
        )}
      </div>

      {/* 상세 정보 */}
      <div className="space-y-4">
        {scholarship.support_details && (
          <InfoCard
            icon={<Wallet className="w-5 h-5" />}
            title="지원내역"
            content={scholarship.support_details}
            highlight
          />
        )}

        {scholarship.grade_criteria && (
          <InfoCard
            icon={<BookOpen className="w-5 h-5" />}
            title="성적기준"
            content={scholarship.grade_criteria}
          />
        )}

        {scholarship.income_criteria && (
          <InfoCard
            icon={<Wallet className="w-5 h-5" />}
            title="소득기준"
            content={scholarship.income_criteria}
          />
        )}

        {scholarship.university_category && (
          <InfoCard
            icon={<GraduationCap className="w-5 h-5" />}
            title="대학구분"
            content={scholarship.university_category}
          />
        )}

        {scholarship.grade_semester && (
          <InfoCard
            icon={<GraduationCap className="w-5 h-5" />}
            title="학년구분"
            content={scholarship.grade_semester}
          />
        )}

        {scholarship.major_category && (
          <InfoCard
            icon={<Briefcase className="w-5 h-5" />}
            title="학과구분"
            content={scholarship.major_category}
          />
        )}

        {scholarship.special_qualification && (
          <InfoCard
            icon={<CheckCircle className="w-5 h-5" />}
            title="특정자격"
            content={scholarship.special_qualification}
          />
        )}

        {scholarship.residency_detail && (
          <InfoCard
            icon={<MapPin className="w-5 h-5" />}
            title="지역거주여부"
            content={scholarship.residency_detail}
          />
        )}

        {scholarship.selection_method && (
          <InfoCard
            icon={<Users className="w-5 h-5" />}
            title="선발방법"
            content={scholarship.selection_method}
          />
        )}

        {scholarship.selection_count && (
          <InfoCard
            icon={<Users className="w-5 h-5" />}
            title="선발인원"
            content={scholarship.selection_count}
          />
        )}

        {scholarship.eligibility_restriction && (
          <InfoCard
            icon={<AlertTriangle className="w-5 h-5" />}
            title="자격제한"
            content={scholarship.eligibility_restriction}
            warning
          />
        )}

        {scholarship.recommendation_required && (
          <InfoCard
            icon={<FileText className="w-5 h-5" />}
            title="추천필요여부"
            content={scholarship.recommendation_required}
          />
        )}

        {scholarship.required_documents && (
          <InfoCard
            icon={<FileText className="w-5 h-5" />}
            title="제출서류"
            content={scholarship.required_documents}
          />
        )}

        {/* 운영기관 정보 */}
        <div className="bg-gray-50 rounded-xl p-5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 bg-gray-200 rounded-lg flex items-center justify-center">
              <Building2 className="w-5 h-5 text-gray-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">운영기관</p>
              <p className="font-semibold text-gray-900">{scholarship.organization}</p>
              {scholarship.organization_type && (
                <p className="text-xs text-gray-500">{scholarship.organization_type}</p>
              )}
            </div>
          </div>
          
          {scholarship.website_url && (
            <a 
              href={scholarship.website_url}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-blue-600 hover:underline text-sm"
            >
              홈페이지 바로가기 →
            </a>
          )}
        </div>
      </div>

      <div className="mt-8">
        <button
          onClick={() => navigate(-1)}
          className="w-full flex items-center justify-center gap-2 px-6 py-3 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors font-semibold"
        >
          <ArrowLeft className="w-5 h-5" />
          뒤로 가기
        </button>
      </div>
    </div>
  )
}

function InfoCard({ 
  icon, 
  title, 
  content, 
  highlight = false,
  warning = false 
}: { 
  icon: React.ReactNode
  title: string
  content: string
  highlight?: boolean
  warning?: boolean
}) {
  let bgColor = 'bg-white border'
  let iconBg = 'bg-gray-100 text-gray-600'
  
  if (highlight) {
    bgColor = 'bg-green-50 border-green-200'
    iconBg = 'bg-green-100 text-green-600'
  } else if (warning) {
    bgColor = 'bg-amber-50 border-amber-200'
    iconBg = 'bg-amber-100 text-amber-600'
  }

  return (
    <div className={`${bgColor} rounded-xl p-5`}>
      <div className="flex items-start gap-4">
        <div className={`w-10 h-10 ${iconBg} rounded-lg flex items-center justify-center flex-shrink-0`}>
          {icon}
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="font-semibold text-gray-900 mb-2">{title}</h3>
          <p className="text-gray-700 whitespace-pre-wrap break-words leading-relaxed">
            {content}
          </p>
        </div>
      </div>
    </div>
  )
}
