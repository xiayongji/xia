const GEOCODING_BASE = 'https://geocoding-api.open-meteo.com/v1'
const WEATHER_BASE = 'https://api.open-meteo.com/v1'

function weatherCodeToText(code) {
  const map = {
    0: '晴天', 1: '大部晴朗', 2: '多云', 3: '阴天',
    45: '雾', 48: '雾凇',
    51: '小毛毛雨', 53: '毛毛雨', 55: '大毛毛雨',
    61: '小雨', 63: '中雨', 65: '大雨',
    71: '小雪', 73: '中雪', 75: '大雪',
    80: '阵雨', 81: '中阵雨', 82: '大阵雨',
    95: '雷暴', 96: '雷暴伴冰雹', 99: '强雷暴伴冰雹'
  }
  return map[code] || '未知'
}

function weatherCodeToIcon(code) {
  if (code === 0 || code === 1) return 'Sunny'
  if (code === 2) return 'PartlyCloudy'
  if (code === 3 || code === 45 || code === 48) return 'Cloudy'
  if ((code >= 51 && code <= 65) || (code >= 80 && code <= 82)) return 'Drizzling'
  if ((code >= 71 && code <= 75)) return 'Drizzling'
  if (code >= 95) return 'Lightning'
  return 'Sunny'
}

const cityCoordinates = {
  '北京': { lat: 39.9042, lon: 116.4074 },
  '上海': { lat: 31.2304, lon: 121.4737 },
  '广州': { lat: 23.1291, lon: 113.2644 },
  '深圳': { lat: 22.5431, lon: 114.0579 },
  '杭州': { lat: 30.2741, lon: 120.1551 },
  '成都': { lat: 30.5728, lon: 104.0668 },
  '重庆': { lat: 29.4316, lon: 106.9123 },
  '武汉': { lat: 30.5928, lon: 114.3055 },
  '南京': { lat: 32.0603, lon: 118.7969 },
  '西安': { lat: 34.3416, lon: 108.9398 },
  '天津': { lat: 39.1257, lon: 117.1638 },
  '苏州': { lat: 31.2990, lon: 120.5853 },
  '长沙': { lat: 28.2282, lon: 112.9388 },
  '郑州': { lat: 34.7466, lon: 113.6254 },
  '济南': { lat: 36.6512, lon: 117.1201 },
  '青岛': { lat: 36.0671, lon: 120.3826 },
  '大连': { lat: 38.9140, lon: 121.6147 },
  '厦门': { lat: 24.4798, lon: 118.0894 },
  '福州': { lat: 26.0745, lon: 119.2965 },
  '合肥': { lat: 31.8206, lon: 117.2272 },
  '昆明': { lat: 25.0389, lon: 102.7183 },
  '贵阳': { lat: 26.6470, lon: 106.6302 },
  '南宁': { lat: 22.8170, lon: 108.3665 },
  '哈尔滨': { lat: 45.8038, lon: 126.5350 },
  '长春': { lat: 43.8171, lon: 125.3235 },
  '沈阳': { lat: 41.8057, lon: 123.4315 },
  '石家庄': { lat: 38.0428, lon: 114.5149 },
  '太原': { lat: 37.8706, lon: 112.5489 },
  '呼和浩特': { lat: 40.8424, lon: 111.7490 },
  '兰州': { lat: 36.0611, lon: 103.8343 },
  '西宁': { lat: 36.6171, lon: 101.7782 },
  '乌鲁木齐': { lat: 43.8256, lon: 87.6168 },
  '拉萨': { lat: 29.6500, lon: 91.1000 },
  '银川': { lat: 38.4860, lon: 106.2325 },
  '海口': { lat: 20.0440, lon: 110.1999 },
  '三亚': { lat: 18.2528, lon: 109.5120 }
}

export async function getWeatherByCity(cityName) {
  let lat, lon
  if (cityCoordinates[cityName]) {
    lat = cityCoordinates[cityName].lat
    lon = cityCoordinates[cityName].lon
  } else {
    try {
      const resp = await fetch(`${GEOCODING_BASE}/search?name=${encodeURIComponent(cityName)}&count=1&language=zh`)
      const data = await resp.json()
      if (data.results?.length) {
        lat = data.results[0].latitude
        lon = data.results[0].longitude
      } else {
        throw new Error('城市未找到')
      }
    } catch {
      throw new Error('城市查询失败')
    }
  }

  try {
    const resp = await fetch(
      `${WEATHER_BASE}/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&timezone=Asia/Shanghai`
    )
    const data = await resp.json()
    const current = data.current
    return {
      city: cityName,
      temperature: Math.round(current.temperature_2m),
      humidity: current.relative_humidity_2m,
      windSpeed: current.wind_speed_10m,
      weatherCode: current.weather_code,
      weatherText: weatherCodeToText(current.weather_code),
      weatherIcon: weatherCodeToIcon(current.weather_code)
    }
  } catch {
    throw new Error('天气数据获取失败')
  }
}

export async function getWeatherByCoords(lat, lon) {
  try {
    const reverseResp = await fetch(
      `${GEOCODING_BASE}/search?name=&count=1&language=zh&latitude=${lat}&longitude=${lon}&count=1`
    )
    let cityName = '当前城市'
    try {
      const geoData = await reverseResp.json()
      if (geoData.results?.length) {
        cityName = geoData.results[0].name || geoData.results[0].admin1 || '当前城市'
      }
    } catch { /* fallback */ }

    const resp = await fetch(
      `${WEATHER_BASE}/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&timezone=Asia/Shanghai`
    )
    const data = await resp.json()
    const current = data.current
    return {
      city: cityName,
      temperature: Math.round(current.temperature_2m),
      humidity: current.relative_humidity_2m,
      windSpeed: current.wind_speed_10m,
      weatherCode: current.weather_code,
      weatherText: weatherCodeToText(current.weather_code),
      weatherIcon: weatherCodeToIcon(current.weather_code)
    }
  } catch {
    throw new Error('天气数据获取失败')
  }
}

export { weatherCodeToIcon, weatherCodeToText }