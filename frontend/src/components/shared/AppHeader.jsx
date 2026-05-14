import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./AppHeader.module.css";

import {
  BsHouse,
  BsBookmark,
  BsJournalPlus,
  BsSearch,
  BsBoxArrowRight,
  BsXCircle,
} from "react-icons/bs";

import LogoIcon from "../../assets/icon-logo.svg?react";
import { clearSession, getUser } from "../../services/auth";
import api from "../../services/api";

export default function AppHeader() {
  const navigate = useNavigate();
  const [termo, setTermo] = useState("");
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [sugestoes, setSugestoes] = useState([]);
  const [showDrop, setShowDrop] = useState(false);
  const [buscando, setBuscando] = useState(false);
  const wrapperRef = useRef(null);
  const debounceRef = useRef(null);
  const user = getUser();

  useEffect(() => {
    const saved = localStorage.getItem('lybre_avatar');
    if (saved) setAvatarUrl(saved);
    api.get('/usuarios/me')
      .then(res => {
        if (res.data.avatarBase64) {
          setAvatarUrl(res.data.avatarBase64);
          localStorage.setItem('lybre_avatar', res.data.avatarBase64);
        }
      })
      .catch(() => {});
    const onStorage = (e) => { if (e.key === 'lybre_avatar') setAvatarUrl(e.newValue); };
    globalThis.addEventListener('storage', onStorage);
    return () => globalThis.removeEventListener('storage', onStorage);
  }, []);

  useEffect(() => {
    const handler = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setShowDrop(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (termo.trim().length < 2) {
      setSugestoes([]);
      setShowDrop(false);
      return;
    }
    debounceRef.current = setTimeout(async () => {
      setBuscando(true);
      try {
        const res = await api.get('/livros?search=' + encodeURIComponent(termo.trim()));
        setSugestoes(res.data.slice(0, 6));
        setShowDrop(true);
      } catch {
        setSugestoes([]);
      } finally {
        setBuscando(false);
      }
    }, 300);
    return () => clearTimeout(debounceRef.current);
  }, [termo]);

  const initials = user.nome
    ? user.nome.split(" ").slice(0, 2).map((n) => n[0].toUpperCase()).join("")
    : "?";

  const handleLogout = () => { clearSession(); navigate("/login"); };

  const executarBusca = () => {
    if (termo.trim()) {
      setShowDrop(false);
      navigate("/listagem", { state: { query: termo } });
      setTermo("");
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") executarBusca();
    if (e.key === "Escape") { setShowDrop(false); setTermo(""); }
  };

  const irParaLivro = (livro) => {
    setShowDrop(false);
    setTermo("");
    navigate(`/livro/${livro.id}`, { state: { bookData: livro } });
  };

  const limpar = () => { setTermo(""); setSugestoes([]); setShowDrop(false); };

  const STATUS_COR = {
    'LIDO':      '#2e6b3e',
    'LENDO':     '#2553a6',
    'QUERO LER': '#8a5f00',
    'RECOMENDADO': '#7a4000',
  };

  return (
    <header className={styles.header}>
      <Link to="/home" className={styles.logo}>
        <LogoIcon className={styles.logoIcon} />
      </Link>

      <nav className={styles.nav}>
        <div className={styles.navIcons}>
          <button onClick={() => navigate("/home")} className={styles.navBtn} title="Início">
            <BsHouse className={styles.navIcon} />
          </button>
          <button onClick={() => navigate("/listagem")} className={styles.navBtn} title="Minha Biblioteca">
            <BsBookmark className={styles.navIcon} />
          </button>
          <button onClick={() => navigate("/novo-livro")} className={styles.navBtn} title="Cadastrar Livro">
            <BsJournalPlus className={styles.navIcon} />
          </button>
        </div>

        <div className={styles.searchWrapper} ref={wrapperRef}>
          <div className={`${styles.searchBar} ${showDrop && sugestoes.length > 0 ? styles.searchBarOpen : ''}`}>
            <input
              type="text"
              className={styles.searchInput}
              placeholder="Título, autor ou categoria..."
              value={termo}
              onChange={(e) => setTermo(e.target.value)}
              onKeyDown={handleKeyDown}
              onFocus={() => sugestoes.length > 0 && setShowDrop(true)}
              aria-label="Buscar livro"
              autoComplete="off"
            />
            {termo && (
              <button type="button" className={styles.clearBtn} onClick={limpar} aria-label="Limpar busca">
                <BsXCircle className={styles.clearIcon} />
              </button>
            )}
            <button type="button" className={styles.searchIconBtn} onClick={executarBusca}
              title="Buscar" aria-label="Buscar">
              <BsSearch className={styles.searchIcon} />
            </button>
          </div>

          {showDrop && (sugestoes.length > 0 || buscando) && (
            <div className={styles.dropdown}>
              {buscando && <p className={styles.dropLoading}>Buscando...</p>}
              {sugestoes.map((livro) => (
                <button
                  key={livro.id}
                  type="button"
                  className={styles.dropItem}
                  onClick={() => irParaLivro(livro)}
                >
                  <img
                    src={livro.cover}
                    alt={livro.title}
                    className={styles.dropCover}
                    onError={(e) => { e.target.src = 'https://via.placeholder.com/32x48?text=?'; }}
                  />
                  <div className={styles.dropInfo}>
                    <p className={styles.dropTitle}>{livro.title}</p>
                    <p className={styles.dropAuthor}>{livro.author}</p>
                  </div>
                  {livro.status && (
                    <span
                      className={styles.dropStatus}
                      style={{ color: STATUS_COR[livro.status] || '#666' }}
                    >
                      {livro.status === 'QUERO LER' ? 'Quero ler' : livro.status.charAt(0) + livro.status.slice(1).toLowerCase()}
                    </span>
                  )}
                </button>
              ))}
              {sugestoes.length > 0 && (
                <button type="button" className={styles.dropVerTodos} onClick={executarBusca}>
                  Ver todos os resultados para "{termo}"
                </button>
              )}
            </div>
          )}
        </div>

        <div className={styles.userArea}>
          <button className={styles.avatarBtn} onClick={() => navigate('/perfil')} title={user.nome || "Usuário"}>
            {avatarUrl
              ? <img src={avatarUrl} alt="Avatar" className={styles.avatarImg} />
              : <div className={styles.avatar}>{initials}</div>
            }
          </button>
          <button className={styles.navBtn} onClick={handleLogout} title="Sair">
            <BsBoxArrowRight className={styles.navIcon} />
          </button>
        </div>
      </nav>
    </header>
  );
}
