import React from 'react';
import styles from './TopReviews.module.scss';

interface Place {
  id: number;
  name: string;
  rating: number;
  description: string;
  image: string;
}

interface TopReviewsProps {
  places: Place[];
}

const TopReviews: React.FC<TopReviewsProps> = ({ places }) => {
  return (
    <section className={styles['top-reviews']}>
      <h2 className={styles['section-title']}>Top Rated Places</h2>
      <div className={styles['reviews-grid']}>
        {places.map((place) => (
          <div
            key={place.id}
            className={styles['place-card']}
            style={{
              backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url(${place.image})`,
            }}
          >
            <h3 className={styles['place-name']}>{place.name}</h3>
            <div className={styles.rating}>
              {'★'.repeat(Math.round(place.rating)) +
                '☆'.repeat(5 - Math.round(place.rating))}
            </div>
            <p className={styles.description}>{place.description}</p>
            <button className={styles['review-button']}>Read Reviews</button>
          </div>
        ))}
      </div>
    </section>
  );
};

export default TopReviews;
