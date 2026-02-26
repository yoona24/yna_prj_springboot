import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { Loader2 } from 'lucide-react'
import { scholarshipApi } from '@/services/api'
import { useScholarshipStore } from '@/stores/scholarshipStore'

type AcademicStatus = 'enrolled' | 'expected' | 'leave'

export default function CheckPage() {
  const navigate = useNavigate()
  const setResult = useScholarshipStore((state) => state.setResult)
  const [formData, setFormData] = useState({
    academicStatus: 'enrolled' as AcademicStatus,
    grade: 1,
    birthYear: 2003,
    gpa: 3.0,
    incomeLevel: 5,
  })

  const checkMutation = useMutation({
    mutationFn: scholarshipApi.checkEligibility,
    onSuccess: (data) => {
      setResult(data)
      navigate('/result')
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    checkMutation.mutate({
      academic_status: formData.academicStatus,
      grade: formData.grade,
      birth_year: formData.birthYear,
      gpa: formData.gpa,
      income_level: formData.incomeLevel,
    })
  }

  return (
    <div className="max-w-xl mx-auto">
      <div className="bg-white rounded-2xl shadow-sm border p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">정부·공공 지원 정책 확인</h1>
        <p className="text-gray-600 mb-8">아래 조건을 입력하시면 장학금 수혜 가능 여부를 확인해 드립니다.</p>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">학적상태</label>
            <select
              value={formData.academicStatus}
              onChange={(e) => setFormData({ ...formData, academicStatus: e.target.value as AcademicStatus })}
              className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              <option value="enrolled">재학중</option>
              <option value="expected">입학예정</option>
              <option value="leave">휴학</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">학년</label>
            <select
              value={formData.grade}
              onChange={(e) => setFormData({ ...formData, grade: Number(e.target.value) })}
              className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              {[1, 2, 3, 4].map((g) => (
                <option key={g} value={g}>{g}학년</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">출생년도</label>
            <select
              value={formData.birthYear}
              onChange={(e) => setFormData({ ...formData, birthYear: Number(e.target.value) })}
              className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              {Array.from({ length: 15 }, (_, i) => 2010 - i).map((y) => (
                <option key={y} value={y}>{y}년생</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">학점 (GPA)</label>
            <input
              type="number"
              min="0"
              max="4.5"
              step="0.1"
              value={formData.gpa}
              onChange={(e) => setFormData({ ...formData, gpa: Number(e.target.value) })}
              className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary-500"
            />
            <p className="text-sm text-gray-500 mt-1">0.0 ~ 4.5 사이 입력</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">소득분위</label>
            <select
              value={formData.incomeLevel}
              onChange={(e) => setFormData({ ...formData, incomeLevel: Number(e.target.value) })}
              className="w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              {Array.from({ length: 10 }, (_, i) => i + 1).map((l) => (
                <option key={l} value={l}>{l}분위</option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            disabled={checkMutation.isPending}
            className="w-full py-4 bg-primary-600 text-white font-semibold rounded-lg hover:bg-primary-700 disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {checkMutation.isPending ? (
              <><Loader2 className="w-5 h-5 animate-spin" />확인 중...</>
            ) : '확인하기'}
          </button>
        </form>

        {checkMutation.isError && (
          <div className="mt-4 p-4 bg-red-50 text-red-600 rounded-lg">
            오류가 발생했습니다. 다시 시도해 주세요.
          </div>
        )}
      </div>
    </div>
  )
}
