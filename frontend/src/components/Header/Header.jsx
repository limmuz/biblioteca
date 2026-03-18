import styles from './Header.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';
import HomeIcon from '../../assets/icon-home.svg?react';
import BookmarkIcon from '../../assets/icon-bookmark.svg?react';
import SearchIcon from '../../assets/icon-search.svg?react';
import ExitIcon from '../../assets/icon-exit.svg?react';

export default function Header() {
  return (
    <header className={styles.header} role="banner">
      <div className={styles.logo} aria-label="Lybre">
        <LogoIcon className={styles.logoIcon} aria-hidden="true" />
      </div>

      <nav className={styles.nav} aria-label="Navegação principal">
        <div className={styles.navIcons}>
          <button className={styles.navBtn} title="Página inicial" aria-label="Página inicial">
            <HomeIcon className={styles.navIcon} aria-hidden="true" />
          </button>
          <button className={styles.navBtn} title="Marcadores" aria-label="Marcadores">
            <BookmarkIcon className={styles.bookmarkIcon} aria-hidden="true" />
          </button>
        </div>

        <div className={styles.searchBar} role="search">
          <input
            type="search"
            className={styles.searchInput}
            placeholder="Pesquisar em Lybre"
            aria-label="Pesquisar em Lybre"
          />
          <SearchIcon className={styles.searchIcon} aria-hidden="true" />
        </div>

        <button className={styles.navBtn} title="Sair" aria-label="Sair da leitura">
          <ExitIcon className={styles.exitIcon} aria-hidden="true" />
        </button>
      </nav>
    </header>
  );
}
