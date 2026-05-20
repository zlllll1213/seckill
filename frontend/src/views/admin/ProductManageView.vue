<template>
  <div class="page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openDialog()">新增商品</el-button>
    </div>
    <el-card class="table-card">
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="price" label="价格"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi } from '@/api'

const products = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const form = ref({ name: '', price: 0, stock: 0, description: '', imageUrl: '', status: 1 })

async function fetchProducts() {
  loading.value = true
  try {
    const res = await productApi.list(1, 1000)
    products.value = res.data.records || []
  } finally { loading.value = false }
}

function openDialog(row = null) {
  editId.value = row?.id ?? null
  form.value = row
    ? { name: row.name, price: row.price, stock: row.stock, description: row.description, imageUrl: row.imageUrl, status: row.status }
    : { name: '', price: 0, stock: 0, description: '', imageUrl: '', status: 1 }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editId.value) {
      await productApi.update(editId.value, form.value)
    } else {
      await productApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchProducts()
  } finally { saving.value = false }
}

onMounted(fetchProducts)
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table-card { border-radius: 8px; }
</style>
