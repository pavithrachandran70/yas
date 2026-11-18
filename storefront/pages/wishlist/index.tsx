import Head from 'next/head';
import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';

import { isAuthenticated, redirectToLogin, refreshAuthentication } from '@/services/authService';
import { getWishlistItems, removeWishlistItem } from '@/services/wishlistService';
import { toastError, toastSuccess } from '@/modules/catalog/services/ToastService';
import { WishlistItem as WishlistItemType } from '@/types/wishlist';
import WishlistItem from '@/modules/wishlist/components/WishlistItem';

import styles from './wishlist.module.css';

function messageFrom(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export default function WishlistPage() {
  const [items, setItems] = useState<WishlistItemType[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchWishlistItems = useCallback(async () => {
    setLoading(true);
    setError(null);
    let redirecting = false;

    try {
      const response = await getWishlistItems();
      setItems(response.items ?? []);
    } catch (err: unknown) {
      if (err instanceof Error && err.message === 'UNAUTHORIZED') {
        redirecting = true;
        redirectToLogin('/wishlist');
        return;
      }
      setError(messageFrom(err, 'Failed to load wishlist items'));
    } finally {
      if (!redirecting) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    let active = true;

    async function init() {
      const signedIn = isAuthenticated() || (await refreshAuthentication());
      if (!active) {
        return;
      }
      if (!signedIn) {
        redirectToLogin('/wishlist');
        return;
      }
      await fetchWishlistItems();
    }

    init();
    return () => {
      active = false;
    };
  }, [fetchWishlistItems]);

  async function handleRemoveItem(wishlistItemId: number) {
    try {
      await removeWishlistItem(wishlistItemId);
      setItems((prevItems) => prevItems.filter((item) => item.wishlistItemId !== wishlistItemId));
      toastSuccess('Item removed from wishlist');
    } catch (err: unknown) {
      if (err instanceof Error && err.message === 'UNAUTHORIZED') {
        redirectToLogin('/wishlist');
        return;
      }
      toastError(messageFrom(err, 'Failed to remove item'));
    }
  }

  if (loading) {
    return (
      <div className={styles.container}>
        <Head>
          <title>My Wishlist</title>
        </Head>
        <h1 className={styles.title}>My Wishlist</h1>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <Head>
          <title>My Wishlist</title>
        </Head>
        <h1 className={styles.title}>My Wishlist</h1>
        <div className={styles.error}>
          <p>{error}</p>
          <button type="button" onClick={fetchWishlistItems} className={styles.retryButton}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className={styles.container}>
        <Head>
          <title>My Wishlist</title>
        </Head>
        <h1 className={styles.title}>My Wishlist</h1>
        <div className={styles.emptyState}>
          <p>Your wishlist is empty</p>
          <Link href="/" className={styles.continueShoppingLink}>
            Continue Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <Head>
        <title>My Wishlist</title>
      </Head>
      <h1 className={styles.title}>My Wishlist</h1>
      <div className={styles.itemsGrid}>
        {items.map((item) => (
          <WishlistItem key={item.wishlistItemId} item={item} onRemove={handleRemoveItem} />
        ))}
      </div>
    </div>
  );
}
