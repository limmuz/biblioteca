import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import BookCardMini from '../components/shared/BookCardMini';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import api from '../services/api';
import AdBanner from '../components/shared/AdBanner';
import styles from './ListagemPage.module.css';

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

export default function ListagemPage() {
  const location = useLocation();
  const [livros, setLivros] = useState([]);
  const [medias, setMedias] = useState(() => lerCache('lybre_medias_cache') || {});
  const [loading, setLoading] = useState(false);

  const queryBusca = location.state?.query || "";

  useEffect(() => {
    const carregarResultados = async () => {
      setLoading(true);
      try {
        const [resLivros, resMedias] = await Promise.all([
          queryBusca
            ? api.get('/livros?search=' + queryBusca)
            : api.get('/livros'),
          api.get('/avaliacoes/medias').catch(() => ({ data: [] })),
        ]);

        const livrosFiltrados = queryBusca
          ? resLivros.data
          : resLivros.data.filter(b => b.status !== 'RECOMENDADO');
        setLivros(livrosFiltrados);

        const map = {};
        resMedias.data.forEach(m => {
          const key = `${(m.livroTitulo || '').toLowerCase()}||${(m.livroAutor || '').toLowerCase()}`;
          map[key] = m;
        });
        salvarCache('lybre_medias_cache', map);
        setMedias(map);
      } catch (err) {
        console.error("Erro ao carregar:", err);
      } finally {
        setLoading(false);
      }
    };

    carregarResultados();
  }, [queryBusca]);

  const getRating = (book) => {
    const key = `${(book.title || '').toLowerCase()}||${(book.author || '').toLowerCase()}`;
    return medias[key] || { media: 0, total: 0 };
  };

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <h1 className={styles.title}>
          {queryBusca ? `Resultados para: "${queryBusca}"` : "Sua Biblioteca"}
        </h1>

        {loading ? (
          <LoadingSpinner />
        ) : (
          <div className={styles.bookGrid}>
            {livros.length > 0 ? (
              livros.map(book => {
                const r = getRating(book);
                return (
                  <BookCardMini
                    key={book.id}
                    book={book}
                    rating={r.media}
                    totalRatings={r.total}
                  />
                );
              })
            ) : (
              <p style={{ color: 'var(--color-sage)', textAlign: 'center', width: '100%' }}>
                Nenhum livro encontrado para esta busca.
              </p>
            )}
          </div>
        )}
      </main>
      <AdBanner variant="banner" />
      <div className={styles.footerWrap}>
        <Footer />
      </div>
    </div>
  );
}
