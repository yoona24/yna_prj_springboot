import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000'

export const api = axios.create({
  baseURL: API_URL,
})

// 요청 인터셉터 - 토큰 자동 추가 및 Content-Type 설정
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  
  // FormData가 아닐 때만 Content-Type을 application/json으로 설정
  if (!(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json'
  }
  // FormData일 때는 Content-Type을 설정하지 않음 (브라우저가 자동으로 multipart/form-data와 boundary 설정)
  
  return config
})

// 응답 인터셉터 - 401 에러 처리
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('access_token')
    }
    return Promise.reject(error)
  }
)

// ========== 타입 정의 ==========

export interface User {
  id: string
  email: string
  name: string | null
  profile_image: string | null
  provider: 'kakao' | 'naver' | 'google'
}

export interface ScholarshipCheckRequest {
  academic_status: 'enrolled' | 'expected' | 'leave'
  grade: number
  birth_year: number
  gpa: number
  income_level: number
  use_ai?: boolean
  include_public_data?: boolean
}

export interface EligibilityDetail {
  satisfied: string[]
  not_satisfied: string[]
  unknown?: string[]
}

export interface AIAnalysisInfo {
  is_ai_analyzed: boolean
  requires_manual_check: boolean
}

export interface ScholarshipInfo {
  id: string
  name: string
  type: string
  description?: string
  external_url?: string
  organization?: string
}

export interface ScholarshipMatchResult {
  scholarship: ScholarshipInfo
  is_eligible: boolean | null
  eligibility_detail: EligibilityDetail
  apply_period?: string
  ai_info?: AIAnalysisInfo
}

export interface CheckSummary {
  eligible_count: number
  total_count: number
  ai_analyzed_count?: number
  public_data_count?: number
}

export interface ScholarshipCheckResponse {
  results: ScholarshipMatchResult[]
  checked_at: string
  summary: CheckSummary
  user_conditions?: Record<string, unknown>
}

export interface ApiStatus {
  public_data_api: { enabled: boolean; description: string }
  ai_analysis: { enabled: boolean; description: string }
  oauth: { kakao: boolean; naver: boolean; google: boolean }
}

// ========== 인증 API ==========

export const authApi = {
  getLoginUrl: (provider: 'kakao' | 'naver' | 'google') => {
    return `${API_URL}/api/v1/auth/${provider}/login`
  },

  getCurrentUser: async (token?: string): Promise<User> => {
    const headers = token ? { Authorization: `Bearer ${token}` } : {}
    const response = await api.get('/api/v1/auth/me', { headers })
    return response.data
  },

  refreshToken: async (): Promise<{ access_token: string }> => {
    const response = await api.post('/api/v1/auth/refresh')
    return response.data
  },

  logout: async (): Promise<void> => {
    try {
      await api.post('/api/v1/auth/logout')
    } finally {
      localStorage.removeItem('access_token')
    }
  },
}

// ========== 장학금 API ==========

export const scholarshipApi = {
  // 장학금 목록 조회
  getScholarships: async (page = 1, perPage = 20, search?: string) => {
    const params = new URLSearchParams({ page: String(page), per_page: String(perPage) })
    if (search) params.append('search', search)
    const response = await api.get(`/api/v1/scholarships?${params}`)
    return response.data
  },

  // 장학금 자격 확인 (AI 자동 판정)
  checkEligibility: async (data: ScholarshipCheckRequest): Promise<ScholarshipCheckResponse> => {
    const response = await api.post('/api/v1/scholarships/check', {
      academic_status: data.academic_status,
      grade: data.grade,
      birth_year: data.birth_year,
      gpa: data.gpa,
      income_level: data.income_level,
    })
    return response.data
  },

  // 공공데이터 장학금 직접 조회
  getPublicScholarships: async (page = 1, perPage = 50) => {
    const response = await api.get(`/api/v1/scholarships/public?page=${page}&per_page=${perPage}`)
    return response.data
  },

  // 조회 이력
  getHistory: async () => {
    const response = await api.get('/api/v1/scholarships/history')
    return response.data
  },

  // API 상태 확인
  getApiStatus: async (): Promise<ApiStatus> => {
    const response = await api.get('/api/v1/scholarships/status')
    return response.data
  },
}

// ========== 사용자 API ==========

export const userApi = {
  getProfile: async () => {
    const response = await api.get('/api/v1/users/profile')
    return response.data
  },

  updateProfile: async (data: {
    academic_status?: string
    grade?: number
    birth_year?: number
    gpa?: number
    income_level?: number
  }) => {
    const response = await api.put('/api/v1/users/profile', data)
    return response.data
  },

  deleteAccount: async () => {
    const response = await api.delete('/api/v1/users')
    return response.data
  },
}
