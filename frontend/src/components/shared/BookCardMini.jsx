import React from 'react';
import PropTypes from 'prop-types'; 
import { useNavigate } from 'react-router-dom';
import styles from './BookCardMini.module.css';

export default function BookCardMini({ book }) {
  const navigate = useNavigate();

  const handleOpenDetails = () => {
    navigate(`/livro/${book.id}`, { state: { bookData: book } });
  };

  return (
   
    <button 
      className={styles.card} 
      onClick={handleOpenDetails}
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
      </div>
    </button>
  );
}

BookCardMini.propTypes = {
  book: PropTypes.shape({
    id: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    author: PropTypes.string.isRequired,
    cover: PropTypes.string
  }).isRequired
};