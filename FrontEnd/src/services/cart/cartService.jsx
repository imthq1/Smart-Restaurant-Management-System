// Key để lưu trong localStorage
const CART_KEY = "restaurant_cart";

export const getCart = () => {
  const cartJson = localStorage.getItem(CART_KEY);
  return cartJson ? JSON.parse(cartJson) : [];
};

export const saveCart = (cart) => {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
  window.dispatchEvent(new Event("storage"));
};

export const addToCart = (product) => {
  const cart = getCart();
  const existingItemIndex = cart.findIndex((item) => item.id === product.id);

  if (existingItemIndex > -1) {
    cart[existingItemIndex].quantity += 1;
  } else {
    cart.push({
      ...product,
      quantity: 1,
      note: "",
    });
  }
  saveCart(cart);
};

export const updateQuantity = (productId, delta) => {
  let cart = getCart();
  const index = cart.findIndex((item) => item.id === productId);

  if (index > -1) {
    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
      cart.splice(index, 1);
    }
    saveCart(cart);
  }
  return cart;
};

export const updateNote = (productId, newNote) => {
  let cart = getCart();
  const index = cart.findIndex((item) => item.id === productId);

  if (index > -1) {
    cart[index].note = newNote;
    saveCart(cart);
  }
};

export const clearCart = () => {
  localStorage.removeItem(CART_KEY);
  window.dispatchEvent(new Event("storage"));
};
