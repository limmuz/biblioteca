import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './AppHeader.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';
import HomeIcon from '../../assets/icon-home.svg?react';
import BookmarkIcon from '../../assets/icon-bookmark.svg?react';
import SearchIcon from '../../assets/icon-search.svg?react';
import ExitIcon from '../../assets/icon-exit.svg?react';

export default function AppHeader() {
  const navigate = useNavigate();
  const [termo, setTermo] = useState('');

  const handleSearch = (e) => {
    if (e.key === 'Enter' && termo.trim()) {
      navigate('/listagem', { state: { query: termo } });
      setTermo('');
    }
  };

  return (
    <header className={styles.header}>
      <Link to="/home" className={styles.logo}>
        <LogoIcon className={styles.logoIcon} />
      </Link>

      <nav className={styles.nav}>
        <div className={styles.navIcons}>
          <button onClick={() => navigate('/home')} className={styles.navBtn} title="Início">
            <HomeIcon className={styles.navIcon} />
          </button>
          
          <button onClick={() => navigate('/listagem')} className={styles.navBtn} title="Minha Biblioteca">
            <BookmarkIcon className={styles.bookmarkIcon} />
          </button>

          <button onClick={() => navigate('/novo-livro')} className={styles.navBtn} title="Cadastrar Manualmente">
            <span style={{fontSize: '24px', color: 'var(--color-forest)', fontWeight: 'bold'}}>+</span>
          </button>
        </div>

        <div className={styles.searchBar}>
          <input
            type="search"
            className={styles.searchInput}
            placeholder="Título, autor ou categoria..."
            value={termo}
            onChange={(e) => setTermo(e.target.value)}
            onKeyDown={handleSearch}
          />
          <SearchIcon className={styles.searchIcon} />
        </div>

        <button className={styles.navBtn} onClick={() => navigate('/login')} title="Sair">
          <ExitIcon className={styles.exitIcon} />
        </button>
      </nav>
    </header>
  );
}