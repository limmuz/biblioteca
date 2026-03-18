import { Link, useNavigate } from 'react-router-dom';
import styles from './AppHeader.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';
import HomeIcon from '../../assets/icon-home.svg?react';
import BookmarkIcon from '../../assets/icon-bookmark.svg?react';
import SearchIcon from '../../assets/icon-search.svg?react';
import ExitIcon from '../../assets/icon-exit.svg?react';

export default function AppHeader() {
  const navigate = useNavigate();

  return (
    <header className={styles.header} role="banner">
      <Link to="/home" className={styles.logo} aria-label="Lybre — ir para início">
        <LogoIcon className={styles.logoIcon} aria-hidden="true" />
      </Link>

      <nav className={styles.nav} aria-label="Navegação principal">
        <div className={styles.navIcons}>
          <Link to="/home" className={styles.navBtn} title="Página inicial" aria-label="Página inicial">
            <HomeIcon className={styles.navIcon} aria-hidden="true" />
          </Link>
          <Link to="/home" className={styles.navBtn} title="Marcadores" aria-label="Marcadores">
            <BookmarkIcon className={styles.bookmarkIcon} aria-hidden="true" />
          </Link>
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

        <button
          className={styles.navBtn}
          title="Sair"
          aria-label="Sair"
          onClick={() => navigate('/login')}
        >
          <ExitIcon className={styles.exitIcon} aria-hidden="true" />
        </button>
      </nav>
    </header>
  );
}
