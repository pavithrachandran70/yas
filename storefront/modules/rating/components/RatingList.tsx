import Moment from 'react-moment';
import ReactPaginate from 'react-paginate';
import { Rating } from '../models/Rating';
import Star from './Star';

export interface Props {
  ratingList: Rating[] | null;
  pageNo: number;
  totalPages: number;
  totalElements: number | null;
  handlePageChange: (selectedItem: { selected: number }) => void;
}

function reviewerName(rating: Rating): string {
  const firstName = rating.firstName?.trim() ?? '';
  const lastName = rating.lastName?.trim() ?? '';
  if (!firstName && !lastName) {
    return 'Anonymous';
  }
  return `${firstName} ${lastName}`.trim();
}

export default function RatingList({
  ratingList,
  pageNo,
  totalPages,
  totalElements,
  handlePageChange,
}: Props) {
  if (totalElements == null) {
    return null;
  }

  return (
    <>
      {totalElements === 0 ? (
        <>No reviews yet</>
      ) : (
        <>
          {ratingList?.map((rating: Rating) => (
            <div className="review-item" key={rating.id}>
              <div style={{ fontWeight: 'bold' }}>
                {reviewerName(rating)}
                <span className="ms-2">
                  <Star star={rating.star} />
                </span>
              </div>
              <div className="d-flex justify-content-between">
                <p className="mx-2">{rating.content}</p>
                <p className="mx-5">
                  <Moment fromNow ago>
                    {rating.createdOn}
                  </Moment>{' '}
                  ago
                </p>
              </div>
            </div>
          ))}
          {/* PAGINATION */}
          {totalPages > 1 && (
            <ReactPaginate
              forcePage={pageNo}
              previousLabel={'Previous'}
              nextLabel={'Next'}
              pageCount={totalPages}
              onPageChange={handlePageChange}
              containerClassName={'pagination-container'}
              previousClassName={'previous-btn'}
              nextClassName={'next-btn'}
              disabledClassName={'pagination-disabled'}
              activeClassName={'pagination-active'}
            />
          )}
        </>
      )}
    </>
  );
}
