import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { User, LogOut, Loader2 } from 'lucide-react'
import { api } from '@/services/api'

export default function MyPage() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [user, setUser] = useState<any>(null)

  useEffect(() => {
    const token = localStorage.getItem('user_token')
    if (!token) {
      navigate('/login')
      return
    }
    loadUser()
  }, [navigate])

  const loadUser = async () => {
    try {
      const response = await api.get('/api/v1/auth/me', {
        headers: { Authorization: `Bearer ${localStorage.getItem('user_token')}` }
      })
      setUser(response.data)
    } catch (err) {
      console.error('User load error:', err)
      localStorage.removeItem('user_token')
      navigate('/login')
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('user_token')
    navigate('/')
  }

  if (loading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-primary-600" />
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-2xl shadow-lg p-8">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold text-gray-900">마이페이지</h1>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 text-gray-500 hover:text-red-600"
          >
            <LogOut className="w-5 h-5" />
            로그아웃
          </button>
        </div>

        <div className="flex items-center gap-4 p-6 bg-gray-50 rounded-xl">
          <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
            <User className="w-8 h-8 text-primary-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              {user?.name || '사용자'}
            </h2>
            <p className="text-gray-500">{user?.email || ''}</p>
          </div>
        </div>
      </div>
    </div>
  )
}
