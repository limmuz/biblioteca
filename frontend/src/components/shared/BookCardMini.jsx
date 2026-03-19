import { useNavigate } from 'react-router-dom';
import styles from './BookCardMini.module.css';

export default function BookCardMini({ book }) {
  const navigate = useNavigate();

  const handleOpenDetails = () => {
    // Se o ID da Open Library vier com "/works/", nós limpamos para não quebrar a URL
    const cleanId = book.id.replace('/works/', '');
    
    // Passamos o objeto completo no state para a DetalhesLivroPage ler instantaneamente
    navigate(`/livro/${cleanId}`, { state: { bookData: book } });
  };

  return (
    <div className={styles.card} onClick={handleOpenDetails} role="button">
      <img src={book.cover} alt={book.title} className={styles.cover} />
      <div className={styles.info}>
        <p className={styles.title}>{book.title}</p>
        <p className={styles.author}>{book.author}</p>
      </div>
    </div>
  );
}