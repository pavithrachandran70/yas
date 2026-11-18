import Link from 'next/link';

import { WishlistItem as WishlistItemType } from '@/types/wishlist';

import AddToCartButton from './AddToCartButton';
import styles from './WishlistItem.module.css';

interface WishlistItemProps {
  item: WishlistItemType;
  onRemove: (wishlistItemId: number) => void;
}

export default function WishlistItem({ item, onRemove }: WishlistItemProps) {
  function handleRemoveClick() {
    onRemove(item.wishlistItemId);
  }

  const price = Number.isFinite(item.price) ? item.price : 0;

  return (
    <div className={styles.card}>
      {item.thumbnailUrl ? (
        // Product thumbnails come from the media service and are not limited to next/image hosts.
        // eslint-disable-next-line @next/next/no-img-element
        <img src={item.thumbnailUrl} alt={item.productName} className={styles.thumbnail} />
      ) : (
        <div className={styles.thumbnailPlaceholder}>No Image</div>
      )}

      <div className={styles.content}>
        {item.productSlug ? (
          <Link href={`/products/${item.productSlug}`} className={styles.productName}>
            {item.productName}
          </Link>
        ) : (
          <span className={styles.productName}>{item.productName}</span>
        )}

        <p className={styles.price}>${price.toFixed(2)}</p>

        {!item.isAvailable && (
          <p className={styles.discontinuedMessage}>
            {item.discontinuedMessage || 'This product is no longer available'}
          </p>
        )}

        <div className={styles.buttonGroup}>
          <AddToCartButton wishlistItemId={item.wishlistItemId} isAvailable={item.isAvailable} />

          <button type="button" onClick={handleRemoveClick} className={styles.removeButton}>
            Remove
          </button>
        </div>
      </div>
    </div>
  );
}
