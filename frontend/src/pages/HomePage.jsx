import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import AppHeader from "../components/shared/AppHeader";
import Footer from "../components/Footer/Footer";
import BookCardMini from "../components/shared/BookCardMini";
import LoadingSpinner from "../components/shared/LoadingSpinner";
import styles from "./HomePage.module.css";
import api from "../services/api";

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

export default function HomePage() {
  const navigate = useNavigate();
  const [recentlyRead, setRecentlyRead] = useState([]);
  const [readingList, setReadingList] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [medias, setMedias] = useState(() => {
    const cached = lerCache('lybre_medias_cache') || {};
    const vals = Object.values(cached);
    const temLivroId = vals.length === 0 || vals.some(m => m.livroId);
    return temLivroId ? cached : {};
  });
  const [loading, setLoading] = useState(() => lerCache('lybre_livros_cache') === null);
  const [showAllRecs, setShowAllRecs] = useState(false);
  const carouselRef = useRef(null);
  const autoScrollRef = useRef(null);
  const isPausedRef = useRef(false);

  const isLibraryEmpty =
    recentlyRead.length === 0 &&
    readingList.length === 0 &&
    recommendations.length === 0;

  useEffect(() => {
    const el = carouselRef.current;
    if (!el || recentlyRead.length === 0) return;
    autoScrollRef.current = setInterval(() => {
      if (isPausedRef.current) return;
      if (el.scrollLeft + el.clientWidth >= el.scrollWidth - 2) {
        el.scrollLeft = 0;
      } else {
        el.scrollLeft += 1;
      }
    }, 20);
    return () => clearInterval(autoScrollRef.current);
  }, [recentlyRead]);

  useEffect(() => {
    const cachedLivros = lerCache('lybre_livros_cache');
    if (cachedLivros) {
      setRecentlyRead(cachedLivros.filter((b) => b.status === "LIDO"));
      setReadingList(cachedLivros.filter((b) => b.status === "LENDO" || b.status === "QUERO LER"));
    }
    const norm = s => s?.toLowerCase().trim().normalize('NFD').replace(/[̀-ͯ]/g, '') ?? '';
    const chaveRec = l => norm(l?.title) + '||' + norm(l?.author);
    const fetchData = async () => {
      try {
        const [resLivros, resMedias] = await Promise.all([
          api.get("/livros"),
          api.get("/avaliacoes/medias").catch(() => ({ data: [] })),
        ]);
        const all = resLivros.data;
        salvarCache('lybre_livros_cache', all);
        setRecentlyRead(all.filter((b) => b.status === "LIDO"));
        setReadingList(all.filter((b) => b.status === "LENDO" || b.status === "QUERO LER"));
        let recsRaw = [];
        try {
          const resNaoTenho = await api.get('/livros/nao-tenho');
          recsRaw = resNaoTenho.data;
        } catch {
          const resDescobrir = await api.get('/livros/descobrir').catch(() => ({ data: [] }));
          const minhasChaves = new Set(all.map(b => chaveRec(b)));
          recsRaw = resDescobrir.data.filter(l => l.cover && !minhasChaves.has(chaveRec(l)));
        }
        const vistos = new Set();
        setRecommendations(recsRaw.filter(l => {
          if (!l.cover) return false;
          const k = chaveRec(l);
          if (vistos.has(k)) return false;
          vistos.add(k);
          return true;
        }));
        const map = {};
        resMedias.data.forEach(m => {
          const key = `${(m.livroTitulo || '').toLowerCase()}||${(m.livroAutor || '').toLowerCase()}`;
          map[key] = m;
        });
        salvarCache('lybre_medias_cache', map);
        setMedias(map);
      } catch {
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const getRating = (book) => {
    const key = `${book.title.toLowerCase()}||${book.author.toLowerCase()}`;
    return medias[key] || { media: 0, total: 0 };
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <LoadingSpinner message="Carregando sua biblioteca..." />
        </main>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <AppHeader />
      <div className={styles.layout}>
      <main className={styles.main}>

        {recentlyRead.length > 0 && (
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>Últimas leituras</h2>
            <div
              className={styles.carouselRect}
              ref={carouselRef}
              role="region"
              onMouseEnter={() => { isPausedRef.current = true; }}
              onMouseLeave={() => { isPausedRef.current = false; }}
              aria-label="Últimas leituras"
            >
              {recentlyRead.map((book) => {
                const r = getRating(book);
                return (
                  <button
                    key={book.id}
                    className={styles.carouselRectCard}
                    onClick={() => navigate(`/livro/${book.id}`, { state: { bookData: book, bookList: recentlyRead } })}
                    type="button"
                  >
                    <img
                      src={book.cover}
                      alt={book.title}
                      className={styles.carouselRectImage}
                      loading="lazy"
                      onError={(e) => { e.target.src = '/books/book-sherlock.png'; }}
                    />
                    <div className={styles.carouselRectInfo}>
                      <p className={styles.carouselRectTitle}>{book.title}</p>
                      <p className={styles.carouselRectAuthor}>por {book.author}</p>
                      {r.media > 0 && (
                        <p className={styles.carouselRectRating}>
                          {'★'.repeat(Math.round(r.media))}{'☆'.repeat(5 - Math.round(r.media))} {r.media.toFixed(1)}
                        </p>
                      )}
                      <p className={styles.carouselRectExcerpt}>{book.excerpt}</p>
                    </div>
                  </button>
                );
              })}
            </div>
          </section>
        )}

        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>Sua lista de leitura</h2>
          <div className={styles.bookGrid}>
            {readingList.length > 0 ? (
              readingList.map((book) => {
                const r = getRating(book);
                return <BookCardMini key={book.id} book={book} rating={r.media} totalRatings={r.total} bookList={readingList} />;
              })
            ) : (
              <div className={styles.emptyState}>
                <span className={styles.emptyIcon}>📖</span>
                <p className={styles.emptyText}>Nenhum livro na lista ainda.</p>
                <p className={styles.emptySubText}>Adicione um livro à sua lista de leitura!</p>
              </div>
            )}
          </div>
        </section>

        {recommendations.length > 0 && (
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>{isLibraryEmpty ? 'Descubra livros' : 'Recomendações'}</h2>
            <div className={styles.bookGrid}>
              {(showAllRecs ? recommendations : recommendations.slice(0, 4)).map((book) => {
                const r = getRating(book);
                return (
                  <BookCardMini
                    key={book.id + "-rec"}
                    book={book}
                    isFromPublicProfile={true}
                    rating={r.media}
                    totalRatings={r.total}
                    bookList={recommendations}
                  />
                );
              })}
            </div>
            {recommendations.length > 4 && (
              <button
                className={styles.moreBtn}
                onClick={() => setShowAllRecs((v) => !v)}
              >
                {showAllRecs ? "Ver menos" : "Mais recomendações"}
              </button>
            )}
          </section>
        )}

        {isLibraryEmpty && (
          <button
            className={styles.moreBtn}
            onClick={() => navigate("/novo-livro")}
          >
            Cadastrar meu primeiro livro
          </button>
        )}

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>

      </div>

    </div>
  );
}
