import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import WishlistItem from '@/modules/wishlist/components/WishlistItem';
import { WishlistItem as WishlistItemType } from '@/types/wishlist';

describe('WishlistItem', () => {
  const mockOnRemove = jest.fn();

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('testWishlistItem_AvailableProduct - renders available product correctly', () => {
    const item: WishlistItemType = {
      wishlistItemId: 1,
      productId: 101,
      productName: 'Product A',
      productSlug: 'product-a',
      thumbnailUrl: 'https://example.com/a.jpg',
      price: 29.99,
      isAvailable: true,
      discontinuedMessage: null,
    };

    render(<WishlistItem item={item} onRemove={mockOnRemove} />);

    expect(screen.getByText('Product A')).toBeInTheDocument();
    expect(screen.getByText('$29.99')).toBeInTheDocument();
    expect(screen.getByText('Remove')).toBeInTheDocument();
    expect(screen.getByAltText('Product A')).toHaveAttribute('src', item.thumbnailUrl);
    expect(screen.queryByText(/no longer available/i)).not.toBeInTheDocument();
  });

  test('testWishlistItem_DiscontinuedProduct - displays discontinued message (AC-4)', () => {
    const item: WishlistItemType = {
      wishlistItemId: 2,
      productId: 102,
      productName: 'Product B',
      productSlug: 'product-b',
      thumbnailUrl: 'https://example.com/b.jpg',
      price: 49.99,
      isAvailable: false,
      discontinuedMessage: 'This product is no longer available',
    };

    render(<WishlistItem item={item} onRemove={mockOnRemove} />);

    expect(screen.getByText('Product B')).toBeInTheDocument();
    expect(screen.getByText('This product is no longer available')).toBeInTheDocument();
  });

  test('testWishlistItem_RemoveButton - calls onRemove with correct ID', async () => {
    const item: WishlistItemType = {
      wishlistItemId: 1,
      productId: 101,
      productName: 'Product A',
      productSlug: 'product-a',
      thumbnailUrl: 'https://example.com/a.jpg',
      price: 29.99,
      isAvailable: true,
      discontinuedMessage: null,
    };

    render(<WishlistItem item={item} onRemove={mockOnRemove} />);

    await userEvent.click(screen.getByText('Remove'));

    expect(mockOnRemove).toHaveBeenCalledWith(1);
  });
});
