import React, { useState, useEffect } from "react";
import "../../../styles/dashboard.css";

const TableModal = ({ table, isOpen, onClose, onSave }) => {
  const initialFormState = {
    id: "",
    numberTable: "",
    capacity: 4,
  };

  const [formData, setFormData] = useState(initialFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Check chế độ Add hay Edit
  const isEditMode = !!table;

  useEffect(() => {
    if (isOpen) {
      if (isEditMode) {
        // Fill data cũ nếu edit
        setFormData({
          id: table.id,
          numberTable: table.numberTable,
          capacity: table.capacity,
        });
      } else {
        // Reset form nếu add
        setFormData(initialFormState);
      }
    }
  }, [isOpen, table]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await onSave(formData);
    } catch (error) {
      alert("Lỗi: " + error);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content" style={{ width: "400px" }}>
        <div className="modal-header">
          <h3>{isEditMode ? "Edit Table" : "Add New Table"}</h3>
          <button onClick={onClose} className="btn-close">
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Tên bàn / Số bàn</label>
            <input
              type="text"
              name="numberTable"
              value={formData.numberTable}
              onChange={handleChange}
              placeholder="Ví dụ: Bàn số 1"
              required
            />
          </div>

          <div className="form-group">
            <label>Sức chứa (Người)</label>
            <input
              type="number"
              name="capacity"
              value={formData.capacity}
              onChange={handleChange}
              min="1"
              required
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-cancel">
              Cancel
            </button>
            <button type="submit" className="btn-save" disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : "Save"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TableModal;
