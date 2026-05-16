import api from './api'

export const sceneApi = {
  getScenes() {
    return api.get('/scenes')
  },
  
  getScene(id) {
    return api.get(`/scenes/${id}`)
  },
  
  createScene(data) {
    return api.post('/scenes', data)
  },
  
  updateScene(id, data) {
    return api.put(`/scenes/${id}`, data)
  },
  
  deleteScene(id) {
    return api.delete(`/scenes/${id}`)
  },
  
  executeScene(id) {
    return api.post(`/scenes/${id}/execute`)
  },
  
  getSceneRules(id) {
    return api.get(`/scenes/${id}/rules`)
  },
  
  addSceneRule(id, data) {
    return api.post(`/scenes/${id}/rules`, data)
  },
  
  getSceneExecutions(id) {
    return api.get(`/scenes/${id}/executions`)
  }
}