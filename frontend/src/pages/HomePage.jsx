import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import AppHeader from "../components/shared/AppHeader";
import Footer from "../components/Footer/Footer";
import BookCardMini from "../components/shared/BookCardMini";
import LoadingSpinner from "../components/shared/LoadingSpinner";
import Modal from "../components/shared/Modal";
import AdBanner from "../components/shared/AdBanner";
import styles from "./HomePage.module.css";
import api from "../services/api";

export default function HomePage() {
  const navigate = useNavigate();
  const [recentlyRead, setRecentlyRead] = useState([]);
  const [readingList, setReadingList] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [medias, setMedias] = useState({});
  const [loading, setLoading] = useState(true);
  const [errorModal, setErrorModal] = useState(null);
  const [showAllRecs, setShowAllRecs] = useState(false);
  const carouselRef = useRef(null);
  const autoScrollRef = useRef(null);
  const isPausedRef = useRef(false);

  const isLibraryEmpty =
    recentlyRead.length === 0 &&
    readingList.length === 0 &&
    recommendations.length === 0;

  const refreshLists = async () => {
    const res = await api.get("/livros");
    const all = res.data;
    setRecentlyRead(all.filter((b) => b.status === "LIDO"));
    setReadingList(all.filter((b) => b.status === "LENDO" || b.status === "QUERO LER"));
    const recs = all.filter((b) => b.status === "RECOMENDADO");
    const naLista = new Set(["LIDO", "LENDO", "QUERO LER"]);
    setRecommendations(recs.length > 0 ? recs : all.filter((b) => !naLista.has(b.status)).slice(0, 4));
  };

  const handleAdicionarLivroNaLista = async (bookToAdd) => {
    try {
      await api.put(`/livros/${bookToAdd.id}`, { ...bookToAdd, status: "QUERO LER" });
      await refreshLists();
    } catch (err) {
      console.error("Erro ao adicionar livro à lista de leitura:", err);
      setErrorModal("Erro ao adicionar livro à lista de leitura. Tente novamente.");
    }
  };

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
    const fetchData = async () => {
      try {
        await refreshLists();
        const res = await api.get("/avaliacoes/medias");
        const map = {};
        res.data.forEach(m => {
          const key = `${m.livroTitulo.toLowerCase()}||${m.livroAutor.toLowerCase()}`;
          map[key] = m;
        });
        setMedias(map);
      } catch (err) {
        console.error("Erro ao carregar dados:", err);
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
                    onClick={() => navigate(`/livro/${book.id}`)}
                    type="button"
                  >
                    <img
                      src={book.cover}
                      alt={book.title}
                      className={styles.carouselRectImage}
                      onError={(e) => { e.target.src = 'https://via.placeholder.com/140x210?text=Sem+Capa'; }}
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
                return <BookCardMini key={book.id} book={book} rating={r.media} totalRatings={r.total} />;
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
            <h2 className={styles.sectionTitle}>Recomendações</h2>
            <div className={styles.bookGrid}>
              {(showAllRecs ? recommendations : recommendations.slice(0, 4)).map((book) => {
                const r = getRating(book);
                return (
                  <BookCardMini
                    key={book.id + "-rec"}
                    book={book}
                    onAddToList={handleAdicionarLivroNaLista}
                    rating={r.media}
                    totalRatings={r.total}
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

      <aside className={styles.sidebar}>
        <AdBanner variant="sidebar" />
        <AdBanner variant="sidebar" />
      </aside>
      </div>

      {errorModal && (
        <Modal
          title="Ops!"
          message={errorModal}
          onConfirm={() => setErrorModal(null)}
          confirmLabel="Entendi"
          singleButton
        />
      )}
    </div>
  );
}
