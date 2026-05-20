import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'seckill-cart-items'

function readCart() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

export const useCartStore = defineStore('cart', () => {
  const items = ref(readCart())

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0))

  function addItem(product, quantity = 1) {
    const safeQuantity = Math.max(Number(quantity) || 1, 1)
    const existing = items.value.find(item => item.id === product.id)
    if (existing) {
      existing.quantity += safeQuantity
      return
    }
    items.value.push({
      id: product.id,
      name: product.name,
      price: Number(product.price),
      imageUrl: product.imageUrl,
      stock: product.stock,
      quantity: safeQuantity
    })
  }

  function updateQuantity(id, quantity) {
    const item = items.value.find(entry => entry.id === id)
    if (!item) return
    item.quantity = Math.max(Number(quantity) || 1, 1)
  }

  function removeItem(id) {
    items.value = items.value.filter(item => item.id !== id)
  }

  function clearCart() {
    items.value = []
  }

  watch(items, value => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  }, { deep: true })

  return { items, totalCount, totalAmount, addItem, updateQuantity, removeItem, clearCart }
})
