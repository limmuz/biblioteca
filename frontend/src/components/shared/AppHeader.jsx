import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './AppHeader.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';
import HomeIcon from '../../assets/icon-home.svg?react';
import BookmarkIcon from '../../assets/icon-bookmark.svg?react';
import SearchIcon from '../../assets/icon-search.svg?react';
import ExitIcon from '../../assets/icon-exit.svg?react';
import { clearSession, getUser } from '../../services/auth';

export default function AppHeader() {
  const navigate = useNavigate();
  const [termo, setTermo] = useState('');
  const user = getUser();

  const initials = user.nome
    ? user.nome.split(' ').slice(0, 2).map((n) => n[0].toUpperCase()).join('')
    : '?';

  const handleLogout = () => {
    clearSession();
    navigate('/login');
  };

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

        <div className={styles.userArea}>
          <div className={styles.avatar} title={user.nome || 'Usuário'}>
            {initials}
          </div>
          <button className={styles.navBtn} onClick={handleLogout} title="Sair">
            <ExitIcon className={styles.exitIcon} />
          </button>
        </div>
      </nav>
    </header>
  );
}