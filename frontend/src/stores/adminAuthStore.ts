import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AdminUser {
  id: string
  username: string
  name: string | null
}

interface AdminAuthState {
  admin: AdminUser | null
  adminToken: string | null
  isAdminAuthenticated: boolean
  
  setAdmin: (admin: AdminUser | null) => void
  setAdminToken: (token: string | null) => void
  adminLogin: (admin: AdminUser, token: string) => void
  adminLogout: () => void
}

export const useAdminAuthStore = create<AdminAuthState>()(
  persist(
    (set) => ({
      admin: null,
      adminToken: null,
      isAdminAuthenticated: false,
      
      setAdmin: (admin) => set({ admin, isAdminAuthenticated: !!admin }),
      
      setAdminToken: (token) => {
        if (token) {
          localStorage.setItem('admin_token', token)
        } else {
          localStorage.removeItem('admin_token')
        }
        set({ adminToken: token })
      },
      
      adminLogin: (admin, token) => {
        localStorage.setItem('admin_token', token)
        set({ admin, adminToken: token, isAdminAuthenticated: true })
      },
      
      adminLogout: () => {
        localStorage.removeItem('admin_token')
        set({ admin: null, adminToken: null, isAdminAuthenticated: false })
      },
    }),
    {
      name: 'admin-auth-storage',
      partialize: (state) => ({ 
        admin: state.admin, 
        adminToken: state.adminToken,
        isAdminAuthenticated: state.isAdminAuthenticated 
      }),
    }
  )
)
