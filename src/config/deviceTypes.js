/**
 * 智能家居设备类型配置
 * 根据美的、格力、海尔等品牌设备分类
 */
export const deviceTypeConfig = {
  categories: [
    {
      name: '环境控制',
      icon: 'WindPower',
      devices: ['空调', '新风系统', '空气净化器', '加湿器', '除湿器']
    },
    {
      name: '照明灯光',
      icon: 'Sunny',
      devices: ['吸顶灯', '台灯', '筒灯', '灯带', '灯泡']
    },
    {
      name: '大家电',
      icon: 'Box',
      devices: ['冰箱', '洗衣机', '电视', '热水器', '油烟机', '微波炉']
    },
    {
      name: '厨房电器',
      icon: 'Coffee',
      devices: ['电饭煲', '电压力锅', '破壁机', '饮水机', '咖啡机', '电磁炉']
    },
    {
      name: '清洁电器',
      icon: 'Odometer',
      devices: ['扫地机器人', '吸尘器', '洗碗机', '蒸汽拖把']
    },
    {
      name: '安防监控',
      icon: 'VideoCamera',
      devices: ['摄像头', '智能门锁', '门铃', '猫眼', '报警器']
    },
    {
      name: '传感检测',
      icon: 'Odometer',
      devices: ['温湿度传感器', '烟雾传感器', '燃气传感器', '人体传感器', '光照传感器']
    },
    {
      name: '智能控制',
      icon: 'Setting',
      devices: ['智能开关', '智能插座', '窗帘电机', '晾衣架', '智能马桶']
    }
  ],
  
  keywords: {
    '空调': 'WindPower',
    'ac': 'WindPower',
    'air': 'WindPower',
    '灯': 'Sunny',
    '光': 'Sunny',
    'light': 'Sunny',
    '冰箱': 'Box',
    '冰': 'Box',
    'fridge': 'Box',
    '洗衣机': 'RefreshLeft',
    '洗衣': 'RefreshLeft',
    '热水': 'RefreshRight',
    'heater': 'RefreshRight',
    '扫地': 'Odometer',
    'robot': 'Odometer',
    '清洁': 'Odometer',
    '门锁': 'Lock',
    'lock': 'Lock',
    '摄像': 'VideoCamera',
    'camera': 'VideoCamera',
    '监控': 'VideoCamera',
    '窗帘': 'Crop',
    'curtain': 'Crop',
    '开关': 'Switch',
    'switch': 'Switch',
    '插座': 'Connection',
    'socket': 'Connection',
    '传感': 'DataLine',
    'sensor': 'DataLine',
    '温湿度': 'Odometer',
    '烟': 'Bell',
    '燃气': 'Bell',
    'alarm': 'Bell',
    '电视': 'Monitor',
    'tv': 'Monitor',
    '音': 'Bell',
    '音响': 'Bell',
    'speaker': 'Bell',
    '路由': 'Link',
    'wifi': 'Link',
    '门': 'Key',
    'door': 'Key',
    '窗': 'Crop',
    'window': 'Crop',
    '厨房': 'Coffee',
    '咖啡': 'Coffee',
    'coffee': 'Coffee',
    '安防': 'Lock'
  }
}

export const getDeviceIcon = (device) => {
  const name = (device.name || '').toLowerCase()
  const type = (device.type || '').toUpperCase()
  
  for (const [keyword, icon] of Object.entries(deviceTypeConfig.keywords)) {
    if (name.includes(keyword.toLowerCase())) {
      return icon
    }
  }
  
  return 'Setting'
}
