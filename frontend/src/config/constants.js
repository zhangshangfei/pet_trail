/**
 * 常量配置
 */

// API 相关
export const API = {
  TIMEOUT: 30000,
  RETRY_COUNT: 1
}

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'token',
  TOKEN_EXPIRE_TIME: 'tokenExpireTime',
  USER_INFO: 'userInfo',
  PET_INFO: 'petInfo'
}

// 性别映射
export const GENDER_MAP = {
  0: '未知',
  1: '公',
  2: '母'
}

// 宠物类别映射
export const PET_CATEGORY_MAP = {
  0: '其他',
  1: '猫',
  2: '狗'
}

// 疫苗/驱虫状态
export const REMINDER_STATUS = {
  PENDING: 0,
  COMPLETED: 1
}

// 状态文本映射
export const STATUS_TEXT_MAP = {
  vaccine: {
    0: '未接种',
    1: '已接种'
  },
  parasite: {
    0: '未完成',
    1: '已完成'
  }
}

// 日期格式
export const DATE_FORMAT = {
  NORMAL: 'YYYY-MM-DD',
  WITH_TIME: 'YYYY-MM-DD HH:mm:ss',
  DISPLAY: 'YYYY 年 MM 月 DD 日'
}

// 默认分页配置
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  DEFAULT_PAGE_NUM: 1,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100]
}

// 默认头像
export const DEFAULT_AVATAR = '/static/images/default-avatar.png'

// 默认宠物头像
export const DEFAULT_PET_AVATAR = '/static/images/default-pet-avatar.png'
