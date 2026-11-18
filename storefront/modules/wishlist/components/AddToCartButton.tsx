import { useState } from 'react';

import { useCartContext } from '@/context/CartContext';
import { toastError, toastSuccess } from '@/modules/catalog/services/ToastService';
import { addWishlistItemToCart } from '@/services/cartService';

import styles from './AddToCartButton.module.css';

interface AddToCartButtonProps {
  wishlistItemId: number;
  isAvailable: boolean;
}

export default function AddToCartButton({ wishlistItemId, isAvailable }: AddToCartButtonProps) {
  const [loading, setLoading] = useState(false);
  const { fetchNumberCartItems } = useCartContext();

  async function handleClick() {
    if (!isAvailable || loading) {
      return;
    }

    setLoading(true);
    try {
      await addWishlistItemToCart(wishlistItemId);
      toastSuccess('Added to cart');
      fetchNumberCartItems();
    } catch (err: unknown) {
      if (err instanceof Error && err.message === 'PRODUCT_NO_LONGER_AVAILABLE') {
        toastError('Product is no longer available');
      } else if (err instanceof Error && err.message === 'UNAUTHORIZED') {
        toastError('Authentication required');
      } else {
        toastError(err instanceof Error && err.message ? err.message : 'Failed to add to cart');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={!isAvailable || loading}
      className={isAvailable ? styles.addToCartButton : styles.disabledButton}
    >
      {!isAvailable ? 'Unavailable' : loading ? 'Adding...' : 'Add to Cart'}
    </button>
  );
}
