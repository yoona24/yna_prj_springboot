import { Link } from 'react-router-dom'
import { GraduationCap, User, LogOut, ChevronDown, Settings } from 'lucide-react'
import { useState, useRef, useEffect } from 'react'
import { useAuthStore } from '@/stores/authStore'

export default function Header() {
  const { user, isAuthenticated, logout } = useAuthStore()
  const [isDropdownOpen, setIsDropdownOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // 드롭다운 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = () => {
    logout()
    setIsDropdownOpen(false)
  }

  return (
    <header className="bg-white shadow-sm border-b">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <Link to="/" className="flex items-center gap-2">
            <GraduationCap className="w-8 h-8 text-primary-600" />
            <span className="text-xl font-bold text-gray-900">장학금 매칭</span>
          </Link>

          {/* 네비게이션 */}
          <nav className="flex items-center gap-6">
            <Link 
              to="/check" 
              className="text-gray-600 hover:text-primary-600 font-medium"
            >
              자격 확인
            </Link>
            
            <Link 
              to="/admin" 
              className="text-gray-600 hover:text-primary-600 font-medium flex items-center gap-1"
            >
              <Settings className="w-4 h-4" />
              관리자
            </Link>

            {isAuthenticated && user ? (
              // 로그인 상태
              <div className="relative" ref={dropdownRef}>
                <button
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  {/* 프로필 이미지 또는 기본 아이콘 */}
                  <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center overflow-hidden">
                    {user.profile_image ? (
                      <img
                        src={user.profile_image}
                        alt={user.name || '프로필'}
                        className="w-8 h-8 object-cover"
                      />
                    ) : (
                      <User className="w-4 h-4 text-primary-600" />
                    )}
                  </div>
                  <span className="text-gray-700 font-medium hidden sm:block">
                    {user.name || '사용자'}
                  </span>
                  <ChevronDown className={`w-4 h-4 text-gray-400 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} />
                </button>

                {/* 드롭다운 메뉴 */}
                {isDropdownOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border py-2 z-50">
                    <div className="px-4 py-2 border-b">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {user.name || '사용자'}
                      </p>
                      <p className="text-xs text-gray-500 truncate">{user.email}</p>
                    </div>
                    <Link
                      to="/mypage"
                      onClick={() => setIsDropdownOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-50"
                    >
                      <User className="w-4 h-4" />
                      <span>마이페이지</span>
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50"
                    >
                      <LogOut className="w-4 h-4" />
                      <span>로그아웃</span>
                    </button>
                  </div>
                )}
              </div>
            ) : (
              // 비로그인 상태
              <Link
                to="/login"
                className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                <User className="w-4 h-4" />
                <span>로그인</span>
              </Link>
            )}
          </nav>
        </div>
      </div>
    </header>
  )
}
