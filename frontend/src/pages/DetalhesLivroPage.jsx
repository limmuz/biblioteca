import { useParams, useNavigate } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import styles from './DetalhesLivroPage.module.css';
import { getBookById } from '../data/books';

export default function DetalhesLivroPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const book = getBookById(id);

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        {/* ── Hero ──────────────────────────────── */}
        <div className={styles.hero}>
          <img
            src={book.cover}
            alt={`Capa de ${book.title}`}
            className={styles.bookCover}
          />
          <div className={styles.heroInfo}>
            <h1 className={styles.bookTitle}>{book.title}</h1>
            <p className={styles.bookAuthor}>por {book.author}</p>
            <div className={styles.heroButtons}>
              <button
                className={styles.btnLer}
                onClick={() => navigate(`/leitura/${book.id}`)}
                aria-label={`Ler ${book.title}`}
              >
                Ler
              </button>
              <button className={styles.btnAdicionar} aria-label="Adicionar à lista de leitura">
                Adicionar a lista de leitura
              </button>
            </div>
          </div>
        </div>

        {/* ── Details card ────────────────────── */}
        <div className={styles.detailsCard}>
          {/* Sinopse column */}
          <div className={styles.sinopseCol}>
            <h2 className={styles.sinopseTitle}>Sinopse</h2>
            <hr className={styles.sinopseDivider} />
            <p className={styles.sinopseText}>{book.synopsis}</p>
          </div>

          {/* Metadata column */}
          <div className={styles.metaCol}>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Categorias</span>
              {book.categories.map((cat) => (
                <span key={cat} className={styles.metaTag}>
                  {cat}
                </span>
              ))}
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Idioma</span>
              <span className={styles.metaValue}>{book.language}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Editora</span>
              <span className={styles.metaValue}>{book.publisher}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Publicação</span>
              <span className={styles.metaValue}>{book.publishDate}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Páginas</span>
              <span className={styles.metaValue}>{book.pages}</span>
            </div>

            <div className={styles.metaButtons}>
              <button
                className={styles.btnLer}
                onClick={() => navigate(`/leitura/${book.id}`)}
                aria-label={`Ler ${book.title}`}
              >
                Ler
              </button>
              <button className={styles.btnAdicionar} aria-label="Adicionar à lista de leitura">
                Adicionar a lista de leitura
              </button>
            </div>
          </div>
        </div>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}
