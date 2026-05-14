import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import { isAuthenticated } from '../services/auth';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import styles from './PerfilPublicoPage.module.css';

const CACHE_TTL = 5 * 60 * 1000;
function lerCache(key) {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return null;
    const { ts, data } = JSON.parse(raw);
    return Date.now() - ts < CACHE_TTL ? data : null;
  } catch { return null; }
}
function salvarCache(key, data) {
  try { localStorage.setItem(key, JSON.stringify({ ts: Date.now(), data })); } catch {}
}

const STATUS_LABEL = {
  'LIDO':      { label: 'Lido',      cls: 'lido' },
  'LENDO':     { label: 'Lendo',     cls: 'lendo' },
  'QUERO LER': { label: 'Quero ler', cls: 'quero' },
};

export default function PerfilPublicoPage() {
  const { nickname, userId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const loggedIn = isAuthenticated();

  const [perfil, setPerfil] = useState(null);
  const [medias, setMedias] = useState(() => lerCache('lybre_medias_cache') || {});
  const [loading, setLoading] = useState(true);
  const [privado, setPrivado] = useState(false);
  const [filtro, setFiltro] = useState('TODOS');
  const [adicionandoLivro, setAdicionandoLivro] = useState(null);
  const [livrosAdicionados, setLivrosAdicionados] = useState(new Set());
  const [livrosNaBiblioteca, setLivrosNaBiblioteca] = useState(new Set());
  const [mostrarBotaoVoltar, setMostrarBotaoVoltar] = useState(false);
  const [toast, setToast] = useState(null);
  const [meNickname, setMeNickname] = useState(null);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 5000);
  };

  useEffect(() => {
    const carregar = async () => {
      try {
        const perfilUrl = userId
          ? `/usuarios/perfil/id/${encodeURIComponent(userId)}`
          : `/usuarios/perfil/${encodeURIComponent(nickname)}`;
        const requisicoes = [
          api.get(perfilUrl),
          api.get('/avaliacoes/medias').catch(() => ({ data: [] })),
        ];
        const cachedLivros = loggedIn ? lerCache('lybre_livros_cache') : null;
        if (cachedLivros) {
          const titulos = new Set(
            cachedLivros.map(l => `${(l.title || '').toLowerCase()}||${(l.author || '').toLowerCase()}`)
          );
          setLivrosNaBiblioteca(titulos);
        }
        if (loggedIn) {
          requisicoes.push(api.get('/usuarios/me').catch(() => ({ data: {} })));
          if (!cachedLivros) requisicoes.push(api.get('/livros').catch(() => ({ data: [] })));
        }
        const [resPerfil, resMedias, resMe, resMeusLivros] = await Promise.all(requisicoes);
        setPerfil(resPerfil.data);
        if (resMe) setMeNickname(resMe.data.nickname || null);
        if (resMeusLivros?.data) {
          salvarCache('lybre_livros_cache', resMeusLivros.data);
          const titulos = new Set(
            resMeusLivros.data.map(l => `${(l.title || '').toLowerCase()}||${(l.author || '').toLowerCase()}`)
          );
          setLivrosNaBiblioteca(titulos);
        }
        const map = {};
        resMedias.data.forEach(m => {
          const key = `${(m.livroTitulo || '').toLowerCase()}||${(m.livroAutor || '').toLowerCase()}`;
          map[key] = m;
        });
        salvarCache('lybre_medias_cache', map);
        setMedias(map);
      } catch (err) {
        if (err.response?.status === 403) setPrivado(true);
      } finally {
        setLoading(false);
      }
    };
    carregar();
  }, [nickname, userId, loggedIn]);

  const getRating = (livro) => {
    const key = `${(livro.title || '').toLowerCase()}||${(livro.author || '').toLowerCase()}`;
    return medias[key] || { media: 0, total: 0 };
  };

  const iniciais = perfil?.nome
    ? perfil.nome.split(' ').slice(0, 2).map(n => n[0].toUpperCase()).join('')
    : '?';

  const generoMaisLido = useMemo(() => {
    if (!perfil?.livros) return null;
    const freq = {};
    perfil.livros.forEach(l => {
      (l.categories || []).forEach(c => {
        if (c && c !== 'Nao informado') freq[c] = (freq[c] || 0) + 1;
      });
    });
    const entradas = Object.entries(freq);
    if (!entradas.length) return null;
    return entradas.reduce((a, b) => (b[1] > a[1] ? b : a), entradas[0])[0];
  }, [perfil]);

  const livrosFiltrados = useMemo(() => {
    if (!perfil?.livros) return [];
    if (filtro === 'TODOS') return perfil.livros;
    return perfil.livros.filter(l => l.status === filtro);
  }, [perfil, filtro]);

  const bgTiles = useMemo(() => {
    if (!perfil?.livros) return [];
    const capas = perfil.livros.map(l => l.cover).filter(Boolean);
    if (capas.length < 4) return [];
    return Array.from({ length: 60 }, (_, i) => ({ id: `tile-${i}`, src: capas[i % capas.length] }));
  }, [perfil]);

  const jaTemNaBiblioteca = (livro) => {
    const chave = `${livro.title.toLowerCase()}||${livro.author.toLowerCase()}`;
    return livrosNaBiblioteca.has(chave) || livrosAdicionados.has(chave);
  };

  const handleAdicionarLivro = async (livro) => {
    setAdicionandoLivro(livro.title);
    try {
      await api.post('/livros', {
        title: livro.title,
        author: livro.author,
        cover: livro.cover,
        categories: livro.categories,
        status: 'QUERO LER',
      });
      const chave = `${livro.title.toLowerCase()}||${livro.author.toLowerCase()}`;
      localStorage.removeItem('lybre_livros_cache');
      setLivrosAdicionados(prev => new Set([...prev, chave]));
      setMostrarBotaoVoltar(true);
      showToast(`"${livro.title}" adicionado à sua biblioteca!`);
    } catch (err) {
      if (err.response?.status === 409) {
        showToast('Este livro já está na sua biblioteca.', 'info');
      } else {
        showToast('Erro ao adicionar o livro. Tente novamente.', 'error');
      }
    } finally {
      setAdicionandoLivro(null);
    }
  };

  const ePerfilPropio = loggedIn && meNickname && perfil && meNickname === perfil.nickname;

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <LoadingSpinner message="Carregando perfil do leitor..." />
      </div>
    );
  }

  if (privado) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <div className={styles.privadoBox}>
            <span className={styles.privadoIcon}>🔒</span>
            <h2 className={styles.privadoTitle}>Perfil privado</h2>
            <p className={styles.privadoText}>Este leitor optou por manter o perfil privado.</p>
            <button className={styles.btnVoltar} onClick={() => navigate(-1)} type="button">
              ← Voltar
            </button>
          </div>
        </main>
      </div>
    );
  }

  if (!perfil) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <div className={styles.privadoBox}>
            <p className={styles.privadoText}>Leitor não encontrado.</p>
            <button className={styles.btnVoltar} onClick={() => navigate(-1)} type="button">← Voltar</button>
          </div>
        </main>
      </div>
    );
  }

  const bgStyle = perfil.bgBase64
    ? { backgroundImage: `url(${perfil.bgBase64})` }
    : null;

  const FILTROS = [
    { valor: 'TODOS', rotulo: 'Todos' },
    { valor: 'LIDO',      rotulo: 'Lidos' },
    { valor: 'LENDO',     rotulo: 'Lendo' },
    { valor: 'QUERO LER', rotulo: 'Quero ler' },
  ];

  return (
    <div className={styles.page}>
      {bgStyle ? (
        <>
          <div className={styles.profileBg} aria-hidden="true" style={bgStyle} />
          <div className={styles.bgOverlay} aria-hidden="true" />
        </>
      ) : null}
      {!bgStyle && bgTiles.length > 0 && (
        <div className={styles.bgCovers} aria-hidden="true">
          {bgTiles.map(tile => (
            <img key={tile.id} src={tile.src} alt="" className={styles.bgCover}
              onError={e => { e.target.style.visibility = 'hidden'; }} />
          ))}
        </div>
      )}

      {toast && (
        <div className={[styles.toast, styles[`toast_${toast.type}`]].filter(Boolean).join(' ')}>
          {toast.msg}
        </div>
      )}

      <AppHeader />
      <main className={styles.main}>

        <div className={styles.profileHeader}>
          <div className={styles.avatarWrapper}>
            {perfil.avatarBase64
              ? <img src={perfil.avatarBase64} alt={`Foto de ${perfil.nome}`} className={styles.avatarImg} />
              : <div className={styles.avatarInitials}>{iniciais}</div>
            }
          </div>
          <div className={styles.profileInfo}>
            <h1 className={styles.nome}>{perfil.nome}</h1>
            {perfil.nickname && <p className={styles.nicknameText}>@{perfil.nickname}</p>}
            {perfil.bio && <p className={styles.bio}>"{perfil.bio}"</p>}
            {generoMaisLido && <span className={styles.genreBadge}>📖 {generoMaisLido}</span>}
          </div>
          {ePerfilPropio && (
            <button
              className={styles.btnVoltar}
              type="button"
              onClick={() => navigate('/home')}
            >
              ✏️ Editar meu perfil
            </button>
          )}
        </div>

        <div className={styles.statsRow}>
          {[
            { rotulo: 'Lidos',     quantidade: perfil.totalLidos,    emoji: '✅' },
            { rotulo: 'Lendo',     quantidade: perfil.totalLendo,    emoji: '📖' },
            { rotulo: 'Quero ler', quantidade: perfil.totalQueroLer, emoji: '📚' },
          ].map(s => (
            <div key={s.rotulo} className={styles.statCard}>
              <span className={styles.statEmoji}>{s.emoji}</span>
              <span className={styles.statCount}>{s.quantidade}</span>
              <span className={styles.statLabel}>{s.rotulo}</span>
            </div>
          ))}
        </div>

        {mostrarBotaoVoltar && (
          <div className={styles.bannerAdicionado}>
            <span>✅ Livro(s) adicionado(s) à sua biblioteca!</span>
            <button
              className={styles.btnIrBiblioteca}
              type="button"
              onClick={() => navigate('/home')}
            >
              Ver minha biblioteca →
            </button>
          </div>
        )}

        {perfil.livros.length > 0 && (
          <div className={styles.filtroRow}>
            {FILTROS.map(f => (
              <button
                key={f.valor}
                className={`${styles.filtroBtn} ${filtro === f.valor ? styles.filtroBtnActive : ''}`}
                onClick={() => setFiltro(f.valor)}
                type="button"
              >
                {f.rotulo}
              </button>
            ))}
          </div>
        )}

        <div className={styles.booksSection}>
          {livrosFiltrados.length === 0 ? (
            <p className={styles.semLivros}>Nenhum livro nesta categoria ainda.</p>
          ) : (
            <div className={styles.bookGrid}>
              {livrosFiltrados.map((livro) => {
                const r = getRating(livro);
                const infoStatus = STATUS_LABEL[livro.status];
                const jaAdicionado = jaTemNaBiblioteca(livro);
                const badgeExtra = infoStatus ? styles[`badge_${infoStatus.cls}`] : '';
                const badgeCls = infoStatus ? `${styles.statusBadge} ${badgeExtra}` : '';
                let btnTexto = '+ Minha biblioteca';
                if (jaAdicionado) btnTexto = '✓ Minha biblioteca';
                else if (adicionandoLivro === livro.title) btnTexto = 'Adicionando...';
                return (
                  <div key={`${livro.title}||${livro.author}`} className={styles.bookCard}>
                    <div className={styles.coverWrapper}>
                      <img
                        src={livro.cover}
                        alt={livro.title}
                        className={styles.bookCover}
                        loading="lazy"
                        onError={e => { e.target.src = '/books/book-sherlock.png'; }}
                      />
                      {infoStatus && (
                        <span className={badgeCls}>{infoStatus.label}</span>
                      )}
                    </div>
                    <div className={styles.bookInfo}>
                      <p className={styles.bookTitle}>{livro.title}</p>
                      <p className={styles.bookAuthor}>{livro.author}</p>
                      {r.media > 0 && (
                        <div className={styles.ratingRow}>
                          {[1,2,3,4,5].map(s => (
                            <span key={s} className={s <= Math.round(r.media) ? styles.starFilled : styles.starEmpty}>★</span>
                          ))}
                          <span className={styles.ratingCount}>{r.media.toFixed(1)} ({r.total})</span>
                        </div>
                      )}
                      {loggedIn && !ePerfilPropio && (
                        <button
                          className={jaAdicionado ? styles.btnAddLivroFeito : styles.btnAddLivro}
                          type="button"
                          disabled={adicionandoLivro === livro.title || jaAdicionado}
                          onClick={() => handleAdicionarLivro(livro)}
                        >
                          {btnTexto}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className={styles.rodapeAcoes}>
          <button
            className={styles.btnVoltar}
            type="button"
            onClick={() => {
              if (location.state?.from === 'perfisAdicionados') {
                navigate('/perfil', { state: { scrollTo: 'perfisAdicionados' } });
              } else {
                navigate(-1);
              }
            }}
          >
            ← Voltar
          </button>
          {mostrarBotaoVoltar && (
            <button className={styles.btnIrBiblioteca} type="button" onClick={() => navigate('/home')}>
              Ver minha biblioteca →
            </button>
          )}
        </div>

        <div className={styles.footerWrap}><Footer /></div>
      </main>
    </div>
  );
}
