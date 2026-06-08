<template>
  <div class="card-page" v-loading="loading">
    <header class="topbar">
      <div class="title-block">
        <el-button text class="back-button" @click="router.push('/')">返回</el-button>
        <div>
          <div class="title">{{ sheet?.basicInformation?.name || '调查员档案' }}</div>
          <div class="subtitle">{{ sheet?.era || sheet?.basicInformation?.era || '未设置时代' }}</div>
        </div>
      </div>
      <div class="top-actions">
        <el-button @click="load">刷新</el-button>
        <el-button :loading="savingBasic" @click="saveBasicInfo">保存基础</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button type="danger" plain :loading="deleting" @click="deleteCard">删除</el-button>
      </div>
    </header>

    <main v-if="sheet" class="sheet-shell">
      <el-tabs v-model="activeTab" class="sheet-tabs">
        <el-tab-pane label="概览" name="overview">
          <section class="sheet-section">
            <div class="section-title">基础信息</div>
            <div class="form-grid basic-grid">
              <label>
                姓名
                <el-input v-model="sheet.basicInformation.name" />
              </label>
              <label>
                玩家
                <el-input v-model="sheet.basicInformation.playerName" />
              </label>
              <label>
                时代
                <el-input v-model="sheet.era" @input="sheet.basicInformation.era = sheet.era" />
              </label>
              <label>
                职业
                <el-input v-model="sheet.basicInformation.occupation" />
              </label>
              <label>
                年龄
                <el-input-number v-model="sheet.basicInformation.age" :min="0" :max="130" controls-position="right" />
              </label>
              <label>
                性别
                <el-input v-model="sheet.basicInformation.gender" />
              </label>
              <label>
                现居地
                <el-input v-model="sheet.basicInformation.residence" />
              </label>
              <label>
                出生地
                <el-input v-model="sheet.basicInformation.birthplace" />
              </label>
            </div>
          </section>

          <section class="sheet-section">
            <div class="section-title">属性</div>
            <div class="character-grid">
              <div v-for="[key, item] in characteristicRows" :key="key" class="character-cell">
                <div class="cell-head">
                  <span>{{ item.name }}</span>
                  <small>{{ item.code }}</small>
                </div>
                <el-input-number
                  v-model="item.value"
                  :min="0"
                  :max="99"
                  controls-position="right"
                  @change="recalcCharacteristic(item)"
                />
                <div class="derived">困难 {{ item.hard }} / 极难 {{ item.extreme }}</div>
              </div>
            </div>
          </section>

          <section class="sheet-section">
            <div class="section-title">资源与状态</div>
            <div class="resource-grid">
              <div v-for="[key, value] in resourceRows" :key="key" class="resource-cell">
                <div class="cell-head">
                  <span>{{ resourceLabel(key) }}</span>
                  <small>{{ key.toUpperCase() }}</small>
                </div>
                <div class="resource-inputs">
                  <el-input-number v-model="value.current" :min="0" controls-position="right" />
                  <el-input-number v-model="value.initial" :min="0" controls-position="right" />
                  <el-input-number v-model="value.max" :min="0" controls-position="right" />
                </div>
                <div class="resource-labels">
                  <span>当前</span>
                  <span>初始</span>
                  <span>最大</span>
                </div>
              </div>
            </div>
            <div class="status-grid">
              <el-checkbox v-model="sheet.status.seriousInjury">重伤</el-checkbox>
              <el-checkbox v-model="sheet.status.unconscious">昏迷</el-checkbox>
              <el-checkbox v-model="sheet.status.dead">死亡</el-checkbox>
              <el-checkbox v-model="sheet.status.temporaryInsanity">临时疯狂</el-checkbox>
              <el-checkbox v-model="sheet.status.indefiniteInsanity">不定期疯狂</el-checkbox>
              <el-checkbox v-model="sheet.status.permanentInsanity">永久疯狂</el-checkbox>
            </div>
          </section>
        </el-tab-pane>

        <el-tab-pane label="技能" name="skills">
          <section class="sheet-section">
            <div class="section-title with-tools">
              <span>技能表</span>
              <div class="toolbar">
                <el-select v-model="skillCategory" size="small" class="category-select">
                  <el-option label="全部" value="" />
                  <el-option v-for="category in skillCategories" :key="category" :label="category" :value="category" />
                </el-select>
                <el-input v-model="skillKeyword" size="small" placeholder="搜索技能" clearable class="search-input" />
                <el-button size="small" @click="addSkill">添加</el-button>
              </div>
            </div>

            <el-table :data="filteredSkills" class="skill-table" height="620">
              <el-table-column label="本职" width="64" align="center">
                <template #default="{ row }">
                  <el-checkbox v-model="row.occupationSkill" />
                </template>
              </el-table-column>
              <el-table-column label="类别" width="92">
                <template #default="{ row }">
                  <el-input v-model="row.category" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="技能" min-width="160">
                <template #default="{ row }">
                  <div class="skill-name-edit">
                    <el-input v-model="row.name" size="small" />
                    <el-input v-model="row.specialization" size="small" placeholder="专长" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="基础" width="86">
                <template #default="{ row }">
                  <el-input-number v-model="row.base" :min="0" :max="99" size="small" controls-position="right" @change="recalcSkill(row)" />
                </template>
              </el-table-column>
              <el-table-column label="职业" width="86">
                <template #default="{ row }">
                  <el-input-number v-model="row.occupation" :min="0" :max="99" size="small" controls-position="right" @change="recalcSkill(row)" />
                </template>
              </el-table-column>
              <el-table-column label="兴趣" width="86">
                <template #default="{ row }">
                  <el-input-number v-model="row.interest" :min="0" :max="99" size="small" controls-position="right" @change="recalcSkill(row)" />
                </template>
              </el-table-column>
              <el-table-column label="成长" width="86">
                <template #default="{ row }">
                  <el-input-number v-model="row.growth" :min="0" :max="99" size="small" controls-position="right" @change="recalcSkill(row)" />
                </template>
              </el-table-column>
              <el-table-column label="成功" width="92">
                <template #default="{ row }">
                  <el-input-number v-model="row.success" :min="0" :max="99" size="small" controls-position="right" @change="recalcSkillThreshold(row)" />
                </template>
              </el-table-column>
              <el-table-column label="困难" prop="hard" width="70" />
              <el-table-column label="极难" prop="extreme" width="70" />
            </el-table>
          </section>
        </el-tab-pane>

        <el-tab-pane label="战斗" name="combat">
          <section class="sheet-section">
            <div class="section-title">战斗</div>
            <div class="form-grid combat-grid">
              <label>
                伤害加值
                <el-input v-model="sheet.combat.damageBonus" />
              </label>
              <label>
                体格
                <el-input-number v-model="sheet.combat.build" controls-position="right" />
              </label>
              <label>
                护甲
                <el-input-number v-model="sheet.combat.armor" :min="0" controls-position="right" />
              </label>
              <label>
                移动力
                <el-input-number v-model="sheet.combat.movement" :min="0" controls-position="right" />
              </label>
            </div>
          </section>

          <section class="sheet-section">
            <div class="section-title with-tools">
              <span>武器</span>
              <el-button size="small" @click="addWeapon">添加</el-button>
            </div>
            <el-table :data="sheet.weapons" class="weapon-table">
              <el-table-column label="名称" min-width="130">
                <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
              </el-table-column>
              <el-table-column label="技能" min-width="130">
                <template #default="{ row }"><el-input v-model="row.skill" size="small" /></template>
              </el-table-column>
              <el-table-column label="%" width="86">
                <template #default="{ row }"><el-input-number v-model="row.successRate" :min="0" :max="99" size="small" controls-position="right" /></template>
              </el-table-column>
              <el-table-column label="伤害" min-width="120">
                <template #default="{ row }"><el-input v-model="row.damage" size="small" /></template>
              </el-table-column>
              <el-table-column label="射程" min-width="100">
                <template #default="{ row }"><el-input v-model="row.range" size="small" /></template>
              </el-table-column>
              <el-table-column label="贯穿" width="70" align="center">
                <template #default="{ row }"><el-checkbox v-model="row.impale" /></template>
              </el-table-column>
              <el-table-column label="次数" width="86">
                <template #default="{ row }"><el-input-number v-model="row.attacksPerRound" :min="0" size="small" controls-position="right" /></template>
              </el-table-column>
              <el-table-column label="装弹" width="86">
                <template #default="{ row }"><el-input-number v-model="row.ammo" :min="0" size="small" controls-position="right" /></template>
              </el-table-column>
              <el-table-column label="故障" width="100">
                <template #default="{ row }"><el-input v-model="row.malfunction" size="small" /></template>
              </el-table-column>
            </el-table>
          </section>
        </el-tab-pane>

        <el-tab-pane label="背景" name="story">
          <section class="sheet-section">
            <div class="section-title">背景故事</div>
            <div class="story-grid">
              <label v-for="item in storyFields" :key="item.key">
                {{ item.label }}
                <el-input v-model="sheet.story[item.key]" type="textarea" :rows="4" resize="vertical" />
              </label>
            </div>
          </section>

          <section class="sheet-section">
            <div class="section-title">物品与资产</div>
            <div class="story-grid two">
              <label>
                物品与装备
                <el-input v-model="sheet.possessions.text" type="textarea" :rows="8" resize="vertical" />
              </label>
              <div class="asset-grid">
                <label>
                  信用评级
                  <el-input-number v-model="sheet.cashAndAssets.creditRating" :min="0" :max="99" controls-position="right" />
                </label>
                <label>
                  现金
                  <el-input v-model="sheet.cashAndAssets.cash" />
                </label>
                <label>
                  消费水平
                  <el-input v-model="sheet.cashAndAssets.spendingLevel" />
                </label>
                <label>
                  资产
                  <el-input v-model="sheet.cashAndAssets.assets" type="textarea" :rows="4" resize="vertical" />
                </label>
              </div>
            </div>
          </section>

          <section class="sheet-section">
            <div class="section-title with-tools">
              <span>克苏鲁神话</span>
              <el-button size="small" @click="addMythosEntry">添加</el-button>
            </div>
            <div class="mythos-head">
              <label>
                神话技能值
                <el-input-number v-model="sheet.cthulhuMythos.mythosSkillValue" :min="0" :max="99" controls-position="right" />
              </label>
              <label>
                魔法物品与典籍
                <el-input v-model="sheet.cthulhuMythos.magicItemsAndTomes" />
              </label>
              <label>
                法术
                <el-input v-model="sheet.cthulhuMythos.spells" />
              </label>
            </div>
            <el-table :data="sheet.cthulhuMythos.entries">
              <el-table-column label="类型" width="120">
                <template #default="{ row }"><el-input v-model="row.entryType" size="small" /></template>
              </el-table-column>
              <el-table-column label="名称" min-width="140">
                <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
              </el-table-column>
              <el-table-column label="描述" min-width="260">
                <template #default="{ row }"><el-input v-model="row.description" size="small" /></template>
              </el-table-column>
              <el-table-column label="来源" min-width="140">
                <template #default="{ row }"><el-input v-model="row.source" size="small" /></template>
              </el-table-column>
            </el-table>
          </section>

          <section class="sheet-section">
            <div class="section-title with-tools">
              <span>关系与经历</span>
              <div class="toolbar">
                <el-button size="small" @click="addRelationship">添加关系</el-button>
                <el-button size="small" @click="addScenario">添加经历</el-button>
              </div>
            </div>
            <div class="split-grid">
              <el-table :data="sheet.relationships">
                <el-table-column label="角色" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.characterName" size="small" /></template>
                </el-table-column>
                <el-table-column label="关系" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.relationship" size="small" /></template>
                </el-table-column>
                <el-table-column label="玩家" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.playerName" size="small" /></template>
                </el-table-column>
              </el-table>
              <el-table :data="sheet.experiencedScenarios">
                <el-table-column label="模组" min-width="140">
                  <template #default="{ row }"><el-input v-model="row.scenarioName" size="small" /></template>
                </el-table-column>
                <el-table-column label="经历" min-width="180">
                  <template #default="{ row }"><el-input v-model="row.experience" size="small" /></template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </el-tab-pane>

        <el-tab-pane label="JSON" name="json">
          <section class="sheet-section">
            <div class="section-title with-tools">
              <span>角色卡 JSON</span>
              <div class="toolbar">
                <el-button size="small" @click="loadJson">读取 JSON</el-button>
                <el-button size="small" type="primary" :loading="savingJson" @click="saveJson">写入 JSON</el-button>
                <el-button size="small" @click="applyJsonToEditor">应用到编辑器</el-button>
              </div>
            </div>
            <el-input
              v-model="jsonText"
              type="textarea"
              :rows="22"
              resize="vertical"
              spellcheck="false"
              class="json-editor"
            />
          </section>
        </el-tab-pane>
      </el-tabs>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const cardId = Number(route.params.cardId)

const loading = ref(false)
const saving = ref(false)
const savingBasic = ref(false)
const savingJson = ref(false)
const deleting = ref(false)
const cardMeta = ref(null)
const sheet = ref(null)
const jsonText = ref('')
const activeTab = ref('overview')
const skillKeyword = ref('')
const skillCategory = ref('')

const storyFields = [
  { key: 'appearance', label: '形象描述' },
  { key: 'ideologyAndBeliefs', label: '思想与信念' },
  { key: 'significantPeople', label: '重要之人' },
  { key: 'meaningfulLocations', label: '意义非凡之地' },
  { key: 'treasuredPossessions', label: '宝贵之物' },
  { key: 'traits', label: '特质' },
  { key: 'injuriesAndScars', label: '伤口与疤痕' },
  { key: 'mentalSymptoms', label: '精神症状' },
  { key: 'personalDescription', label: '个人介绍' }
]

const characteristicRows = computed(() => Object.entries(sheet.value?.characteristics || {}))
const resourceRows = computed(() => Object.entries(sheet.value?.resources || {}))
const skillCategories = computed(() => {
  const values = new Set((sheet.value?.skills || []).map(skill => skill.category).filter(Boolean))
  return [...values]
})
const filteredSkills = computed(() => {
  const keyword = skillKeyword.value.trim()
  return (sheet.value?.skills || []).filter(skill => {
    const categoryOk = !skillCategory.value || skill.category === skillCategory.value
    const keywordOk = !keyword
      || skill.name?.includes(keyword)
      || skill.specialization?.includes(keyword)
      || skill.category?.includes(keyword)
    return categoryOk && keywordOk
  })
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [cardRes, sheetRes] = await Promise.all([
      api.get(`/player-cards/${cardId}`),
      api.get(`/player-cards/${cardId}/sheet`)
    ])
    cardMeta.value = cardRes.data
    sheet.value = normalizeSheet(sheetRes.data.sheet)
    syncJsonText()
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    normalizeBeforeSave()
    await api.put(`/player-cards/${cardId}/sheet`, { sheet: sheet.value })
    syncJsonText()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function saveBasicInfo() {
  if (!sheet.value) return
  savingBasic.value = true
  try {
    const name = sheet.value.basicInformation?.name || cardMeta.value?.name || '未命名角色'
    const era = sheet.value.era || sheet.value.basicInformation?.era || ''
    await api.patch(`/player-cards/${cardId}/basic`, { name, era })
    sheet.value.basicInformation.name = name
    sheet.value.basicInformation.era = era
    sheet.value.era = era
    if (cardMeta.value) {
      cardMeta.value.name = name
      cardMeta.value.era = era
    }
    ElMessage.success('基础信息已保存')
  } finally {
    savingBasic.value = false
  }
}

async function loadJson() {
  const { data } = await api.get(`/player-cards/${cardId}/json`)
  jsonText.value = JSON.stringify(data, null, 2)
  ElMessage.success('JSON 已读取')
}

function applyJsonToEditor() {
  try {
    sheet.value = normalizeSheet(JSON.parse(jsonText.value))
    ElMessage.success('JSON 已应用到编辑器')
  } catch (error) {
    ElMessage.error('JSON 格式不正确')
  }
}

async function saveJson() {
  savingJson.value = true
  try {
    const parsed = JSON.parse(jsonText.value)
    await api.put(`/player-cards/${cardId}/json`, { sheet: parsed })
    sheet.value = normalizeSheet(parsed)
    syncJsonText()
    ElMessage.success('JSON 已写入')
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('JSON 格式不正确')
      return
    }
    throw error
  } finally {
    savingJson.value = false
  }
}

async function deleteCard() {
  await ElMessageBox.confirm('确定删除这张角色卡吗？', '删除角色卡', { type: 'warning' })
  deleting.value = true
  try {
    await api.delete(`/player-cards/${cardId}`)
    ElMessage.success('角色卡已删除')
    router.push('/')
  } finally {
    deleting.value = false
  }
}

function syncJsonText() {
  jsonText.value = JSON.stringify(sheet.value, null, 2)
}

function normalizeSheet(value) {
  const target = value || {}
  target.basicInformation ||= {}
  target.characteristics ||= {}
  target.resources ||= {}
  target.status ||= {}
  target.skillPoints ||= {}
  target.skills ||= []
  target.weapons ||= []
  target.combat ||= {}
  target.story ||= {}
  target.possessions ||= {}
  target.possessions.items ||= []
  target.cashAndAssets ||= {}
  target.cthulhuMythos ||= {}
  target.cthulhuMythos.entries ||= []
  target.relationships ||= []
  target.experiencedScenarios ||= []
  return target
}

function normalizeBeforeSave() {
  sheet.value.basicInformation.era = sheet.value.era || sheet.value.basicInformation.era || ''
  sheet.value.basicInformation.name = sheet.value.basicInformation.name || cardMeta.value?.name || ''
  sheet.value.status.majorWound = sheet.value.status.seriousInjury
  sheet.value.skills.forEach(recalcSkillThreshold)
  characteristicRows.value.forEach(([, item]) => recalcCharacteristic(item))
}

function recalcCharacteristic(item) {
  const value = Number(item.value || 0)
  item.hard = Math.max(1, Math.floor(value / 2))
  item.extreme = Math.max(1, Math.floor(value / 5))
}

function recalcSkill(skill) {
  skill.success = Math.min(99, Number(skill.base || 0) + Number(skill.occupation || 0) + Number(skill.interest || 0) + Number(skill.growth || 0))
  recalcSkillThreshold(skill)
}

function recalcSkillThreshold(skill) {
  const value = Number(skill.success || 0)
  skill.hard = Math.max(1, Math.floor(value / 2))
  skill.extreme = Math.max(1, Math.floor(value / 5))
}

function addSkill() {
  sheet.value.skills.push({
    category: '其它',
    name: '自定义',
    specialization: '',
    base: 0,
    occupation: 0,
    interest: 0,
    growth: 0,
    success: 0,
    hard: 1,
    extreme: 1,
    occupationSkill: false
  })
}

function addWeapon() {
  sheet.value.weapons.push({
    name: '',
    skill: '',
    successRate: 0,
    damage: '',
    range: '',
    impale: false,
    attacksPerRound: 1,
    ammo: 0,
    malfunction: ''
  })
}

function addMythosEntry() {
  sheet.value.cthulhuMythos.entries.push({ entryType: '', name: '', description: '', source: '' })
}

function addRelationship() {
  sheet.value.relationships.push({ characterName: '', relationship: '', playerName: '' })
}

function addScenario() {
  sheet.value.experiencedScenarios.push({ scenarioName: '', experience: '' })
}

function resourceLabel(key) {
  return { hp: '生命值', mp: '魔法值', san: '理智值' }[key] || key
}
</script>

<style scoped>
.card-page {
  min-height: 100vh;
  background: #f4f6f8;
  color: #20252b;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #d8dee6;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-block,
.top-actions,
.with-tools,
.toolbar {
  display: flex;
  align-items: center;
}

.title-block {
  gap: 12px;
}

.back-button {
  padding-left: 0;
}

.title {
  font-size: 18px;
  font-weight: 700;
}

.subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #687381;
}

.top-actions {
  gap: 8px;
}

.sheet-shell {
  max-width: 1320px;
  margin: 0 auto;
  padding: 16px;
}

.sheet-tabs {
  background: #ffffff;
  border: 1px solid #d8dee6;
  border-radius: 8px;
  padding: 0 16px 16px;
}

.sheet-section {
  padding: 18px 0;
  border-bottom: 1px solid #e5e9ef;
}

.sheet-section:last-child {
  border-bottom: 0;
}

.section-title {
  margin-bottom: 14px;
  font-weight: 700;
  color: #20252b;
}

.with-tools {
  justify-content: space-between;
  gap: 12px;
}

.toolbar {
  gap: 8px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
}

.category-select {
  width: 120px;
}

.form-grid {
  display: grid;
  gap: 14px;
}

.basic-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.combat-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #687381;
}

.character-grid,
.resource-grid,
.status-grid,
.mythos-head {
  display: grid;
  gap: 12px;
}

.character-grid {
  grid-template-columns: repeat(9, minmax(0, 1fr));
}

.resource-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.status-grid {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  margin-top: 14px;
}

.character-cell,
.resource-cell {
  border: 1px solid #d8dee6;
  background: #fbfcfd;
  border-radius: 8px;
  padding: 10px;
}

.cell-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 700;
}

.cell-head small {
  color: #8a94a3;
}

.derived {
  margin-top: 7px;
  font-size: 12px;
  color: #687381;
}

.resource-inputs,
.resource-labels {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.resource-labels {
  margin-top: 6px;
  font-size: 11px;
  color: #8a94a3;
}

.skill-table,
.weapon-table {
  width: 100%;
}

.skill-name-edit {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(90px, 0.55fr);
  gap: 6px;
}

.story-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.story-grid.two {
  grid-template-columns: minmax(0, 1fr) 360px;
}

.asset-grid {
  display: grid;
  gap: 12px;
}

.json-editor {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.mythos-head {
  grid-template-columns: 160px minmax(0, 1fr) minmax(0, 1fr);
  margin-bottom: 14px;
}

.split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1100px) {
  .basic-grid,
  .combat-grid,
  .resource-grid,
  .story-grid,
  .story-grid.two,
  .mythos-head,
  .split-grid {
    grid-template-columns: 1fr 1fr;
  }

  .character-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .topbar {
    height: auto;
    min-height: 64px;
    align-items: flex-start;
    padding: 12px;
    gap: 12px;
    flex-direction: column;
  }

  .top-actions {
    width: 100%;
  }

  .top-actions .el-button {
    flex: 1;
  }

  .basic-grid,
  .combat-grid,
  .resource-grid,
  .status-grid,
  .story-grid,
  .story-grid.two,
  .mythos-head,
  .split-grid {
    grid-template-columns: 1fr;
  }

  .character-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .with-tools {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar,
  .search-input,
  .category-select {
    width: 100%;
  }
}
</style>
