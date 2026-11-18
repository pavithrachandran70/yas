export interface WishlistItem {
  wishlistItemId: number;
  productId: number;
  productName: string;
  productSlug: string;
  thumbnailUrl: string | null;
  price: number;
  isAvailable: boolean;
  discontinuedMessage: string | null;
}

export interface WishlistItemListResponse {
  items: WishlistItem[];
}
