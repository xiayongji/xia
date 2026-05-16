export const Role = {
  ADMIN: 'admin',
  USER: 'user'
}

export const Permission = {
  DASHBOARD_VIEW: 'dashboard:view',
  DEVICE_CONTROL: 'device:control',
  DEVICE_MANAGE: 'device:manage',
  SCENE_MANAGE: 'scene:manage',
  ENERGY_VIEW: 'energy:view',
  ENERGY_MANAGE: 'energy:manage',
  USER_MANAGE: 'user:manage',
  SYSTEM_SETTING: 'system:setting',
  PROFILE_VIEW: 'profile:view',
  PROFILE_EDIT: 'profile:edit'
}

export const RolePermissions = {
  [Role.ADMIN]: [
    Permission.DASHBOARD_VIEW,
    Permission.DEVICE_CONTROL,
    Permission.DEVICE_MANAGE,
    Permission.SCENE_MANAGE,
    Permission.ENERGY_VIEW,
    Permission.ENERGY_MANAGE,
    Permission.USER_MANAGE,
    Permission.SYSTEM_SETTING,
    Permission.PROFILE_VIEW,
    Permission.PROFILE_EDIT
  ],
  [Role.USER]: [
    Permission.DASHBOARD_VIEW,
    Permission.DEVICE_CONTROL,
    Permission.SCENE_MANAGE,
    Permission.ENERGY_VIEW,
    Permission.PROFILE_VIEW,
    Permission.PROFILE_EDIT
  ]
}

export const hasPermission = (role, permission) => {
  if (!role) return false
  return RolePermissions[role]?.includes(permission) || false
}

export const isAdmin = (role) => role === Role.ADMIN
export const isUser = (role) => role === Role.USER
