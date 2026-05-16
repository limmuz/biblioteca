import React, { useState, useEffect, useMemo } from 'react';
import PropTypes from 'prop-types';
import { useParams, useNavigate, useLocation } from 'react-router-dom';

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
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import Modal from '../components/shared/Modal';
import api from '../services/api';
import AdBanner from '../components/shared/AdBanner';
import styles from './DetalhesLivroPage.module.css';

function StarRating({ value, onChange, readonly }) {
  const [hovered, setHovered] = useState(0);
  return (
    <div className={styles.stars}>
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          className={`${styles.star} ${(hovered || value) >= star ? styles.starFilled : ''}`}
          onClick={() => !readonly && onChange?.(star)}
          onMouseEnter={() => !readonly && setHovered(star)}
          onMouseLeave={() => !readonly && setHovered(0)}
          disabled={readonly}
          aria-label={`${star} estrelas`}
        >★</button>
      ))}
    </div>
  );
}

StarRating.propTypes = {
  value: PropTypes.number.isRequired,
  onChange: PropTypes.func,
  readonly: PropTypes.bool,
};
StarRating.defaultProps = { onChange: null, readonly: false };

export default function DetalhesLivroPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [adicionando, setAdicionando] = useState(false);
  const [atualizandoStatus, setAtualizandoStatus] = useState(false);
  const [toastVisible, setToastVisible] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const [avaliacoes, setAvaliacoes] = useState([]);
  const [minhaAvaliacao, setMinhaAvaliacao] = useState(null);
  const [ratingForm, setRatingForm] = useState(0);
  const [comentarioForm, setComentarioForm] = useState('');
  const [salvandoAvaliacao, setSalvandoAvaliacao] = useState(false);
  const [editandoAvaliacao, setEditandoAvaliacao] = useState(false);

  const [outrosLeitores, setOutrosLeitores] = useState([]);
  const [respostaAberta, setRespostaAberta] = useState(null);
  const [textoResposta, setTextoResposta] = useState('');
  const [enviandoResposta, setEnviandoResposta] = useState(false);

  const [allBooks, setAllBooks] = useState(() => lerCache('lybre_livros_cache') || []);

  useEffect(() => {
    setError(false);
    setToastVisible(false);
    setRespostaAberta(null);
    const fromState = location.state?.bookData;
    if (fromState && fromState.id === id) {
      setBook(fromState);
      setLoading(false);
      return;
    }
    setBook(null);
    setLoading(true);
    api.get(`/livros/${id}`)
      .then((res) => { setBook(res.data); setLoading(false); })
      .catch(() => { setError(true); setLoading(false); });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (!id) return;
    setAvaliacoes([]);
    setMinhaAvaliacao(null);
    setRatingForm(0);
    setComentarioForm('');
    setEditandoAvaliacao(false);
    setOutrosLeitores([]);
    api.get(`/avaliacoes/livro/${id}`)
      .then((res) => {
        setAvaliacoes(res.data);
        const minha = res.data.find(a => a.minha);
        if (minha) {
          setMinhaAvaliacao(minha);
          setRatingForm(minha.rating);
          setComentarioForm(minha.comentario || '');
        }
      })
      .catch(() => {});
    api.get(`/avaliacoes/livro/${id}/leitores`)
      .then((res) => setOutrosLeitores(res.data))
      .catch(() => {});
  }, [id]);

  useEffect(() => {
    api.get('/livros')
      .then((res) => { salvarCache('lybre_livros_cache', res.data); setAllBooks(res.data); })
      .catch(() => {});
  }, []);

  const currentIdx = allBooks.findIndex(b => b.id === id);
  const prevBook = currentIdx > 0 ? allBooks[currentIdx - 1] : null;
  const nextBook = currentIdx >= 0 && currentIdx < allBooks.length - 1 ? allBooks[currentIdx + 1] : null;

  const recoBooks = useMemo(() => {
    if (!book || allBooks.length === 0) return [];
    const cats = new Set((book.categories || []).map(c => c.toLowerCase()));
    return allBooks
      .filter(b => b.id !== id && (
        b.author?.toLowerCase() === book.author?.toLowerCase() ||
        (b.categories || []).some(c => cats.has(c.toLowerCase()))
      ));
  }, [allBooks, book, id]);

  const recoCarouselRef = React.useRef(null);
  const scrollReco = (dir) => {
    if (!recoCarouselRef.current) return;
    recoCarouselRef.current.scrollBy({ left: dir * 220, behavior: 'smooth' });
  };

  const mediaRating = avaliacoes.length
    ? (avaliacoes.reduce((s, a) => s + a.rating, 0) / avaliacoes.length).toFixed(1)
    : null;

  const handleSalvarAvaliacao = async () => {
    if (!ratingForm) return;
    setSalvandoAvaliacao(true);
    try {
      const res = await api.post(`/avaliacoes/livro/${id}`, { rating: ratingForm, comentario: comentarioForm });
      setMinhaAvaliacao(res.data);
      setAvaliacoes(prev => {
        const sem = prev.filter(a => !a.minha);
        return [res.data, ...sem];
      });
      setEditandoAvaliacao(false);
    } catch (err) { console.error(err); }
    finally { setSalvandoAvaliacao(false); }
  };

  const handleExcluirAvaliacao = async () => {
    try {
      await api.delete(`/avaliacoes/livro/${id}`);
      setMinhaAvaliacao(null);
      setRatingForm(0);
      setComentarioForm('');
      setAvaliacoes(prev => prev.filter(a => !a.minha));
    } catch (err) { console.error(err); }
  };

  const handleAdicionarFavoritos = async () => {
    if (!book || adicionando) return;
    setAdicionando(true);
    try {
      await api.put(`/livros/${book.id}`, {
        title: book.title, author: book.author, cover: book.cover,
        excerpt: book.excerpt || 'Sinopse não disponível', status: 'QUERO LER',
        pages: book.pages, language: book.language, categories: book.categories,
        publisher: book.publisher, publishedDate: book.publishedDate,
      });
      setBook({ ...book, status: 'QUERO LER' });
      setToastVisible(true);
    } catch (err) {
      console.error(err);
    } finally {
      setAdicionando(false);
    }
  };

  const handleAtualizarStatus = async (novoStatus) => {
    if (!book || atualizandoStatus) return;
    setAtualizandoStatus(true);
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: novoStatus });
      setBook({ ...book, status: novoStatus });
    } catch (err) {
      console.error(err);
    } finally {
      setAtualizandoStatus(false);
    }
  };

  const handleRemoverFavoritos = async () => {
    setToastVisible(false);
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: 'LIDO' });
      navigate('/home');
    } catch (err) {
      console.error(err);
    }
  };

  const handleEditar = () => {
    navigate(`/editar-livro/${book.id}`, { state: { bookData: book } });
  };

  const handleCurtir = async (avaliacaoId) => {
    try {
      const res = await api.post(`/avaliacoes/${avaliacaoId}/curtir`);
      setAvaliacoes(prev => prev.map(a => a.id === avaliacaoId ? { ...a, ...res.data } : a));
    } catch (err) { console.error(err); }
  };

  const handleResponder = async (avaliacaoId) => {
    if (!textoResposta.trim()) return;
    setEnviandoResposta(true);
    try {
      const res = await api.post(`/avaliacoes/${avaliacaoId}/responder`, { texto: textoResposta });
      setAvaliacoes(prev => prev.map(a => a.id === avaliacaoId ? { ...a, ...res.data } : a));
      setTextoResposta('');
      setRespostaAberta(null);
    } catch (err) { console.error(err); }
    finally { setEnviandoResposta(false); }
  };

  const handleExcluirResposta = async (avaliacaoId, respostaId) => {
    try {
      await api.delete(`/avaliacoes/${avaliacaoId}/resposta/${respostaId}`);
      setAvaliacoes(prev => prev.map(a => a.id === avaliacaoId
        ? { ...a, respostas: (a.respostas || []).filter(r => r.id !== respostaId) }
        : a
      ));
    } catch (err) { console.error(err); }
  };

  const handleExcluirPermanente = async () => {
    try {
      await api.delete(`/livros/${book.id}`);
      localStorage.removeItem('lybre_livros_cache');
      navigate('/home');
    } catch (err) {
      console.error(err);
    }
    setConfirmDelete(false);
  };

  const goToBook = (b) => navigate(`/livro/${b.id}`, { state: { bookData: b } });

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <LoadingSpinner message="Carregando livro..." />
        </main>
      </div>
    );
  }

  if (error || !book) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', paddingTop: '60px' }}>
          <h2 style={{ fontFamily: 'var(--font-serif)', color: 'var(--color-ink)' }}>Livro não encontrado</h2>
          <button onClick={() => navigate('/home')} className={styles.btnLer} style={{ marginTop: '20px' }}>Voltar para Home</button>
        </main>
      </div>
    );
  }

  const categorias = Array.isArray(book.categories) && book.categories.length > 0
    ? book.categories : ['Não informado'];
  const idioma = book.language || 'Não informado';
  const editora = book.publisher || 'Não informado';
  const publicacao = book.publishedDate || 'Não informado';
  const paginas = book.pages || '--';

  const statusConfig = {
    'QUERO LER': { label: 'Na sua lista', icon: '📚', cls: styles.badgeList },
    'LENDO':     { label: 'Lendo agora',  icon: '📖', cls: styles.badgeLendo },
    'LIDO':      { label: 'Já lido',      icon: '✅', cls: styles.badgeLido },
    'RECOMENDADO': { label: 'Recomendado', icon: '⭐', cls: styles.badgeRec },
  };
  const statusInfo = statusConfig[book.status];

  return (
    <>
    <div className={styles.page}>
      <AppHeader />

      <main className={styles.main}>

        <div className={styles.hero}>
          <img
            src={book.cover}
            alt={book.title}
            className={styles.bookCover}
            onError={(e) => { e.target.src = 'https://via.placeholder.com/200x300?text=Sem+Capa'; }}
          />
          <div className={styles.heroInfo}>
            <h1 className={styles.bookTitle}>{book.title}</h1>
            <p className={styles.bookAuthor}>por {book.author}</p>
            <div className={styles.heroButtons}>
              {!['QUERO LER', 'LENDO', 'LIDO', 'RECOMENDADO'].includes(book.status) && (
                <button className={styles.btnAdicionar} onClick={handleAdicionarFavoritos} disabled={adicionando}>
                  {adicionando ? 'Adicionando...' : 'Adicionar à lista'}
                </button>
              )}
              {statusInfo && (
                <select
                  className={styles.statusSelect}
                  value={book.status}
                  onChange={e => handleAtualizarStatus(e.target.value)}
                  disabled={atualizandoStatus}
                >
                  <option value="QUERO LER">📚 Quero Ler</option>
                  <option value="LENDO">📖 Lendo</option>
                  <option value="LIDO">✅ Lido</option>
                  <option value="RECOMENDADO">⭐ Recomendado</option>
                </select>
              )}
            </div>
          </div>
        </div>

        <div className={styles.detailsCard}>
          <div className={styles.sinopseCol}>
            <h2 className={styles.sinopseTitle}>Sinopse</h2>
            <hr className={styles.sinopseDivider} />
            <p className={styles.sinopseText}>{book.excerpt || 'Sinopse não disponível.'}</p>
          </div>

          <div className={styles.metaCol}>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Categorias</span>
              <div className={styles.metaTagsWrap}>
                {categorias.map((cat) => (
                  <span key={cat} className={styles.metaTag}>{cat}</span>
                ))}
              </div>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Idioma</span>
              <span className={styles.metaValue}>{idioma}</span>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Editora</span>
              <span className={styles.metaValue}>{editora}</span>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Publicação</span>
              <span className={styles.metaValue}>{publicacao}</span>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Páginas</span>
              <span className={styles.metaValue}>{paginas}</span>
            </div>
            <div className={styles.metaButtons}>
              <button className={styles.btnEditar} onClick={handleEditar}>Editar</button>
              <button className={styles.btnExcluir} onClick={() => setConfirmDelete(true)}>Excluir</button>
            </div>
          </div>
        </div>

        {outrosLeitores.length > 0 && (
          <div className={styles.leitoresCard}>
            <h2 className={styles.sectionTitle}>Outros leitores com este livro</h2>
            <div className={styles.leitoresGrid}>
              {outrosLeitores.map((l) => {
                const iniciais = l.nome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
                const perfilUrl = l.nickname ? `/leitor/${l.nickname}` : `/leitor/id/${l.id}`;
                return (
                  <button
                    key={l.id}
                    type="button"
                    className={styles.leitorChip}
                    onClick={() => navigate(perfilUrl)}
                    title={`Ver perfil de ${l.nome}`}
                  >
                    {l.avatarBase64
                      ? <img src={l.avatarBase64} alt={l.nome} className={styles.leitorChipAvatar} />
                      : <div className={styles.leitorChipInitials}>{iniciais}</div>
                    }
                    <div>
                      <p className={styles.leitorChipNome}>{l.nome}</p>
                      {l.nickname && <p className={styles.leitorChipNick}>@{l.nickname}</p>}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        <div className={styles.avaliacoesCard}>
          <div className={styles.avaliacoesHeader}>
            <h2 className={styles.sectionTitle}>Avaliações</h2>
            {mediaRating && (
              <div className={styles.mediaWrap}>
                <span className={styles.mediaNum}>{mediaRating}</span>
                <StarRating value={Math.round(Number(mediaRating))} readonly />
                <span className={styles.mediaSub}>({avaliacoes.length} avaliação{avaliacoes.length !== 1 ? 'ões' : ''})</span>
              </div>
            )}
          </div>

          {!minhaAvaliacao || editandoAvaliacao ? (
            <div className={styles.minhaAvaliacaoForm}>
              <h3 className={styles.formTitle}>{minhaAvaliacao ? 'Editar avaliação' : 'Avaliar este livro'}</h3>
              <StarRating value={ratingForm} onChange={setRatingForm} />
              <textarea
                className={styles.comentarioInput}
                placeholder="Comentário (opcional)..."
                value={comentarioForm}
                onChange={e => setComentarioForm(e.target.value)}
                maxLength={500}
                rows={2}
              />
              <div className={styles.formActions}>
                <button className={styles.btnAvaliar} onClick={handleSalvarAvaliacao}
                  disabled={!ratingForm || salvandoAvaliacao}>
                  {salvandoAvaliacao ? 'Salvando...' : 'Publicar'}
                </button>
                {editandoAvaliacao && (
                  <button className={styles.btnCancelar} onClick={() => setEditandoAvaliacao(false)}>Cancelar</button>
                )}
              </div>
            </div>
          ) : (
            <div className={styles.minhaAvaliacaoCard}>
              <div className={styles.avaliacaoHeader}>
                <span className={styles.minhaTag}>Minha avaliação</span>
                <StarRating value={minhaAvaliacao.rating} readonly />
              </div>
              {minhaAvaliacao.comentario && <p className={styles.avaliacaoTexto}>{minhaAvaliacao.comentario}</p>}
              <div className={styles.avaliacaoActions}>
                <button className={styles.btnEditar2} onClick={() => setEditandoAvaliacao(true)}>Editar</button>
                <button className={styles.btnExcluir2} onClick={handleExcluirAvaliacao}>Excluir</button>
              </div>
            </div>
          )}

          <div className={styles.avaliacoesList}>
            {avaliacoes.filter(a => !a.minha).length === 0 ? (
              <p className={styles.semAvaliacoes}>Ainda sem avaliações de outros leitores.</p>
            ) : (
              avaliacoes.filter(a => !a.minha).map((av) => {
                const ini = av.usuarioNome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
                const data = new Date(av.criadoEm).toLocaleDateString('pt-BR');
                const respostaOpen = respostaAberta === av.id;
                return (
                  <div key={av.id} className={styles.avaliacaoItem}>
                    <div className={styles.avaliacaoAvatar}>
                      {av.avatarBase64
                        ? <img src={av.avatarBase64} alt={av.usuarioNome} className={styles.avaliacaoAvatarImg} />
                        : <div className={styles.avaliacaoAvatarIni}>{ini}</div>
                      }
                    </div>
                    <div className={styles.avaliacaoBody}>
                      <div className={styles.avaliacaoMeta}>
                        <span className={styles.avaliacaoNome}>{av.usuarioNome}</span>
                        {av.usuarioNickname && <span className={styles.avaliacaoNick}>@{av.usuarioNickname}</span>}
                        <StarRating value={av.rating} readonly />
                        <span className={styles.avaliacaoData}>{data}</span>
                      </div>
                      {av.comentario && <p className={styles.avaliacaoTexto}>{av.comentario}</p>}
                      <div className={styles.avaliacaoFooter}>
                        <button
                          type="button"
                          className={av.euCurti ? styles.btnCurtirAtivo : styles.btnCurtir}
                          onClick={() => handleCurtir(av.id)}
                          title={av.euCurti ? 'Descurtir' : 'Curtir'}
                        >
                          {av.euCurti ? '❤️' : '🤍'} {av.totalCurtidas > 0 ? av.totalCurtidas : ''}
                        </button>
                        <button
                          type="button"
                          className={styles.btnResponder}
                          onClick={() => {
                            setRespostaAberta(respostaOpen ? null : av.id);
                            setTextoResposta('');
                          }}
                        >
                          💬 {(av.respostas?.length > 0) ? av.respostas.length : ''} Responder
                        </button>
                      </div>
                      {respostaOpen && (
                        <div className={styles.respostaForm}>
                          <textarea
                            className={styles.respostaInput}
                            placeholder="Escreva uma resposta..."
                            value={textoResposta}
                            onChange={e => setTextoResposta(e.target.value)}
                            maxLength={500}
                            rows={2}
                          />
                          <div className={styles.formActions}>
                            <button
                              type="button"
                              className={styles.btnAvaliar}
                              onClick={() => handleResponder(av.id)}
                              disabled={!textoResposta.trim() || enviandoResposta}
                            >
                              {enviandoResposta ? 'Enviando...' : 'Enviar'}
                            </button>
                            <button
                              type="button"
                              className={styles.btnCancelar}
                              onClick={() => { setRespostaAberta(null); setTextoResposta(''); }}
                            >
                              Cancelar
                            </button>
                          </div>
                        </div>
                      )}
                      {av.respostas?.length > 0 && (
                        <div className={styles.respostaList}>
                          {av.respostas.map(r => {
                            const rIni = r.usuarioNome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
                            return (
                              <div key={r.id} className={styles.respostaItem}>
                                <div className={styles.avaliacaoAvatar}>
                                  {r.avatarBase64
                                    ? <img src={r.avatarBase64} alt={r.usuarioNome} className={styles.avaliacaoAvatarImg} />
                                    : <div className={styles.avaliacaoAvatarIni} style={{ width: 26, height: 26, fontSize: 10 }}>{rIni}</div>
                                  }
                                </div>
                                <div className={styles.avaliacaoBody}>
                                  <div className={styles.avaliacaoMeta}>
                                    <span className={styles.avaliacaoNome}>{r.usuarioNome}</span>
                                    {r.usuarioNickname && <span className={styles.avaliacaoNick}>@{r.usuarioNickname}</span>}
                                    <span className={styles.avaliacaoData}>{new Date(r.criadoEm).toLocaleDateString('pt-BR')}</span>
                                  </div>
                                  <p className={styles.avaliacaoTexto}>{r.texto}</p>
                                  {r.minha && (
                                    <button
                                      type="button"
                                      className={styles.btnExcluir2}
                                      onClick={() => handleExcluirResposta(av.id, r.id)}
                                    >
                                      Excluir
                                    </button>
                                  )}
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {recoBooks.length > 0 && (
          <div className={styles.recoSection}>
            <h2 className={styles.sectionTitle}>Você também pode gostar</h2>
            <div className={styles.recoCarouselWrap}>
              {recoBooks.length > 3 && (
                <button type="button" className={styles.recoArrowLeft} onClick={() => scrollReco(-1)} aria-label="Anterior">‹</button>
              )}
              <div className={styles.recoCarousel} ref={recoCarouselRef}>
                {recoBooks.map(b => (
                  <button
                    key={b.id}
                    type="button"
                    className={styles.recoCard}
                    onClick={() => goToBook(b)}
                  >
                    <img
                      src={b.cover}
                      alt={b.title}
                      className={styles.recoCover}
                      onError={e => { e.target.src = 'https://via.placeholder.com/120x180?text=Sem+Capa'; }}
                    />
                    <p className={styles.recoTitle}>{b.title}</p>
                    <p className={styles.recoAuthor}>{b.author}</p>
                  </button>
                ))}
              </div>
              {recoBooks.length > 3 && (
                <button type="button" className={styles.recoArrowRight} onClick={() => scrollReco(1)} aria-label="Próximo">›</button>
              )}
            </div>
          </div>
        )}

        <AdBanner variant="banner" />
        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>

      {toastVisible && (
        <div className={styles.toast}>
          <div className={styles.toastContent}>
            <strong className={styles.toastTitle}>Adicionado!</strong>
            <p className={styles.toastMsg}>O livro foi adicionado à sua lista de leituras</p>
          </div>
          <div className={styles.toastActions}>
            <button className={styles.btnToastSecondary} onClick={handleRemoverFavoritos}>Remover da lista</button>
          </div>
        </div>
      )}

      {confirmDelete && (
        <Modal
          title="Excluir livro"
          message={`Tem certeza que deseja excluir "${book.title}" permanentemente? Esta ação não pode ser desfeita.`}
          onConfirm={handleExcluirPermanente}
          onCancel={() => setConfirmDelete(false)}
          confirmLabel="Excluir"
          cancelLabel="Cancelar"
          danger
        />
      )}
    </div>

    {prevBook && (
      <button
        className={styles.navArrowLeft}
        onClick={() => goToBook(prevBook)}
        title={prevBook.title}
        aria-label={`Livro anterior: ${prevBook.title}`}
      >
        ‹
        <span className={styles.navArrowLabel}>{prevBook.title}</span>
      </button>
    )}
    {nextBook && (
      <button
        className={styles.navArrowRight}
        onClick={() => goToBook(nextBook)}
        title={nextBook.title}
        aria-label={`Próximo livro: ${nextBook.title}`}
      >
        ›
        <span className={styles.navArrowLabel}>{nextBook.title}</span>
      </button>
    )}
    </>
  );
}