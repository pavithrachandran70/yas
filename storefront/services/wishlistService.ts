import { getToken } from '@/services/authService';
import { WishlistItemListResponse } from '@/types/wishlist';

const BASE_URL = '/api/customer/storefront/wishlist';

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

function throwForStatus(response: Response, fallback: string, notFoundMessage?: string): void {
  if (response.ok) {
    return;
  }
  if (response.status === 401 || response.status === 403) {
    throw new Error('UNAUTHORIZED');
  }
  if (response.status === 404 && notFoundMessage) {
    throw new Error(notFoundMessage);
  }
  throw new Error(`${fallback}: ${response.statusText}`);
}

export async function getWishlistItems(): Promise<WishlistItemListResponse> {
  const response = await fetch(`${BASE_URL}/items`, {
    method: 'GET',
    headers: authHeaders(),
  });

  throwForStatus(response, 'Failed to fetch wishlist items');
  return response.json();
}

export async function removeWishlistItem(wishlistItemId: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/items/${wishlistItemId}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });

  throwForStatus(response, 'Failed to remove wishlist item', 'Wishlist item not found');
}

export async function addProductToWishlist(productId: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/items`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ productId }),
  });

  throwForStatus(response, 'Failed to add product to wishlist', 'Product not found');
}
