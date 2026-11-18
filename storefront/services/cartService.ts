import { CartItemGetVm } from '@/modules/cart/models/CartItemGetVm';
import { getToken } from '@/services/authService';

const BASE_URL = '/api/cart/storefront';

function authHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

export async function addWishlistItemToCart(wishlistItemId: number): Promise<CartItemGetVm> {
  const response = await fetch(`${BASE_URL}/items/from-wishlist`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ wishlistItemId }),
  });

  if (!response.ok) {
    if (response.status === 400) {
      const error = await response.json();
      if (typeof error?.detail === 'string' && error.detail.toLowerCase().includes('no longer available')) {
        throw new Error('PRODUCT_NO_LONGER_AVAILABLE');
      }
    }
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED');
    }
    if (response.status === 403) {
      throw new Error('ACCESS_DENIED');
    }
    if (response.status === 404) {
      throw new Error('Wishlist item not found');
    }
    throw new Error(`Failed to add wishlist item to cart: ${response.statusText}`);
  }

  return response.json();
}
