import { create } from 'zustand'
import { ScholarshipCheckResponse, ScholarshipCheckRequest } from '@/services/api'

interface ScholarshipState {
  // 결과 저장
  result: ScholarshipCheckResponse | null
  setResult: (result: ScholarshipCheckResponse) => void
  clearResult: () => void
  
  // 로딩 상태
  isLoading: boolean
  setLoading: (loading: boolean) => void
  
  // 에러 상태
  error: string | null
  setError: (error: string | null) => void
  
  // 마지막 요청 조건 저장
  lastRequest: ScholarshipCheckRequest | null
  setLastRequest: (request: ScholarshipCheckRequest) => void
  
  // AI 사용 설정
  useAI: boolean
  setUseAI: (use: boolean) => void
  
  // 공공데이터 사용 설정
  usePublicData: boolean
  setUsePublicData: (use: boolean) => void
}

export const useScholarshipStore = create<ScholarshipState>((set) => ({
  result: null,
  setResult: (result) => set({ result, error: null }),
  clearResult: () => set({ result: null }),
  
  isLoading: false,
  setLoading: (isLoading) => set({ isLoading }),
  
  error: null,
  setError: (error) => set({ error }),
  
  lastRequest: null,
  setLastRequest: (lastRequest) => set({ lastRequest }),
  
  useAI: true,
  setUseAI: (useAI) => set({ useAI }),
  
  usePublicData: true,
  setUsePublicData: (usePublicData) => set({ usePublicData }),
}))
