import React from "react";
import { FaShoppingCart, FaArrowLeft, FaBars, FaUser } from "react-icons/fa";

const ClientHeader = ({
  cartCount = 0,
  onBack,
  onMenuToggle,
  userName = "Khách hàng",
}) => {
  return (
    <header className="client-header">
      <div className="header-left">
        {onBack && (
          <button className="header-btn btn-back" onClick={onBack}>
            <FaArrowLeft />
          </button>
        )}

        <div className="header-brand">
          <div className="brand-icon">
            <FaUser />
          </div>
          <div className="brand-info">
            <span className="brand-greeting">Xin chào</span>
            <span className="brand-name">{userName}</span>
          </div>
        </div>
      </div>

      <div className="header-right">
        <button
          className="header-btn btn-cart"
          onClick={() => console.log("Open cart")}
        >
          <FaShoppingCart />
          {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
        </button>
      </div>
    </header>
  );
};

export default ClientHeader;
