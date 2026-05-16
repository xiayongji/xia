<template>
  <div class="energy">
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">能耗统计</h1>
        <p class="page-subtitle">查看和分析您的能源消耗情况</p>
      </div>
      <div class="header-actions">
        <el-select v-model="timeRange" class="time-range-select">
          <el-option label="今日" value="day" />
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本年" value="year" />
        </el-select>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card primary">
        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="28"><Lightning /></el-icon>
          </div>
          <span class="stat-badge">较昨日 -12%</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ currentPeriodEnergy }}</div>
          <div class="stat-unit">kWh</div>
        </div>
        <div class="stat-label">{{ timeRangeText }}能耗</div>
      </div>

      <div class="stat-card secondary">
        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="28"><WalletFilled /></el-icon>
          </div>
          <span class="stat-badge positive">较上月 -8%</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ currentPeriodCost }}</div>
          <div class="stat-unit">元</div>
        </div>
        <div class="stat-label">{{ timeRangeText }}费用</div>
      </div>

      <div class="stat-card tertiary">
        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <span class="stat-badge">日均</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ avgDailyEnergy }}</div>
          <div class="stat-unit">kWh</div>
        </div>
        <div class="stat-label">日均能耗</div>
      </div>

      <div class="stat-card accent">
        <div class="stat-header">
          <div class="stat-icon">
            <el-icon :size="28"><Trophy /></el-icon>
          </div>
          <span class="stat-badge positive">节能达人</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ savedEnergy }}</div>
          <div class="stat-unit">kWh</div>
        </div>
        <div class="stat-label">本月已节省</div>
      </div>
    </div>

    <div class="chart-section">
      <div class="section-card">
        <div class="section-header">
          <h2>能耗趋势</h2>
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-color" style="background: #10B981"></span>
              <span>能耗 (kWh)</span>
            </div>
            <div class="legend-item">
              <span class="legend-color" style="background: #3B82F6"></span>
              <span>费用 (元)</span>
            </div>
          </div>
        </div>
        <div ref="chartRef" class="chart-container"></div>
      </div>

      <div class="section-card">
        <div class="section-header">
          <h2>设备能耗排行</h2>
        </div>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in deviceEnergyRanking" 
            :key="item.name"
            class="ranking-item"
          >
            <div class="ranking-position" :class="getRankClass(index)">
              {{ index + 1 }}
            </div>
            <div class="ranking-info">
              <div class="device-name">{{ item.name }}</div>
              <div class="progress-bar">
                <div 
                  class="progress-fill" 
                  :style="{ width: item.percentage + '%', background: item.color }"
                ></div>
              </div>
            </div>
            <div class="ranking-stats">
              <div class="energy-value">{{ item.energy }} kWh</div>
              <div class="energy-percent">{{ item.percentage }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="tips-section">
      <div class="section-card">
        <div class="section-header">
          <h2>节能建议</h2>
          <el-icon :size="20"><Sunny /></el-icon>
        </div>
        <div class="tips-list">
          <div class="tip-card" v-for="tip in energyTips" :key="tip.id">
            <div class="tip-icon" :style="{ background: tip.bgColor }">
              <component :is="tip.icon" :size="24" />
            </div>
            <div class="tip-content">
              <h4>{{ tip.title }}</h4>
              <p>{{ tip.description }}</p>
            </div>
            <div class="tip-savings">
              <span class="savings-value">{{ tip.savings }}</span>
              <span class="savings-label">预计节省</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { Lightning, WalletFilled, TrendCharts, Trophy, Sunny, Clock, WindPower, HotWater } from '@element-plus/icons-vue'

const timeRange = ref('month')
const chartRef = ref(null)
let chart = null

const timeRangeText = computed(() => {
  const texts = {
    'day': '今日',
    'week': '本周',
    'month': '本月',
    'year': '本年'
  }
  return texts[timeRange.value] || '本月'
})

const currentPeriodEnergy = ref(120.5)
const currentPeriodCost = ref(60.25)
const avgDailyEnergy = ref(4.0)
const savedEnergy = ref(23.8)

const deviceEnergyRanking = ref([
  { name: '空调', energy: 45.2, percentage: 37.5, color: 'linear-gradient(90deg, #3B82F6 0%, #1D4ED8 100%)' },
  { name: '洗衣机', energy: 28.6, percentage: 23.7, color: 'linear-gradient(90deg, #8B5CF6 0%, #7C3AED 100%)' },
  { name: '冰箱', energy: 22.8, percentage: 18.9, color: 'linear-gradient(90deg, #10B981 0%, #059669 100%)' },
  { name: '照明', energy: 15.3, percentage: 12.7, color: 'linear-gradient(90deg, #F59E0B 0%, #D97706 100%)' },
  { name: '其他', energy: 8.6, percentage: 7.2, color: 'linear-gradient(90deg, #94A3B8 0%, #64748B 100%)' }
])

const energyTips = ref([
  {
    id: '1',
    icon: HotWater,
    title: '优化空调温度',
    description: '夏季设置在26°C，冬季设置在20°C，可节省约15%的能耗',
    savings: '15%',
    bgColor: 'linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%)'
  },
  {
    id: '2',
    icon: Clock,
    title: '定时关闭设备',
    description: '设置智能定时，自动关闭待机设备，避免待机能耗',
    savings: '8%',
    bgColor: 'linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%)'
  },
  {
    id: '3',
    icon: Sunny,
    title: '利用自然光',
    description: '白天尽量使用自然光照明，减少人工光源使用',
    savings: '12%',
    bgColor: 'linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%)'
  },
  {
    id: '4',
    icon: WindPower,
    title: '使用节能模式',
    description: '开启家电的节能模式，在保证使用体验的同时降低能耗',
    savings: '10%',
    bgColor: 'linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%)'
  }
])

const getRankClass = (index) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

const generateChartData = () => {
  const data = []
  if (timeRange.value === 'day') {
    for (let i = 0; i < 24; i++) {
      data.push({
        time: i + ':00',
        energy: Math.random() * 0.5 + 0.1,
        cost: Math.random() * 0.25 + 0.05
      })
    }
  } else if (timeRange.value === 'week') {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    for (let i = 0; i < 7; i++) {
      data.push({
        time: days[i],
        energy: Math.random() * 8 + 3,
        cost: Math.random() * 4 + 1.5
      })
    }
  } else if (timeRange.value === 'month') {
    for (let i = 1; i <= 30; i++) {
      data.push({
        time: i + '日',
        energy: Math.random() * 5 + 2,
        cost: Math.random() * 2.5 + 1
      })
    }
  } else if (timeRange.value === 'year') {
    const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    for (let i = 0; i < 12; i++) {
      data.push({
        time: months[i],
        energy: Math.random() * 50 + 80,
        cost: Math.random() * 25 + 40
      })
    }
  }
  return data
}

const updateChart = () => {
  if (!chart) return
  
  const chartData = generateChartData()
  const xAxisData = chartData.map(item => item.time)
  const energyData = chartData.map(item => item.energy)
  const costData = chartData.map(item => item.cost)
  
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: {
        color: '#1E293B'
      },
      formatter: (params) => {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].axisValue}</div>`
        params.forEach(param => {
          result += `<div style="display: flex; justify-content: space-between; gap: 20px; margin: 4px 0;">
            <span>${param.seriesName}</span>
            <span style="font-weight: 600;">${param.value} ${param.seriesName === '能耗' ? 'kWh' : '元'}</span>
          </div>`
        })
        return result
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLine: {
        lineStyle: { color: '#E2E8F0' }
      },
      axisLabel: {
        color: '#64748B',
        rotate: timeRange.value === 'month' ? 45 : 0
      },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value',
        name: '能耗 (kWh)',
        nameTextStyle: { color: '#64748B' },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#64748B' },
        splitLine: { lineStyle: { color: '#F1F5F9' } }
      },
      {
        type: 'value',
        name: '费用 (元)',
        nameTextStyle: { color: '#64748B' },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#64748B' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '能耗',
        type: 'line',
        smooth: true,
        data: energyData,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
            ]
          }
        },
        lineStyle: { color: '#10B981', width: 3 },
        itemStyle: { color: '#10B981' },
        symbol: 'circle',
        symbolSize: 6
      },
      {
        name: '费用',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: costData,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
            ]
          }
        },
        lineStyle: { color: '#3B82F6', width: 3 },
        itemStyle: { color: '#3B82F6' },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  })
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
  updateChart()
  
  window.addEventListener('resize', () => {
    chart.resize()
  })
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
  }
  window.removeEventListener('resize', () => {
    chart.resize()
  })
})

watch(timeRange, () => {
  updateChart()
  
  if (timeRange.value === 'day') {
    currentPeriodEnergy.value = 5.2
    currentPeriodCost.value = 2.6
    avgDailyEnergy.value = 5.2
    savedEnergy.value = 0.8
  } else if (timeRange.value === 'week') {
    currentPeriodEnergy.value = 35.8
    currentPeriodCost.value = 17.9
    avgDailyEnergy.value = 5.1
    savedEnergy.value = 5.2
  } else if (timeRange.value === 'month') {
    currentPeriodEnergy.value = 120.5
    currentPeriodCost.value = 60.25
    avgDailyEnergy.value = 4.0
    savedEnergy.value = 23.8
  } else if (timeRange.value === 'year') {
    currentPeriodEnergy.value = 1452.3
    currentPeriodCost.value = 726.15
    avgDailyEnergy.value = 4.0
    savedEnergy.value = 285.6
  }
})
</script>

<style scoped>
.energy {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.time-range-select {
  width: 120px;
  border-radius: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card.primary::before {
  background: linear-gradient(90deg, #10B981 0%, #059669 100%);
}

.stat-card.secondary::before {
  background: linear-gradient(90deg, #3B82F6 0%, #1D4ED8 100%);
}

.stat-card.tertiary::before {
  background: linear-gradient(90deg, #8B5CF6 0%, #7C3AED 100%);
}

.stat-card.accent::before {
  background: linear-gradient(90deg, #F59E0B 0%, #D97706 100%);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card.primary .stat-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.stat-card.secondary .stat-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.stat-card.tertiary .stat-icon {
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.stat-card.accent .stat-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.stat-badge {
  padding: 4px 10px;
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.stat-badge.positive {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.stat-content {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1E293B;
}

.stat-unit {
  font-size: 14px;
  color: #64748B;
}

.stat-label {
  font-size: 14px;
  color: #64748B;
}

.chart-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 32px;
}

.section-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748B;
}

.legend-color {
  width: 12px;
  height: 4px;
  border-radius: 2px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ranking-position {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #64748B;
}

.ranking-position.rank-1 {
  background: linear-gradient(135deg, #FCD34D 0%, #F59E0B 100%);
  color: #fff;
}

.ranking-position.rank-2 {
  background: linear-gradient(135deg, #CBD5E1 0%, #94A3B8 100%);
  color: #fff;
}

.ranking-position.rank-3 {
  background: linear-gradient(135deg, #FDBA74 0%, #F97316 100%);
  color: #fff;
}

.ranking-info {
  flex: 1;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  margin-bottom: 6px;
}

.progress-bar {
  height: 8px;
  background: #F1F5F9;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s ease;
}

.ranking-stats {
  text-align: right;
}

.energy-value {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.energy-percent {
  font-size: 12px;
  color: #64748B;
}

.tips-section {
  margin-bottom: 32px;
}

.tips-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.tip-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #F8FAFC;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.tip-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.tip-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-content h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 6px;
}

.tip-content p {
  font-size: 13px;
  color: #64748B;
  margin: 0;
  line-height: 1.5;
}

.tip-savings {
  text-align: right;
}

.savings-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #10B981;
}

.savings-label {
  font-size: 11px;
  color: #94A3B8;
}

@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-section {
    grid-template-columns: 1fr;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .tips-list {
    grid-template-columns: 1fr;
  }
  
  .tip-card {
    flex-direction: column;
    text-align: center;
  }
  
  .tip-savings {
    text-align: center;
  }
}
</style>