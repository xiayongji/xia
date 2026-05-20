import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { Role } from '../utils/permissions'

/** 管理员管系统，普通用户只用设备 */
export function usePermissions() {
  const auth = useAuthStore()
  const role = computed(() => auth.role || Role.USER)
  const isAdmin = computed(() => role.value === Role.ADMIN)

  const canManageUsers = computed(() => isAdmin.value)
  const canManageDevices = computed(() => isAdmin.value)
  const canManageScenes = computed(() => isAdmin.value)
  const canManageSystem = computed(() => isAdmin.value)
  const canViewDevices = computed(() => true)
  const canControlDevices = computed(() => true)
  const canExecuteScenes = computed(() => true)
  const canViewEnergy = computed(() => true)

  return {
    role,
    isAdmin,
    canManageUsers,
    canManageDevices,
    canManageScenes,
    canManageSystem,
    canViewDevices,
    canControlDevices,
    canExecuteScenes,
    canViewEnergy
  }
}
