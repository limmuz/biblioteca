import { useParams, useNavigate } from 'react-router-dom';
import AppHeader from '../shared/AppHeader';
import Footer from '../Footer/Footer';
import styles from './ReadingPage.module.css';
import { getBookById } from '../../data/books';
import { bookData } from './bookContent';

export default function ReadingPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const book = id ? getBookById(id) : null;
  const title = book ? book.title : bookData.title;
  const author = book ? `por ${book.author}` : bookData.author;

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main} aria-label="Conteúdo do livro">
        {/* Book title & author */}
        <div className={styles.bookHeader}>
          <h1 className={styles.bookTitle}>{title}</h1>
          <p className={styles.bookAuthor}>{author}</p>
        </div>

        {/* Reading content */}
        <article className={styles.contentCard}>
          {bookData.blocks.map((block, i) =>
            block.type === 'quote' ? (
              <blockquote key={i} className={styles.blockquote}>
                {block.text}
              </blockquote>
            ) : (
              <p key={i} className={styles.paragraph}>
                {block.text}
              </p>
            )
          )}
        </article>

        {/* Finish reading button */}
        <button
          className={styles.finishBtn}
          onClick={() => navigate(book ? `/livro/${book.id}` : '/home')}
          aria-label="Terminar leitura"
        >
          Terminar leitura
        </button>

        {/* Footer */}
        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}
