export function formatReviewCount(count: number): string {
  if (count === 1) {
    return '1 review';
  }
  return `${count} reviews`;
}

export function formatAverageRating(average: number): string {
  const value = Number(average);
  if (Number.isNaN(value)) {
    return '0.0';
  }
  return value.toFixed(1);
}
