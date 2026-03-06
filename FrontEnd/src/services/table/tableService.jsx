import axiosClient from "../../utils/api";
import { TableModel } from "../../models/TableModel";

export const getTables = async () => {
  try {
    const response = await axiosClient.get("/api/v1/tables");
    const rawData = response.data.data;
    if (Array.isArray(rawData)) {
      return rawData.map((item) => new TableModel(item));
    }
    return [];
  } catch (error) {
    throw error.response?.data?.message || "Lỗi lấy danh sách bàn";
  }
};
export const getActiveSessionByTable = async (idTable) => {
  try {
    const response = await axiosClient.get(
      `/api/v1/tables/getActiveSession/${idTable}`,
    );
    return response.data;
  } catch (error) {
    throw error.response?.data?.message;
  }
};
export const createTable = async (data) => {
  try {
    const payload = {
      numberTable: data.numberTable,
      capacity: parseInt(data.capacity),
    };
    const response = await axiosClient.post("/api/v1/tables", payload);
    return new TableModel(response.data.data);
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tạo bàn";
  }
};

export const deleteTable = async (id) => {
  try {
    await axiosClient.delete(`/api/v1/tables/${id}`);
    return true;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi xóa bàn";
  }
};
export const createTokenFromTable = async (tableCode) => {
  try {
    const response = await axiosClient.post("/api/v1/sessions/open", {
      tableCode: tableCode,
    });
    const { accessToken, refreshToken } = response.data.data;
    localStorage.setItem("authToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("currentTable", tableCode);

    return response.data;
  } catch (error) {
    console.error("LỖI GỐC (Đọc cái này):", error);

    throw error.response?.data?.message || "Lỗi không xác định";
  }
};

export const generateTableQr = async (tableName) => {
  try {
    const response = await axiosClient.post(`/api/v1/tables/qr-code`, null, {
      params: {
        idTable: tableName,
      },
    });
    return new TableModel(response.data.data);
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tạo QR Code";
  }
};
