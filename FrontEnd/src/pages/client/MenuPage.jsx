import React, { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  getProducts,
  getCategories,
  getProductsWith,
} from "../../services/menu/menuService";
import ClientHeader from "../../components/Client/ClientHeader";
import ClientSidebar from "../../components/Client/ClientSidebar";
import "../../styles/client-header.css";
import "../../styles/menu-page.css";
import { FaPlus, FaSearch } from "react-icons/fa";
import { createTokenFromTable } from "../../services/table/tableService";

const MenuPage = () => {
  const [searchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);

  const [activeCategoryId, setActiveCategoryId] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);
  const [isSessionReady, setIsSessionReady] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    const initSession = async () => {
      const code = searchParams.get("table");
      console.log("id", code);
      if (code) {
        try {
          await createTokenFromTable(code);
          console.log("Đã vào bàn thành công!");
          setIsSessionReady(true);
        } catch (err) {
          console.error("Lỗi vào bàn:", err);
        }
      } else {
        setIsSessionReady(true);
      }
    };
    initSession();
  }, []);

  useEffect(() => {
    const fetchCats = async () => {
      try {
        const data = await getCategories();
        setCategories(data);
      } catch (e) {
        console.error("Lỗi lấy danh mục:", e);
      }
    };
    fetchCats();
  }, []);

  useEffect(() => {
    if (!isSessionReady) return;

    const fetchProducts = async () => {
      setLoading(true);
      try {
        const params = {
          availableOnly: true,
        };

        if (activeCategoryId) params.categoryId = activeCategoryId;
        if (searchTerm.trim() !== "") params.search = searchTerm.trim();

        console.log("Calling Product API params:", params);

        const data = await getProductsWith(params);
        setProducts(data);
      } catch (e) {
        console.error("Lỗi lấy sản phẩm:", e);
      } finally {
        setLoading(false);
      }
    };

    const timeoutId = setTimeout(() => {
      fetchProducts();
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [activeCategoryId, searchTerm, isSessionReady]);

  const handleAddToCart = (item) => {
    alert(`Đã thêm ${item.name} vào giỏ!`);
  };

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const handleCategorySelect = (catId) => {
    setActiveCategoryId(catId);
    // Đóng sidebar trên mobile sau khi chọn
    if (window.innerWidth <= 768) {
      setSidebarOpen(false);
    }
  };

  return (
    <div className="client-layout">
      <ClientHeader
        cartCount={0}
        userName="Khách hàng"
        onMenuToggle={toggleSidebar}
      />

      {/* Overlay cho mobile */}
      <div
        className={`sidebar-overlay ${sidebarOpen ? "show" : ""}`}
        onClick={() => setSidebarOpen(false)}
      />

      <div className="client-body">
        {/* Sidebar Danh mục */}
        <ClientSidebar
          categories={categories}
          activeCatId={activeCategoryId}
          onSelectCategory={handleCategorySelect}
          isOpen={sidebarOpen}
        />

        {/* Nội dung chính */}
        <main className="menu-list">
          {/* Welcome Section */}
          <div className="welcome-section">
            <div className="welcome-greeting">Xin chào</div>
            <h1 className="welcome-title">Hôm nay bạn muốn ăn gì?</h1>
          </div>

          {/* Thanh tìm kiếm */}
          <div className="menu-search">
            <FaSearch className="search-icon" />
            <input
              type="text"
              className="search-input"
              placeholder="Tìm kiếm món ăn..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* Featured Banner - Optional */}
          {!searchTerm && !activeCategoryId && (
            <div className="featured-banner">
              <div className="banner-content">
                <h2>Khuyến mãi hôm nay</h2>
                <p>Giảm giá đến 30% cho các món ăn được chọn</p>
              </div>
              <img
                src="https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400"
                alt="Featured"
                className="banner-image"
                onError={(e) => (e.target.style.display = "none")}
              />
            </div>
          )}

          {/* Section Header */}
          <div className="section-header">
            <h2 className="section-title">
              {activeCategoryId
                ? categories.find((c) => c.id === activeCategoryId)?.name ||
                  "Thực đơn"
                : searchTerm
                ? "Kết quả tìm kiếm"
                : "Tất cả món ăn"}
            </h2>
            <button className="view-all-btn">Xem tất cả</button>
          </div>

          {/* Grid sản phẩm */}
          {loading ? (
            <div className="loading-state">Đang tải món ngon...</div>
          ) : (
            <div className="product-grid-client">
              {products.length > 0 ? (
                products.map((item) => (
                  <div className="product-card-client" key={item.id}>
                    <div className="product-image-container">
                      <img
                        src={item.image}
                        alt={item.name}
                        className="p-img"
                        onError={(e) =>
                          (e.target.src =
                            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400")
                        }
                      />
                      {item.isNew && <span className="product-badge">Mới</span>}
                    </div>
                    <div className="p-details">
                      <div className="p-name">{item.name}</div>
                      <div className="p-desc">
                        {item.description && item.description.length > 50
                          ? item.description.substring(0, 50) + "..."
                          : item.description || "Món ăn ngon tuyệt vời"}
                      </div>
                      <div className="p-price">
                        <span className="price-amount">
                          {item.formattedPrice}
                        </span>
                        <button
                          className="btn-add-cart"
                          onClick={() => handleAddToCart(item)}
                        >
                          <FaPlus size={14} />
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="empty-state">
                  <p>Không tìm thấy món ăn phù hợp</p>
                </div>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default MenuPage;
