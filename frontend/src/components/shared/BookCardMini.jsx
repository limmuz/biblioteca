import React from 'react';
import PropTypes from 'prop-types'; 
import { useNavigate } from 'react-router-dom';
import styles from './BookCardMini.module.css';

export default function BookCardMini({ book, onAddToList, rating, totalRatings }) {
  const navigate = useNavigate();

  return (
    <button
      className={styles.card}
      onClick={() => navigate(`/livro/${book.id}`, { state: { bookData: book } })}
      type="button"
      aria-label={`Ver detalhes de ${book.title}`}
    >
      <img
        src={book.cover}
        alt={`Capa do livro ${book.title}`}
        className={styles.cover}
      />
      <div className={styles.info}>
        <p className={styles.title}>{book.title}</p>
        <p className={styles.author}>{book.author}</p>
        {rating > 0 && (
          <div className={styles.ratingRow}>
            <div className={styles.stars}>
              {[1,2,3,4,5].map(s => (
                <span key={s} className={`${styles.star} ${s <= Math.round(rating) ? styles.starFilled : ''}`}>★</span>
              ))}
            </div>
            <span className={styles.ratingCount}>{rating.toFixed(1)} ({totalRatings})</span>
          </div>
        )}
        {onAddToList && !["LIDO", "LENDO", "QUERO LER"].includes(book.status) && (
          <button
            className={styles.addButton}
            onClick={(e) => { e.stopPropagation(); onAddToList(book); }}
            aria-label="Adicionar à lista de leitura"
          >
            + Adicionar à lista
          </button>
        )}
      </div>
    </button>
  );
}

BookCardMini.propTypes = {
  book: PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    author: PropTypes.string.isRequired,
    cover: PropTypes.string,
    status: PropTypes.string,
    excerpt: PropTypes.string,
    pages: PropTypes.number,
    language: PropTypes.string,
    categories: PropTypes.array,
    publisher: PropTypes.string,
    publishedDate: PropTypes.string,
  }).isRequired,
  onAddToList: PropTypes.func,
  rating: PropTypes.number,
  totalRatings: PropTypes.number,
};

BookCardMini.defaultProps = {
  onAddToList: null,
  rating: 0,
  totalRatings: 0,
};
