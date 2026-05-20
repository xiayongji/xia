import { defineStore } from 'pinia'
import { ref } from 'vue'
import { sceneApi } from '../api/scene'
import { useDeviceStore } from './devices'

const QUICK_SCENE_DEFS = [
  { key: 'home', name: '回家模式', description: '开启灯光和空调', icon: 'home' },
  { key: 'leave', name: '离家模式', description: '关闭灯光和空调', icon: 'leave' },
  { key: 'sleep', name: '睡眠模式', description: '关闭灯光', icon: 'sleep' },
  { key: 'movie', name: '阅读模式', description: '调节灯光', icon: 'movie' }
]

export const useSceneStore = defineStore('scenes', () => {
  const scenes = ref([])
  const loading = ref(false)

  async function fetchScenes() {
    loading.value = true
    try {
      const data = await sceneApi.getScenes()
      scenes.value = Array.isArray(data) ? data : []
      localStorage.setItem('scenesCache', JSON.stringify(scenes.value))
    } catch (error) {
      console.error('加载场景失败', error)
      const cached = localStorage.getItem('scenesCache')
      scenes.value = cached ? JSON.parse(cached) : []
    } finally {
      loading.value = false
    }
    return scenes.value
  }

  function resolveQuickScene(quick) {
    return scenes.value.find(
      s => s.name === quick.name || s.name?.includes(quick.name?.replace('模式', ''))
    )
  }

  async function triggerQuickScene(quick) {
    const deviceStore = useDeviceStore()
    const backendScene = resolveQuickScene(quick)

    if (backendScene?.id) {
      try {
        await sceneApi.executeScene(backendScene.id)
        await deviceStore.fetchDevices(true)
        return { ok: true, name: quick.name }
      } catch (error) {
        console.warn('服务端场景执行失败，尝试本地预设', error)
      }
    }

    const presetKey = quick.key || quick.presetKey
    if (presetKey) {
      return deviceStore.runScenePreset(presetKey)
    }
    return { ok: false, message: '场景未配置' }
  }

  async function toggleScene(sceneId, enabled) {
    await sceneApi.toggleScene(sceneId, enabled)
    const scene = scenes.value.find(s => s.id === sceneId)
    if (scene) scene.enabled = enabled
  }

  async function triggerSceneById(sceneId) {
    await sceneApi.executeScene(sceneId)
    const deviceStore = useDeviceStore()
    await deviceStore.fetchDevices(true)
    return { ok: true }
  }

  return {
    scenes,
    loading,
    quickSceneDefs: QUICK_SCENE_DEFS,
    fetchScenes,
    triggerQuickScene,
    triggerSceneById,
    toggleScene
  }
})
