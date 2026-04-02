import request from '@/utils/request'

/**
 * 获取疫苗提醒列表
 * @param {number} petId - 宠物 ID
 */
export const getVaccineReminders = (petId) => {
  return request.get(`/api/reminders/vaccine`, {
    params: { pet_id: petId }
  })
}

/**
 * 获取即将到期的疫苗提醒
 * @param {number} petId - 宠物 ID
 */
export const getUpcomingVaccineReminders = (petId) => {
  return request.get(`/api/pets/${petId}/vaccine-reminders/upcoming`)
}

/**
 * 创建疫苗提醒
 * @param {number} petId - 宠物 ID
 * @param {string} vaccineName - 疫苗名称
 * @param {string} nextDate - 下次接种日期
 * @param {number} vaccineType - 疫苗类型
 * @param {number} reminderDays - 提前几天提醒
 */
export const createVaccineReminder = (petId, vaccineName, nextDate, vaccineType, reminderDays) => {
  return request.post('/api/reminders/vaccine', null, {
    params: { pet_id: petId, vaccine_name: vaccineName, next_date: nextDate, vaccine_type: vaccineType, reminder_days: reminderDays }
  })
}

/**
 * 完成疫苗提醒
 * @param {number} reminderId - 提醒 ID
 */
export const completeVaccineReminder = (reminderId) => {
  return request.post(`/api/reminders/vaccine/${reminderId}/complete`)
}

/**
 * 删除疫苗提醒
 * @param {number} petId - 宠物 ID
 * @param {number} reminderId - 提醒 ID
 */
export const deleteVaccineReminder = (petId, reminderId) => {
  return request.delete(`/api/pets/${petId}/vaccine-reminders/${reminderId}`)
}

/**
 * 获取驱虫提醒列表
 * @param {number} petId - 宠物 ID
 */
export const getParasiteReminders = (petId) => {
  return request.get(`/api/reminders/parasite`, {
    params: { pet_id: petId }
  })
}

/**
 * 创建驱虫提醒
 * @param {number} petId - 宠物 ID
 * @param {number} type - 类型 (1-体内 2-体外)
 * @param {string} productName - 产品名称
 * @param {string} nextDate - 下次驱虫日期
 * @param {number} intervalDays - 间隔天数
 */
export const createParasiteReminder = (petId, type, productName, nextDate, intervalDays) => {
  return request.post('/api/reminders/parasite', null, {
    params: { pet_id: petId, type, product_name: productName, next_date: nextDate, interval_days: intervalDays }
  })
}

/**
 * 完成驱虫提醒
 * @param {number} reminderId - 提醒 ID
 */
export const completeParasiteReminder = (reminderId) => {
  return request.post(`/api/reminders/parasite/${reminderId}/complete`)
}

/**
 * 删除驱虫提醒
 * @param {number} petId - 宠物 ID
 * @param {number} reminderId - 提醒 ID
 */
export const deleteParasiteReminder = (petId, reminderId) => {
  return request.delete(`/api/pets/${petId}/parasite-reminders/${reminderId}`)
}
