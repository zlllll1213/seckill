<template>
  <div class="page">
    <div class="page-header">
      <h2>秒杀活动管理</h2>
      <el-button type="primary" @click="openDialog()">新增活动</el-button>
    </div>
    <el-card class="table-card">
      <el-table :data="activities" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="160" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column prop="seckillPrice" label="秒杀价"><template #default="{ row }">¥{{ row.seckillPrice }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['info','success','warning'][row.status]">{{ ['待开始','进行中','已结束'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="170" />
        <el-table-column prop="endTime" label="结束时间" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑活动' : '新增活动'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="活动名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="1" /></el-form-item>
        <el-alert v-if="editId && lockedRunning" title="活动进行中，价格和库存将保持原值" type="warning" :closable="false" show-icon class="dialog-alert" />
        <el-form-item label="秒杀价格"><el-input-number v-model="form.seckillPrice" :precision="2" :min="0" :disabled="lockedRunning" /></el-form-item>
        <el-form-item label="库存数量"><el-input-number v-model="form.stock" :min="1" :disabled="lockedRunning" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="0" label="待开始" />
            <el-option :value="1" label="进行中" />
            <el-option :value="2" label="已结束" />
          </el-select>
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
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { activityApi } from '@/api'

const activities = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const emptyForm = () => ({ name: '', productId: null, seckillPrice: 0, stock: 100, startTime: '', endTime: '', status: 0 })
const form = ref(emptyForm())
const lockedRunning = computed(() => form.value.status === 1)

async function fetchActivities() {
  loading.value = true
  try {
    const res = await activityApi.list()
    activities.value = res.data
  } finally { loading.value = false }
}

function openDialog(row = null) {
  editId.value = row?.id ?? null
  form.value = row
    ? { name: row.name, productId: row.productId, seckillPrice: row.seckillPrice, stock: row.stock, startTime: row.startTime, endTime: row.endTime, status: row.status }
    : emptyForm()
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editId.value) {
      await activityApi.update(editId.value, form.value)
    } else {
      await activityApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchActivities()
  } finally { saving.value = false }
}

onMounted(fetchActivities)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.table-card { border-radius: 8px; }
.dialog-alert { margin-bottom: 16px; }
</style>
