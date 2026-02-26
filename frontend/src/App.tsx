import { Routes, Route } from 'react-router-dom'
import Header from '@/components/common/Header'
import HomePage from '@/pages/HomePage'
import CheckPage from '@/pages/CheckPage'
import ResultPage from '@/pages/ResultPage'
import LoginPage from '@/pages/LoginPage'
import AuthCallbackPage from '@/pages/AuthCallbackPage'
import MyPage from '@/pages/MyPage'
import AdminLoginPage from '@/pages/AdminLoginPage'
import AdminPage from '@/pages/AdminPage'
import ScholarshipDetailPage from '@/pages/ScholarshipDetailPage'

function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/check" element={<CheckPage />} />
          <Route path="/result" element={<ResultPage />} />
          <Route path="/scholarship/:id" element={<ScholarshipDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/auth/callback" element={<AuthCallbackPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/admin/login" element={<AdminLoginPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
