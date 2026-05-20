export function parseSceneActions(scene) {
  if (!scene) return []
  if (Array.isArray(scene.actions)) return scene.actions
  if (typeof scene.actions === 'string' && scene.actions.trim()) {
    try {
      const parsed = JSON.parse(scene.actions)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

export function normalizeScene(scene) {
  return {
    ...scene,
    enabled: scene.enabled !== false,
    actions: parseSceneActions(scene)
  }
}
