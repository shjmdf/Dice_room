<template>
  <div class="card-page">
    <el-header class="header">
      <el-button text @click="$router.push('/')">← 返回</el-button>
      <span class="card-title">{{ card?.name || '角色卡' }}</span>
      <el-button type="primary" @click="saveSheet" :loading="saving">保存</el-button>
    </el-header>
    <div class="card-body" v-if="card">
      <el-card>
        <template #header>基础信息</template>
        <el-form :inline="true">
          <el-form-item label="角色名">
            <el-input v-model="card.name" />
          </el-form-item>
          <el-form-item label="时代">
            <el-input v-model="card.era" />
          </el-form-item>
        </el-form>
      </el-card>
      <el-card style="margin-top:16px" v-if="sheet">
        <template #header>属性</template>
        <el-row :gutter="12">
          <el-col :span="4" v-for="(val, key) in sheet.characteristics" :key="key">
            <el-statistic :title="key" :value="val" />
          </el-col>
        </el-row>
      </el-card>
      <el-card style="margin-top:16px" v-if="sheet?.skills">
        <template #header>技能</template>
        <el-table :data="skillRows" stripe size="small" max-height="400">
          <el-table-column prop="name" label="技能名" width="140" />
          <el-table-column prop="value" label="当前值" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.value" :min="0" :max="99" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column prop="base" label="基础值" width="80" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const cardId = Number(route.params.cardId)
const card = ref(null)
const sheet = ref(null)
const saving = ref(false)

const skillRows = computed(() => {
  if (!sheet.value?.skills) return []
  return Object.entries(sheet.value.skills).map(([name, val]) => {
    if (typeof val === 'object') {
      return { name, value: val.value ?? val.current ?? 0, base: val.base ?? 0 }
    }
    return { name, value: val, base: 0 }
  })
})

onMounted(async () => {
  const { data } = await api.get(`/player-cards/${cardId}/sheet`)
  card.value = { id: data.id, name: data.name, era: data.era, ownerId: data.ownerId }
  sheet.value = data.sheet || {}
})

async function saveSheet() {
  saving.value = true
  try {
    await api.patch(`/player-cards/${cardId}/basic`, { name: card.value.name, era: card.value.era })
    if (sheet.value) {
      const updatedSkills = {}
      for (const row of skillRows.value) {
        updatedSkills[row.name] = { value: row.value, base: row.base }
      }
      const payload = { ...sheet.value, skills: updatedSkills }
      await api.put(`/player-cards/${cardId}/sheet`, { sheet: payload })
    }
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.card-page { min-height: 100vh; }
.header {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0 20px;
}
.card-title { font-weight: 600; flex: 1; }
.card-body { padding: 20px; }
</style>