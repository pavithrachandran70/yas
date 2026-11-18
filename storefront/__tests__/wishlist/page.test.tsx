import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import WishlistPage from '@/pages/wishlist';
import * as authService from '@/services/authService';
import * as wishlistService from '@/services/wishlistService';

jest.mock('@/services/wishlistService');
jest.mock('@/services/authService');
jest.mock('@/modules/catalog/services/ToastService', () => ({
  toastSuccess: jest.fn(),
  toastError: jest.fn(),
}));

const availableItem = {
  wishlistItemId: 1,
  productId: 101,
  productName: 'Product A',
  productSlug: 'product-a',
  thumbnailUrl: 'https://example.com/a.jpg',
  price: 29.99,
  isAvailable: true,
  discontinuedMessage: null,
};

describe('WishlistPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.refreshAuthentication as jest.Mock).mockResolvedValue(true);
  });

  test('testWishlistPage_GuestRedirect - redirects guest to login (AC-5)', async () => {
    const mockRedirectToLogin = jest.fn();
    (authService.isAuthenticated as jest.Mock).mockReturnValue(false);
    (authService.refreshAuthentication as jest.Mock).mockResolvedValue(false);
    (authService.redirectToLogin as jest.Mock).mockImplementation(mockRedirectToLogin);

    render(<WishlistPage />);

    await waitFor(() => {
      expect(mockRedirectToLogin).toHaveBeenCalledWith('/wishlist');
    });
  });

  test('testWishlistPage_LoadsItems - renders items for signed-in user', async () => {
    (wishlistService.getWishlistItems as jest.Mock).mockResolvedValue({
      items: [
        availableItem,
        {
          wishlistItemId: 2,
          productId: 102,
          productName: 'Product B',
          productSlug: 'product-b',
          thumbnailUrl: 'https://example.com/b.jpg',
          price: 49.99,
          isAvailable: true,
          discontinuedMessage: null,
        },
      ],
    });

    render(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Product A')).toBeInTheDocument();
      expect(screen.getByText('Product B')).toBeInTheDocument();
    });
  });

  test('testWishlistPage_EmptyState - shows empty message when no items', async () => {
    (wishlistService.getWishlistItems as jest.Mock).mockResolvedValue({ items: [] });

    render(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Your wishlist is empty')).toBeInTheDocument();
      expect(screen.getByText('Continue Shopping')).toBeInTheDocument();
    });
  });

  test('testWishlistPage_RemoveItem - removes item from list on delete (AC-1)', async () => {
    (wishlistService.getWishlistItems as jest.Mock).mockResolvedValue({
      items: [availableItem],
    });
    (wishlistService.removeWishlistItem as jest.Mock).mockResolvedValue(undefined);

    render(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Product A')).toBeInTheDocument();
    });

    await userEvent.click(screen.getByText('Remove'));

    await waitFor(() => {
      expect(wishlistService.removeWishlistItem).toHaveBeenCalledWith(1);
      expect(screen.queryByText('Product A')).not.toBeInTheDocument();
    });
  });

  test('testWishlistPage_ErrorState - shows error message and retry button', async () => {
    (wishlistService.getWishlistItems as jest.Mock).mockRejectedValue(new Error('Network error'));

    render(<WishlistPage />);

    await waitFor(() => {
      expect(screen.getByText('Network error')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });
  });
});
