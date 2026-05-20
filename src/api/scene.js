import api from './api'

export const sceneApi = {
  getScenes() {
    return api.get('/scene/scenes')
  },
  
  getScene(id) {
    return api.get(`/scene/scenes/${id}`)
  },
  
  createScene(data) {
    return api.post('/scene/scenes', data)
  },
  
  updateScene(id, data) {
    return api.put(`/scene/scenes/${id}`, data)
  },
  
  deleteScene(id) {
    return api.delete(`/scene/scenes/${id}`)
  },
  
  executeScene(id) {
    return api.post(`/scene/scenes/${id}/execute`)
  },

  toggleScene(id, enabled) {
    return api.put(`/scene/scenes/${id}/toggle`, null, { params: { enabled } })
  },
  
  getSceneRules(id) {
    return api.get(`/scene/scenes/${id}/rules`)
  },
  
  addSceneRule(id, data) {
    return api.post(`/scene/scenes/${id}/rules`, data)
  },
  
  getSceneExecutions(id) {
    return api.get(`/scene/scenes/${id}/executions`)
  }
}