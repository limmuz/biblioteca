import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./AppHeader.module.css";

import { 
  BsHouse,
  BsBookmark,
  BsJournalPlus,
  BsSearch,
  BsBoxArrowRight
} from "react-icons/bs";

import LogoIcon from "../../assets/icon-logo.svg?react";
import { clearSession, getUser } from "../../services/auth";

export default function AppHeader() {
  const navigate = useNavigate();
  const [termo, setTermo] = useState("");
  const [avatarUrl, setAvatarUrl] = useState(null);
  const user = getUser();

  useEffect(() => {
    const saved = localStorage.getItem('lybre_avatar');
    if (saved) setAvatarUrl(saved);

    const onStorage = (e) => {
      if (e.key === 'lybre_avatar') setAvatarUrl(e.newValue);
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const initials = user.nome
    ? user.nome
        .split(" ")
        .slice(0, 2)
        .map((n) => n[0].toUpperCase())
        .join("")
    : "?";

  const handleLogout = () => {
    clearSession();
    navigate("/login");
  };

  const handleSearch = (e) => {
    if (e.key === "Enter" && termo.trim()) {
      navigate("/listagem", { state: { query: termo } });
      setTermo("");
    }
  };

  return (
    <header className={styles.header}>
      <Link to="/home" className={styles.logo}>
        <LogoIcon className={styles.logoIcon} />
      </Link>

      <nav className={styles.nav}>
        <div className={styles.navIcons}>

          <button
            onClick={() => navigate("/home")}
            className={styles.navBtn}
            title="Início"
          >
            <BsHouse className={styles.navIcon} />
          </button>

          <button
            onClick={() => navigate("/listagem")}
            className={styles.navBtn}
            title="Minha Biblioteca"
          >
            <BsBookmark className={styles.navIcon} />
          </button>

          <button
            onClick={() => navigate("/novo-livro")}
            className={styles.navBtn}
            title="Cadastrar Livro"
          >
            <BsJournalPlus className={styles.navIcon} />
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

          <BsSearch className={styles.searchIcon} />
        </div>

        <div className={styles.userArea}>
          <button
            className={styles.avatarBtn}
            onClick={() => navigate('/perfil')}
            title={user.nome || "Usuário"}
          >
            {avatarUrl ? (
              <img src={avatarUrl} alt="Avatar" className={styles.avatarImg} />
            ) : (
              <div className={styles.avatar}>{initials}</div>
            )}
          </button>

          <button
            className={styles.navBtn}
            onClick={handleLogout}
            title="Sair"
          >
            <BsBoxArrowRight className={styles.navIcon} />
          </button>
        </div>
      </nav>
    </header>
  );
}