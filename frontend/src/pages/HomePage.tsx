import { Link } from 'react-router-dom'
import { ArrowRight, CheckCircle, Clock, Shield, User } from 'lucide-react'
import { useAuthStore } from '@/stores/authStore'

export default function HomePage() {
  const { isAuthenticated, user } = useAuthStore()

  return (
    <div className="max-w-4xl mx-auto">
      {/* 히어로 섹션 */}
      <section className="text-center py-16">
        {isAuthenticated && user && (
          <div className="mb-6">
            <span className="inline-flex items-center gap-2 px-4 py-2 bg-primary-100 text-primary-700 rounded-full text-sm font-medium">
              <User className="w-4 h-4" />
              {user.name || '사용자'}님, 환영합니다!
            </span>
          </div>
        )}
        
        <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-6">
          나에게 맞는 <span className="text-primary-600">장학금</span>을<br />빠르게 확인하세요
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          학적상태, 성적, 소득분위만 입력하면<br />
          국가장학금, 학자금대출 등 수혜 가능 여부를 바로 확인할 수 있습니다.
        </p>
        
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <Link 
            to="/check" 
            className="inline-flex items-center gap-2 px-8 py-4 bg-primary-600 text-white text-lg font-semibold rounded-xl hover:bg-primary-700 transition-colors"
          >
            지금 바로 확인하기 <ArrowRight className="w-5 h-5" />
          </Link>
          
          {!isAuthenticated && (
            <Link 
              to="/login" 
              className="inline-flex items-center gap-2 px-8 py-4 bg-white text-primary-600 text-lg font-semibold rounded-xl border-2 border-primary-600 hover:bg-primary-50 transition-colors"
            >
              로그인하고 저장하기
            </Link>
          )}
        </div>
      </section>

      {/* 특징 섹션 */}
      <section className="grid md:grid-cols-3 gap-8 py-12">
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <div className="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center mb-4">
            <Clock className="w-6 h-6 text-primary-600" />
          </div>
          <h3 className="text-lg font-semibold mb-2">빠른 확인</h3>
          <p className="text-gray-600">1분 안에 모든 장학금 자격을 한 번에 확인할 수 있습니다.</p>
        </div>
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <div className="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center mb-4">
            <CheckCircle className="w-6 h-6 text-green-600" />
          </div>
          <h3 className="text-lg font-semibold mb-2">정확한 판정</h3>
          <p className="text-gray-600">한국장학재단 기준에 맞춰 정확한 자격 여부를 안내합니다.</p>
        </div>
        <div className="bg-white p-6 rounded-2xl shadow-sm border">
          <div className="w-12 h-12 bg-yellow-100 rounded-xl flex items-center justify-center mb-4">
            <Shield className="w-6 h-6 text-yellow-600" />
          </div>
          <h3 className="text-lg font-semibold mb-2">안전한 이용</h3>
          <p className="text-gray-600">입력한 정보는 저장되지 않으며, 안전하게 처리됩니다.</p>
        </div>
      </section>

      {/* 지원 장학금 안내 */}
      <section className="py-12">
        <h2 className="text-2xl font-bold text-center text-gray-900 mb-8">지원하는 장학금 종류</h2>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            { name: '국가장학금', desc: '소득연계 국가장학금', color: 'bg-blue-500' },
            { name: '국가근로장학금', desc: '교내/교외 근로장학금', color: 'bg-green-500' },
            { name: '학자금대출', desc: '등록금 학자금 대출', color: 'bg-purple-500' },
            { name: '생활비대출', desc: '생활비 학자금 대출', color: 'bg-orange-500' },
          ].map((item, index) => (
            <div key={index} className="bg-white rounded-xl border p-4 text-center">
              <div className={`w-3 h-3 ${item.color} rounded-full mx-auto mb-3`} />
              <h3 className="font-semibold text-gray-900">{item.name}</h3>
              <p className="text-sm text-gray-500 mt-1">{item.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
