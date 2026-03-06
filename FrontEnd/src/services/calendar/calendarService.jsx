import axiosClient from "../../utils/api";
export const getSchedules = async (from, to) => {
  try {
    const response = await axiosClient.get(`/api/v1/calendars`, {
      params: { from, to },
    });
    return response.data.data;
  } catch (error) {
    console.error("Lỗi lấy lịch:", error);
    return [];
  }
};

export const createSchedule = async (scheduleData) => {
  try {
    const response = await axiosClient.post(`/api/v1/calendars`, scheduleData);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tạo lịch trình";
  }
};
export const getScheduleStats = async (from, to) => {
  try {
    const response = await axiosClient.get(`/api/v1/calendars/stats/type`, {
      params: { from, to },
    });

    return response.data.data;
  } catch (error) {
    console.error("Lỗi lấy thống kê:", error);
    return [];
  }
};
