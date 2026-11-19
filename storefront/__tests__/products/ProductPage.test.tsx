import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import ProductDetailsPage from '@/pages/products/[slug]';
import { ProductDetail } from '@/modules/catalog/models/ProductDetail';
import * as RatingService from '@/modules/rating/services/RatingService';
import { toastError } from '@/modules/catalog/services/ToastService';

jest.mock('next/router', () => ({
  useRouter: () => ({
    query: { slug: 'oak-chair' },
    push: jest.fn(),
    asPath: '/products/oak-chair',
    pathname: '/products/[slug]',
  }),
}));

jest.mock('@/modules/rating/services/RatingService', () => ({
  getAverageStarByProductId: jest.fn(),
  getRatingsByProductId: jest.fn(),
  createRating: jest.fn(),
}));

jest.mock('@/modules/catalog/services/ProductService', () => ({
  getProductOptionValueByProductId: jest.fn().mockResolvedValue([]),
  getRelatedProductsByProductId: jest.fn().mockResolvedValue({ productContent: [] }),
  getSimilarProductsByProductId: jest.fn().mockResolvedValue([]),
}));

jest.mock('@/modules/catalog/services/ToastService', () => ({
  toastSuccess: jest.fn(),
  toastError: jest.fn(),
}));

const product: ProductDetail = {
  id: 9,
  name: 'Oak Chair',
  brandName: 'Yas',
  productCategories: ['Furniture'],
  productAttributeGroups: [],
  shortDescription: 'A chair',
  description: '<p>A chair</p>',
  specification: '<p>Wood</p>',
  isAllowedToOrder: true,
  isPublished: true,
  isFeatured: false,
  hasOptions: false,
  price: 120,
  averageStar: 0,
  thumbnailMediaUrl: '',
  productImageMediaUrls: [],
};

const namedReview = {
  id: 1,
  content: 'Solid chair',
  createdBy: 'ada',
  star: 4,
  productId: 9,
  createdOn: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
  lastName: 'Lovelace',
  firstName: 'Ada',
  productName: 'Oak Chair',
};

const anonymousReview = {
  id: 2,
  content: 'No name here',
  createdBy: 'guest',
  star: 5,
  productId: 9,
  createdOn: new Date().toISOString(),
  lastName: '',
  firstName: '',
  productName: 'Oak Chair',
};

describe('Product details reviews', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (RatingService.getAverageStarByProductId as jest.Mock).mockReset();
    (RatingService.getRatingsByProductId as jest.Mock).mockReset();
    (RatingService.createRating as jest.Mock).mockReset();
  });

  test('header and Reviews tab share the loaded count', async () => {
    (RatingService.getAverageStarByProductId as jest.Mock).mockResolvedValue(4.25);
    (RatingService.getRatingsByProductId as jest.Mock).mockResolvedValue({
      ratingList: [anonymousReview, namedReview],
      totalPages: 1,
      totalElements: 2,
    });

    render(
      <ProductDetailsPage
        product={product}
        productOptions={[]}
        productVariations={[]}
        pvid={null}
      />
    );

    expect(await screen.findByText('4.3')).toBeInTheDocument();
    expect(screen.getByText('2 reviews')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Reviews (2 reviews)' })).toBeInTheDocument();
    expect(screen.getByText(/Anonymous/)).toBeInTheDocument();
    expect(screen.getByText(/Ada Lovelace/)).toBeInTheDocument();
    expect(screen.getByText('Solid chair')).toBeInTheDocument();
    expect(RatingService.getRatingsByProductId).toHaveBeenCalledWith(9, 0, 5);
  });

  test('header and Reviews tab show No reviews yet when nothing is loaded', async () => {
    (RatingService.getAverageStarByProductId as jest.Mock).mockResolvedValue(0);
    (RatingService.getRatingsByProductId as jest.Mock).mockResolvedValue({
      ratingList: [],
      totalPages: 0,
      totalElements: 0,
    });

    const { container } = render(
      <ProductDetailsPage
        product={product}
        productOptions={[]}
        productVariations={[]}
        pvid={null}
      />
    );

    expect(await screen.findAllByText('No reviews yet')).toHaveLength(2);
    expect(container.querySelector('.rating-star')).not.toBeInTheDocument();
    expect(screen.queryByText('(0 ratings)')).not.toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Reviews' })).toBeInTheDocument();
    expect(screen.queryByRole('tab', { name: /0/ })).not.toBeInTheDocument();
  });

  test('updates the header and Reviews tab after a successful post without reloading', async () => {
    const user = userEvent.setup();
    const postedReview = {
      ...anonymousReview,
      id: 3,
      content: 'Just posted',
      star: 5,
    };

    const averages = [4, 4, 4.5];
    const ratingPages = [
      {
        ratingList: [namedReview],
        totalPages: 2,
        totalElements: 6,
      },
      {
        ratingList: [namedReview],
        totalPages: 2,
        totalElements: 6,
      },
      {
        ratingList: [postedReview, namedReview],
        totalPages: 2,
        totalElements: 7,
      },
    ];
    (RatingService.getAverageStarByProductId as jest.Mock).mockImplementation(
      () => Promise.resolve(averages.shift())
    );
    (RatingService.getRatingsByProductId as jest.Mock).mockImplementation(
      () => Promise.resolve(ratingPages.shift())
    );
    (RatingService.createRating as jest.Mock).mockResolvedValue(postedReview);

    render(
      <ProductDetailsPage
        product={product}
        productOptions={[]}
        productVariations={[]}
        pvid={null}
      />
    );

    expect(await screen.findByText('4.0')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Reviews (6 reviews)' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Next page' }));
    await waitFor(() => {
      expect(RatingService.getRatingsByProductId).toHaveBeenCalledWith(9, 1, 5);
    });

    await user.type(screen.getByPlaceholderText('Great...'), 'Just posted');
    await user.click(screen.getByRole('button', { name: 'Post' }));

    expect(await screen.findByText('4.5')).toBeInTheDocument();
    expect(screen.getByText('7 reviews')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: 'Reviews (7 reviews)' })).toBeInTheDocument();
    expect(screen.getByText('Just posted')).toBeInTheDocument();
    expect(RatingService.getRatingsByProductId).toHaveBeenLastCalledWith(9, 0, 5);
    expect(screen.getByRole('button', { name: 'Next page' })).toBeInTheDocument();
  });

  test('keeps the review form and shows the existing message when posting is refused', async () => {
    const user = userEvent.setup();
    (RatingService.getAverageStarByProductId as jest.Mock).mockResolvedValue(4);
    (RatingService.getRatingsByProductId as jest.Mock).mockResolvedValue({
      ratingList: [namedReview],
      totalPages: 1,
      totalElements: 1,
    });
    (RatingService.createRating as jest.Mock).mockRejectedValue(403);

    render(
      <ProductDetailsPage
        product={product}
        productOptions={[]}
        productVariations={[]}
        pvid={null}
      />
    );

    await screen.findByText('1 review');
    await user.type(screen.getByPlaceholderText('Great...'), 'Second try');
    await user.click(screen.getByRole('button', { name: 'Post' }));

    await waitFor(() => {
      expect(toastError).toHaveBeenCalledWith(
        'You are not authorized to rate this product since you are not logged in or have not purchased it'
      );
    });
    expect(screen.getByPlaceholderText('Great...')).toHaveValue('Second try');
    expect(screen.getByText('1 review')).toBeInTheDocument();
    expect(RatingService.getRatingsByProductId).toHaveBeenCalledTimes(1);
  });

  test('keeps the review form when the shopper has already reviewed the product', async () => {
    const user = userEvent.setup();
    (RatingService.getAverageStarByProductId as jest.Mock).mockResolvedValue(4);
    (RatingService.getRatingsByProductId as jest.Mock).mockResolvedValue({
      ratingList: [namedReview],
      totalPages: 1,
      totalElements: 1,
    });
    (RatingService.createRating as jest.Mock).mockRejectedValue(409);

    render(
      <ProductDetailsPage
        product={product}
        productOptions={[]}
        productVariations={[]}
        pvid={null}
      />
    );

    await screen.findByText('1 review');
    await user.type(screen.getByPlaceholderText('Great...'), 'Again');
    await user.click(screen.getByRole('button', { name: 'Post' }));

    await waitFor(() => {
      expect(toastError).toHaveBeenCalledWith('You have rated this product before');
    });
    expect(screen.getByPlaceholderText('Great...')).toHaveValue('Again');
  });
});
