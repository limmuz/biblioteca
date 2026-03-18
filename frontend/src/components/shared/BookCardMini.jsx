import { useNavigate } from 'react-router-dom';
import styles from './BookCardMini.module.css';

export default function BookCardMini({ book }) {
  const navigate = useNavigate();

  return (
    <div
      className={styles.card}
      onClick={() => navigate(`/livro/${book.id}`)}
      role="button"
      tabIndex={0}
      aria-label={`${book.title} por ${book.author}`}
      onKeyDown={(e) => e.key === 'Enter' && navigate(`/livro/${book.id}`)}
    >
      <img src={book.cover} alt={`Capa de ${book.title}`} className={styles.cover} loading="lazy" />
      <div className={styles.info}>
        <p className={styles.title}>{book.title}</p>
        <p className={styles.author}>{book.author}</p>
      </div>
    </div>
  );
}
