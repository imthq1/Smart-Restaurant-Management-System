import React from "react";
import { FaChevronRight } from "react-icons/fa";

const ClientSidebar = ({
  categories,
  activeCatId,
  onSelectCategory,
  isOpen = false,
}) => {
  return (
    <aside className={`client-sidebar ${isOpen ? "show" : ""}`}>
      <div className="sidebar-title">Danh mục món ăn</div>
      <div className="sidebar-subtitle">Chọn để xem thực đơn</div>

      {/* All Categories Option */}
      <div
        className={`category-item ${!activeCatId ? "active" : ""}`}
        onClick={() => onSelectCategory(null)}
      >
        <img
          src="https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100"
          alt="Tất cả"
          className="category-image"
          onError={(e) => (e.target.src = "https://placehold.co/60")}
        />
        <div className="category-info">
          <div className="category-name">Tất cả món</div>
          <div className="category-count">Xem tất cả</div>
        </div>
        <FaChevronRight className="category-arrow" />
      </div>

      {/* Category List */}
      {categories.map((cat) => (
        <div
          key={cat.id}
          className={`category-item ${activeCatId === cat.id ? "active" : ""}`}
          onClick={() => onSelectCategory(cat.id)}
        >
          <img
            src={
              cat.image ||
              "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100"
            }
            alt={cat.name}
            className="category-image"
            onError={(e) => (e.target.src = "https://placehold.co/60")}
          />
          <div className="category-info">
            <div className="category-name">{cat.name}</div>
            <div className="category-count">{cat.productCount || 12} Món</div>
          </div>
          <FaChevronRight className="category-arrow" />
        </div>
      ))}
    </aside>
  );
};

export default ClientSidebar;
