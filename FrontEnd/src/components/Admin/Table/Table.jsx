import React, { useEffect, useState } from "react";
import {
  getTables,
  createTable,
  deleteTable,
  generateTableQr,
} from "../../../services/table/tableService";
import TableModal from "./TableModal";
import { FaEdit, FaTrash, FaPlus, FaQrcode } from "react-icons/fa";
import "../../../styles/dashboard.css";

const Tables = () => {
  const [tables, setTables] = useState([]);
  const [loading, setLoading] = useState(true);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTable, setSelectedTable] = useState(null);

  const fetchTableData = async () => {
    try {
      setLoading(true);
      const data = await getTables();
      setTables(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTableData();
  }, []);

  // Mở Modal Add
  const handleAddClick = () => {
    setSelectedTable(null);
    setIsModalOpen(true);
  };
  const handleEditClick = (table) => {
    setSelectedTable(table);
    setIsModalOpen(true);
  };

  const handleSaveTable = async (formData) => {
    try {
      if (selectedTable) {
        alert("Chức năng edit đang phát triển");
      } else {
        await createTable(formData);
        alert("Thêm bàn thành công!");
      }
      setIsModalOpen(false);
      fetchTableData();
    } catch (error) {
      alert("Lỗi: " + error);
    }
  };

  // Xử lý Delete
  const handleDelete = async (id) => {
    if (window.confirm("Bạn chắc chắn muốn xóa bàn này?")) {
      try {
        await deleteTable(id);
        fetchTableData();
      } catch (err) {
        alert("Lỗi xóa: " + err);
      }
    }
  };
  const handleGenerateQR = async (table) => {
    if (!window.confirm(`Tạo lại mã QR mới cho "${table.id}"?`)) return;

    try {
      const updatedTable = await generateTableQr(table.id);

      alert("Đã tạo mã QR mới thành công!");
      setTables((prevTables) =>
        prevTables.map((t) => (t.id === updatedTable.id ? updatedTable : t))
      );
    } catch (error) {
      alert("Lỗi tạo QR: " + error);
    }
  };
  if (loading) return <div>Loading Tables...</div>;

  return (
    <div className="admin-content">
      <div className="page-header">
        <h2>Table Management</h2>
        <button className="btn-add" onClick={handleAddClick}>
          <FaPlus /> Add Table
        </button>
      </div>

      <div className="table-container">
        <table className="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Table Name</th>
              <th>Capacity</th>
              <th>Status</th>
              <th>QR Code</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {tables.length > 0 ? (
              tables.map((item) => (
                <tr key={item.id}>
                  <td>#{item.id}</td>
                  <td style={{ fontWeight: "bold" }}>{item.numberTable}</td>
                  <td>{item.capacity} người</td>
                  <td>
                    <span
                      className={`status-badge ${
                        item.isAvailable ? "in-stock" : "out-stock"
                      }`}
                    >
                      {item.statusText}
                    </span>
                  </td>
                  <td>
                    {/* Hiển thị QR Code từ Base64 */}
                    <img
                      src={item.qrCodeImage}
                      alt="QR"
                      style={{
                        width: "60px",
                        height: "60px",
                        border: "1px solid #eee",
                      }}
                    />
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-icon edit"
                        onClick={() => handleEditClick(item)}
                      >
                        <FaEdit />
                      </button>
                      <button
                        className="btn-icon"
                        style={{ color: "#0ea5e9" }}
                        onClick={() => handleGenerateQR(item)}
                        title="Tạo mới QR Code"
                      >
                        <FaQrcode />
                      </button>
                      <button
                        className="btn-icon delete"
                        onClick={() => handleDelete(item.id)}
                      >
                        <FaTrash />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" style={{ textAlign: "center" }}>
                  Chưa có bàn nào
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <TableModal
        isOpen={isModalOpen}
        table={selectedTable}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveTable}
      />
    </div>
  );
};

export default Tables;
