import { useNavigate } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import BookCardMini from '../components/shared/BookCardMini';
import styles from './HomePage.module.css';
import { recentlyRead, readingList, recommendations } from '../data/books';

export default function HomePage() {
  const navigate = useNavigate();

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        {/* ── Últimas leituras ─────────────────── */}
        <h2 className={styles.sectionTitle}>Últimas leituras</h2>
        <div className={styles.carousel} aria-label="Últimas leituras">
          {recentlyRead.map((book) => (
            <div
              key={book.id}
              className={styles.carouselCard}
              onClick={() => navigate(`/livro/${book.id}`)}
              role="button"
              tabIndex={0}
              aria-label={`${book.title} por ${book.author}`}
              onKeyDown={(e) => e.key === 'Enter' && navigate(`/livro/${book.id}`)}
            >
              <img
                src={book.cover}
                alt={`Capa de ${book.title}`}
                className={styles.carouselCover}
                loading="lazy"
              />
              <div className={styles.carouselInfo}>
                <p className={styles.carouselTitle}>{book.title}</p>
                <p className={styles.carouselAuthor}>{book.author}</p>
                <p className={styles.carouselExcerpt}>{book.excerpt}</p>
              </div>
            </div>
          ))}
        </div>

        {/* ── Lista de leitura ──────────────────── */}
        <h2 className={styles.sectionTitle}>Sua lista de leitura</h2>
        <div className={styles.bookGrid}>
          {readingList.map((book) => (
            <BookCardMini key={book.id} book={book} />
          ))}
        </div>

        {/* ── Recomendações ─────────────────────── */}
        <h2 className={styles.sectionTitle}>Recomendações</h2>
        <div className={styles.bookGrid}>
          {recommendations.slice(0, 4).map((book) => (
            <BookCardMini key={book.id + '-rec'} book={book} />
          ))}
        </div>
        <button className={styles.moreBtn} onClick={() => navigate('/listagem')}>
          Mais recomendações
        </button>

        {/* ── Footer ───────────────────────────── */}
        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}
