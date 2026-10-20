import { create } from 'zustand'
import { persist } from 'zustand/middleware'

// Giỏ hàng lưu localStorage để giữ lại cả khi chưa đăng nhập
export const useCartStore = create(
  persist(
    (set) => ({
      items: [],

      addItem: (product, quantity = 1) =>
        set((state) => {
          const existing = state.items.find((item) => item.id === product.id)
          if (existing) {
            return {
              items: state.items.map((item) =>
                item.id === product.id ? { ...item, quantity: item.quantity + quantity } : item,
              ),
            }
          }
          const { id, name, price, imageUrl } = product
          return { items: [...state.items, { id, name, price, imageUrl, quantity }] }
        }),

      updateQuantity: (id, quantity) =>
        set((state) => ({
          items: state.items.map((item) =>
            item.id === id ? { ...item, quantity: Math.max(1, quantity) } : item,
          ),
        })),

      removeItem: (id) => set((state) => ({ items: state.items.filter((item) => item.id !== id) })),

      clear: () => set({ items: [] }),
    }),
    { name: 'watchstore-cart' },
  ),
)

export const selectCartCount = (state) => state.items.reduce((sum, item) => sum + item.quantity, 0)

export const selectCartTotal = (state) =>
  state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
