import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  LayoutDashboard, Upload, List, LogOut, Loader2, 
  Award, CheckCircle, XCircle, Star, RefreshCw
} from 'lucide-react'
import { api } from '@/services/api'

interface DashboardStats {
  total_scholarships: number
  active_scholarships: number
  inactive_scholarships: number
  featured_scholarships: number
}

interface Scholarship {
  id: string
  name: string
  organization: string
  is_active: boolean
  is_featured: boolean
}

export default function AdminPage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<'dashboard' | 'upload' | 'list'>('dashboard')
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [scholarships, setScholarships] = useState<Scholarship[]>([])
  const [uploadMode, setUploadMode] = useState<'replace' | 'deactivate' | 'append'>('append')
  const [uploadFile, setUploadFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)
  const [message, setMessage] = useState('')
  const [updatingIds, setUpdatingIds] = useState<Set<string>>(new Set())

  useEffect(() => {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      navigate('/admin/login')
      return
    }
    loadDashboard()
  }, [navigate])

  const getAuthHeaders = () => {
    const token = localStorage.getItem('admin_token')
    return { headers: { Authorization: `Bearer ${token}` } }
  }

  const loadDashboard = async () => {
    setLoading(true)
    try {
      const response = await api.get('/api/v1/admin/dashboard', getAuthHeaders())
      setStats(response.data)
    } catch (err: any) {
      console.error('Dashboard load error:', err)
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.removeItem('admin_token')
        navigate('/admin/login')
      }
    } finally {
      setLoading(false)
    }
  }

  const loadScholarships = async () => {
    setLoading(true)
    try {
      const response = await api.get('/api/v1/admin/scholarships?perPage=100', getAuthHeaders())
      const data = response.data.scholarships || response.data.items || []
      setScholarships(data)
    } catch (err) {
      console.error('Scholarships load error:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleToggleActive = async (id: string, currentValue: boolean) => {
    setUpdatingIds(prev => new Set(prev).add(id))
    try {
      await api.put(`/api/v1/admin/scholarships/${id}`, 
        { is_active: !currentValue },
        getAuthHeaders()
      )
      // 로컬 상태 업데이트
      setScholarships(prev => prev.map(s => 
        s.id === id ? { ...s, is_active: !currentValue } : s
      ))
      // 대시보드 통계도 갱신
      loadDashboard()
    } catch (err: any) {
      console.error('Toggle active error:', err)
      alert('상태 변경에 실패했습니다.')
    } finally {
      setUpdatingIds(prev => {
        const next = new Set(prev)
        next.delete(id)
        return next
      })
    }
  }

  const handleToggleFeatured = async (id: string, currentValue: boolean) => {
    setUpdatingIds(prev => new Set(prev).add(id))
    try {
      await api.put(`/api/v1/admin/scholarships/${id}`, 
        { is_featured: !currentValue },
        getAuthHeaders()
      )
      setScholarships(prev => prev.map(s => 
        s.id === id ? { ...s, is_featured: !currentValue } : s
      ))
      loadDashboard()
    } catch (err: any) {
      console.error('Toggle featured error:', err)
      alert('추천 상태 변경에 실패했습니다.')
    } finally {
      setUpdatingIds(prev => {
        const next = new Set(prev)
        next.delete(id)
        return next
      })
    }
  }

  const handleUpload = async () => {
    if (!uploadFile) {
      setMessage('❌ 파일을 선택해주세요.')
      return
    }

    setUploading(true)
    setMessage('')

    const formData = new FormData()
    formData.append('file', uploadFile)
    formData.append('mode', uploadMode)

    try {
      const token = localStorage.getItem('admin_token')
      const response = await api.post('/api/v1/admin/upload-csv', formData, {
        headers: { Authorization: `Bearer ${token}` }
      })
      
      console.log('Upload response:', response.data)
      
      const data = response.data
      const successCount = data.success ?? data.totalRows ?? 0
      const failedCount = data.failed ?? 0
      const totalRows = data.totalRows ?? (successCount + failedCount)
      
      setMessage(`✅ 업로드 완료: 총 ${totalRows}건 중 성공 ${successCount}건, 실패 ${failedCount}건`)
      setUploadFile(null)
      
      const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
      if (fileInput) fileInput.value = ''
      
      loadDashboard()
    } catch (err: any) {
      console.error('Upload error:', err)
      const errorMsg = err.response?.data?.detail || err.response?.data?.message || err.message
      setMessage(`❌ 업로드 실패: ${errorMsg}`)
    } finally {
      setUploading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('admin_token')
    navigate('/admin/login')
  }

  const handleTabChange = (tab: 'dashboard' | 'upload' | 'list') => {
    setActiveTab(tab)
    setMessage('')
    if (tab === 'list') loadScholarships()
    else if (tab === 'dashboard') loadDashboard()
  }

  if (loading && !stats) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
        <button onClick={handleLogout} className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-red-600">
          <LogOut className="w-5 h-5" />
          로그아웃
        </button>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        <button
          onClick={() => handleTabChange('dashboard')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium ${
            activeTab === 'dashboard' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <LayoutDashboard className="w-5 h-5" />
          대시보드
        </button>
        <button
          onClick={() => handleTabChange('upload')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium ${
            activeTab === 'upload' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <Upload className="w-5 h-5" />
          CSV 업로드
        </button>
        <button
          onClick={() => handleTabChange('list')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium ${
            activeTab === 'list' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <List className="w-5 h-5" />
          장학금 목록
        </button>
      </div>

      {/* Dashboard Tab */}
      {activeTab === 'dashboard' && stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard icon={<Award className="w-6 h-6" />} label="전체 장학금" value={stats.total_scholarships} color="blue" />
          <StatCard icon={<CheckCircle className="w-6 h-6" />} label="활성 장학금" value={stats.active_scholarships} color="green" />
          <StatCard icon={<XCircle className="w-6 h-6" />} label="비활성 장학금" value={stats.inactive_scholarships} color="gray" />
          <StatCard icon={<Star className="w-6 h-6" />} label="추천 장학금" value={stats.featured_scholarships} color="yellow" />
        </div>
      )}

      {/* Upload Tab */}
      {activeTab === 'upload' && (
        <div className="bg-white rounded-xl shadow-sm border p-6">
          <h2 className="text-lg font-semibold mb-4">CSV 파일 업로드</h2>
          
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">업로드 모드</label>
              <select
                value={uploadMode}
                onChange={(e) => setUploadMode(e.target.value as any)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="append">추가 (기존 데이터 유지)</option>
                <option value="deactivate">비활성화 후 추가 (기존 데이터 비활성화)</option>
                <option value="replace">교체 (기존 데이터 삭제)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">CSV 파일</label>
              <input
                type="file"
                accept=".csv"
                onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg"
              />
              <p className="mt-1 text-sm text-gray-500">
                한국장학재단 CSV 파일 (CP949, EUC-KR, UTF-8 인코딩 지원)
              </p>
            </div>

            {message && (
              <div className={`p-4 rounded-lg ${message.startsWith('✅') ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
                {message}
              </div>
            )}

            <button
              onClick={handleUpload}
              disabled={uploading || !uploadFile}
              className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {uploading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  업로드 중...
                </>
              ) : (
                <>
                  <Upload className="w-5 h-5" />
                  업로드
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* List Tab */}
      {activeTab === 'list' && (
        <div className="bg-white rounded-xl shadow-sm border">
          <div className="p-4 border-b flex justify-between items-center">
            <h2 className="text-lg font-semibold">장학금 목록 ({scholarships.length}개)</h2>
            <button onClick={loadScholarships} className="flex items-center gap-2 text-gray-600 hover:text-blue-600">
              <RefreshCw className="w-4 h-4" />
              새로고침
            </button>
          </div>

          {/* 테이블 헤더 */}
          <div className="grid grid-cols-12 gap-4 px-4 py-3 bg-gray-50 border-b text-sm font-medium text-gray-600">
            <div className="col-span-5">장학금명</div>
            <div className="col-span-3">운영기관</div>
            <div className="col-span-2 text-center">활성화</div>
            <div className="col-span-2 text-center">추천</div>
          </div>
          
          {loading ? (
            <div className="flex justify-center py-12">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600" />
            </div>
          ) : scholarships.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              등록된 장학금이 없습니다.
            </div>
          ) : (
            <div className="divide-y max-h-[600px] overflow-y-auto">
              {scholarships.map((s) => {
                const isUpdating = updatingIds.has(s.id)
                return (
                  <div key={s.id} className="grid grid-cols-12 gap-4 px-4 py-3 items-center hover:bg-gray-50">
                    <div className="col-span-5">
                      <h3 className="font-medium text-gray-900 truncate">{s.name}</h3>
                    </div>
                    <div className="col-span-3">
                      <p className="text-sm text-gray-500 truncate">{s.organization}</p>
                    </div>
                    <div className="col-span-2 flex justify-center">
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={s.is_active}
                          onChange={() => handleToggleActive(s.id, s.is_active)}
                          disabled={isUpdating}
                          className="sr-only peer"
                        />
                        <div className={`w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-500 ${isUpdating ? 'opacity-50' : ''}`}></div>
                      </label>
                    </div>
                    <div className="col-span-2 flex justify-center">
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={s.is_featured}
                          onChange={() => handleToggleFeatured(s.id, s.is_featured)}
                          disabled={isUpdating}
                          className="sr-only peer"
                        />
                        <div className={`w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-yellow-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-yellow-500 ${isUpdating ? 'opacity-50' : ''}`}></div>
                      </label>
                    </div>
                  </div>
                )
              })}
            </div>
          )}

          {/* 안내 메시지 */}
          <div className="p-4 bg-blue-50 border-t text-sm text-blue-700">
            <strong>안내:</strong> 비활성화된 장학금은 사용자 매칭 결과에 표시되지 않습니다.
          </div>
        </div>
      )}
    </div>
  )
}

function StatCard({ icon, label, value, color }: { icon: React.ReactNode; label: string; value: number; color: string }) {
  const colors: Record<string, string> = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    gray: 'bg-gray-50 text-gray-600',
    yellow: 'bg-yellow-50 text-yellow-600'
  }
  return (
    <div className="bg-white rounded-xl shadow-sm border p-6">
      <div className={`w-12 h-12 rounded-lg flex items-center justify-center mb-4 ${colors[color]}`}>{icon}</div>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-3xl font-bold text-gray-900">{value}</p>
    </div>
  )
}
