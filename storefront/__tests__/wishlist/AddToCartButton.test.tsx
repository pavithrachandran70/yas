import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import AddToCartButton from '@/modules/wishlist/components/AddToCartButton';
import { toastError, toastSuccess } from '@/modules/catalog/services/ToastService';
import { addWishlistItemToCart } from '@/services/cartService';

jest.mock('@/services/cartService');
jest.mock('@/modules/catalog/services/ToastService', () => ({
  toastSuccess: jest.fn(),
  toastError: jest.fn(),
}));

describe('AddToCartButton', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('testAddToCartButton_AvailableProduct - calls the cart API for an available product', async () => {
    (addWishlistItemToCart as jest.Mock).mockResolvedValue({
      customerId: 'customer',
      productId: 1,
      quantity: 1,
    });

    render(<AddToCartButton wishlistItemId={1} isAvailable />);

    const button = screen.getByRole('button', { name: 'Add to Cart' });
    expect(button).toBeEnabled();

    await userEvent.click(button);

    expect(addWishlistItemToCart).toHaveBeenCalledWith(1);
    expect(toastSuccess).toHaveBeenCalledWith('Added to cart');
    expect(toastError).not.toHaveBeenCalled();
  });

  test('testAddToCartButton_DisabledForDiscontinued - prevents add to cart (AC-4)', () => {
    render(<AddToCartButton wishlistItemId={2} isAvailable={false} />);

    const button = screen.getByRole('button', { name: 'Unavailable' });
    expect(button).toBeDisabled();
    expect(addWishlistItemToCart).not.toHaveBeenCalled();
  });
});
