import { Star } from '../../rating/components';
import { formatAverageRating } from '../../rating/reviewSummary';

export interface Props {
  productName: string;
  averageStar: number;
  ratingCount: number | null;
  reviewCountLabel: string | null;
}

export default function DetailHeader({
  productName,
  averageStar,
  ratingCount,
  reviewCountLabel,
}: Props) {
  let ratingSummary = null;
  if (ratingCount === 0) {
    ratingSummary = <span className="rating-empty">No reviews yet</span>;
  } else if (ratingCount !== null && ratingCount > 0 && reviewCountLabel) {
    ratingSummary = (
      <>
        <div className="rating-star">
          <Star star={averageStar} />
        </div>
        <span className="rating-average">{formatAverageRating(averageStar)}</span>
        <span className="rating-count">{reviewCountLabel}</span>
      </>
    );
  }

  return (
    <div className="product-detail-header">
      <div className="left">
        <h4 className="title">{productName}</h4>
        {ratingSummary}
      </div>

      <span>
        <i className="bi bi-bag-heart fs-3 text-danger"></i>
      </span>
    </div>
  );
}
