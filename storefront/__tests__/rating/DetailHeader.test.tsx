import { render, screen } from '@testing-library/react';

import DetailHeader from '@/modules/catalog/components/DetailHeader';
import { formatReviewCount } from '@/modules/rating/reviewSummary';

describe('DetailHeader rating summary', () => {
  test('shows stars, one-decimal average, and singular count', () => {
    const { container } = render(
      <DetailHeader
        productName="Oak Chair"
        averageStar={4}
        ratingCount={1}
        reviewCountLabel={formatReviewCount(1)}
      />
    );

    expect(screen.getByText('Oak Chair')).toBeInTheDocument();
    expect(screen.getByText('4.0')).toBeInTheDocument();
    expect(screen.getByText('1 review')).toBeInTheDocument();
    expect(container.querySelector('.rating-star')).toBeInTheDocument();
  });

  test('shows plural count and rounds the average to one decimal', () => {
    render(
      <DetailHeader
        productName="Oak Chair"
        averageStar={4.25}
        ratingCount={3}
        reviewCountLabel={formatReviewCount(3)}
      />
    );

    expect(screen.getByText('4.3')).toBeInTheDocument();
    expect(screen.getByText('3 reviews')).toBeInTheDocument();
    expect(screen.queryByText('4.25')).not.toBeInTheDocument();
  });

  test('shows No reviews yet without stars or a zero count', () => {
    const { container } = render(
      <DetailHeader
        productName="Oak Chair"
        averageStar={0}
        ratingCount={0}
        reviewCountLabel={null}
      />
    );

    expect(screen.getByText('No reviews yet')).toBeInTheDocument();
    expect(container.querySelector('.rating-star')).not.toBeInTheDocument();
    expect(screen.queryByText('(0 ratings)')).not.toBeInTheDocument();
    expect(screen.queryByText('0 reviews')).not.toBeInTheDocument();
  });
});
