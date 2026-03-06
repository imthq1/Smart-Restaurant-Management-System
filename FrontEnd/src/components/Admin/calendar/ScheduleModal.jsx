import React, { useState, useEffect } from "react";
import { FaTimes, FaSave } from "react-icons/fa";
import "../../../styles/schedule-modal.css"; 

const ScheduleModal = ({ isOpen, onClose, onSave, initialDate }) => {
  const [formData, setFormData] = useState({
    title: "",
    type: "EVENT", 
    startTime: "",
    endTime: "",
    location: "",
    note: "",
    status: "PLANNED",
    scope: "RESTAURANT"
  });

  useEffect(() => {
    if (isOpen && initialDate) {
      const dateStr = initialDate.toISOString().split("T")[0];
      
      setFormData(prev => ({
        ...prev,
        startTime: `${dateStr}T09:00`,
        endTime: `${dateStr}T10:00`
      }));
    }
  }, [isOpen, initialDate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (new Date(formData.startTime) >= new Date(formData.endTime)) {
      alert("Thời gian kết thúc phải sau thời gian bắt đầu!");
      return;
    }
    onSave(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Tạo lịch trình mới</h3>
          <button className="btn-close" onClick={onClose}><FaTimes /></button>
        </div>

        <form onSubmit={handleSubmit} className="modal-body">
          <div className="form-group">
            <label>Tiêu đề sự kiện <span className="req">*</span></label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="Ví dụ: Họp đầu tuần..."
              required
            />
          </div>

          <div className="form-group">
            <label>Loại sự kiện</label>
            <select name="type" value={formData.type} onChange={handleChange}>
              <option value="EVENT">Sự kiện (Event)</option>
              <option value="MENU_UPDATE">Cập nhật Menu</option>
              <option value="TRAINING">Đào tạo (Training)</option>
              <option value="MEETING">Họp (Meeting)</option>
            </select>
          </div>

          <div className="form-row">
            <div className="form-group col">
              <label>Bắt đầu</label>
              <input
                type="datetime-local"
                name="startTime"
                value={formData.startTime}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group col">
              <label>Kết thúc</label>
              <input
                type="datetime-local"
                name="endTime"
                value={formData.endTime}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Địa điểm</label>
            <input
              type="text"
              name="location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Vd: Sảnh chính, Phòng họp..."
            />
          </div>
          <div className="form-group">
            <label>Ghi chú</label>
            <textarea
              name="note"
              rows="3"
              value={formData.note}
              onChange={handleChange}
              placeholder="Chi tiết nội dung..."
            ></textarea>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn-cancel" onClick={onClose}>Hủy bỏ</button>
            <button type="submit" className="btn-save"><FaSave /> Lưu lịch trình</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ScheduleModal;