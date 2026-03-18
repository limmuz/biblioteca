import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import BookCardMini from '../components/shared/BookCardMini';
import styles from './ListagemPage.module.css';
import { recommendations } from '../data/books';

export default function ListagemPage() {
  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <h2 className={styles.sectionTitle}>Recomendações</h2>
        <div className={styles.bookGrid}>
          {[...recommendations, ...recommendations].slice(0, 20).map((book, i) => (
            <BookCardMini key={book.id + '-list-' + i} book={book} />
          ))}
        </div>
        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}
