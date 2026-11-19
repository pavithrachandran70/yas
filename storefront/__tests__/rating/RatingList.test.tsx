import { render, screen } from '@testing-library/react';

import RatingList from '@/modules/rating/components/RatingList';
import { Rating } from '@/modules/rating/models/Rating';

const handlePageChange = jest.fn();

function rating(overrides: Partial<Rating> = {}): Rating {
  return {
    id: 1,
    content: 'Solid chair',
    createdBy: 'user',
    star: 4,
    productId: 9,
    createdOn: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
    lastName: 'Lovelace',
    firstName: 'Ada',
    productName: 'Oak Chair',
    ...overrides,
  };
}

describe('RatingList', () => {
  test('shows No reviews yet when the product has no reviews', () => {
    render(
      <RatingList
        ratingList={[]}
        pageNo={0}
        totalPages={0}
        totalElements={0}
        handlePageChange={handlePageChange}
      />
    );

    expect(screen.getByText('No reviews yet')).toBeInTheDocument();
  });

  test('shows Anonymous and stars when a review has no name', () => {
    const { container } = render(
      <RatingList
        ratingList={[rating({ id: 2, firstName: '', lastName: '', content: 'Quiet review' })]}
        pageNo={0}
        totalPages={1}
        totalElements={1}
        handlePageChange={handlePageChange}
      />
    );

    expect(screen.getByText(/Anonymous/)).toBeInTheDocument();
    expect(container.querySelector('.review-item .star-ratings')).toBeInTheDocument();
  });

  test('shows the reviewer name, stars, text, and relative time', () => {
    const { container } = render(
      <RatingList
        ratingList={[rating()]}
        pageNo={0}
        totalPages={1}
        totalElements={1}
        handlePageChange={handlePageChange}
      />
    );

    expect(screen.getByText(/Ada Lovelace/)).toBeInTheDocument();
    expect(screen.getByText('Solid chair')).toBeInTheDocument();
    expect(screen.getByText(/ago/)).toBeInTheDocument();
    expect(container.querySelector('.review-item .star-ratings')).toBeInTheDocument();
  });

  test('keeps pages of reviews when there is more than one page', () => {
    render(
      <RatingList
        ratingList={[rating()]}
        pageNo={0}
        totalPages={2}
        totalElements={6}
        handlePageChange={handlePageChange}
      />
    );

    expect(screen.getByText('Next')).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeInTheDocument();
  });
});
