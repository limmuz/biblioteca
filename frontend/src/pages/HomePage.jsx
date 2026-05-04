import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import AppHeader from "../components/shared/AppHeader";
import Footer from "../components/Footer/Footer";
import BookCardMini from "../components/shared/BookCardMini";
import styles from "./HomePage.module.css";
import api from "../services/api";

export default function HomePage() {
  const navigate = useNavigate();
  const [recentlyRead, setRecentlyRead] = useState([]);
  const [readingList, setReadingList] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const carouselRef = useRef(null);
  const scrollIntervalRef = useRef(null);

  const isLibraryEmpty = 
    recentlyRead.length === 0 && 
    readingList.length === 0 && 
    recommendations.length === 0;

  // Auto-scroll animation for "Últimas leituras"
  const autoScrollRef = useRef(null);
  const isPausedRef = useRef(false);

  useEffect(() => {
    const el = carouselRef.current;
    if (!el || recentlyRead.length === 0) return;

    const startAutoScroll = () => {
      autoScrollRef.current = setInterval(() => {
        if (isPausedRef.current) return;
        if (el.scrollLeft + el.clientWidth >= el.scrollWidth - 2) {
          el.scrollLeft = 0;
        } else {
          el.scrollLeft += 1;
        }
      }, 20);
    };

    startAutoScroll();
    return () => clearInterval(autoScrollRef.current);
  }, [recentlyRead]);

  const stopScroll = () => { isPausedRef.current = false; };
  const handleCarouselMouseMove = () => { isPausedRef.current = true; };
  const handleCarouselMouseLeave = () => { isPausedRef.current = false; };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await api.get("/livros");
        const todosOsLivros = res.data;

        setRecentlyRead(todosOsLivros.filter((b) => b.status === "LIDO"));

        setReadingList(
          todosOsLivros.filter(
            (b) => b.status === "LENDO" || b.status === "QUERO LER"
          )
        );

        const recs = todosOsLivros.filter((b) => b.status === "RECOMENDADO");
        setRecommendations(recs.length > 0 ? recs : todosOsLivros.slice(0, 4));

      } catch (err) {
        console.error("Erro ao carregar dados do MongoDB:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading)
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <h2 style={{ color: "white", padding: "100px" }}>Carregando sua biblioteca... 📚</h2>
        </main>
      </div>
    );

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        
        {recentlyRead.length > 0 && (
          <>
            <h2 className={styles.sectionTitle}>Últimas leituras</h2>
            <div
              className={styles.carouselRect}
              ref={carouselRef}
              onMouseEnter={handleCarouselMouseMove}
              onMouseLeave={handleCarouselMouseLeave}
              aria-label="Últimas leituras"
            >
              {recentlyRead.map((book) => (
                <div
                  key={book.id}
                  className={styles.carouselRectCard}
                  onClick={() => navigate(`/livro/${book.id}`)}
                  role="button"
                  tabIndex={0}
                >
                  <img
                    src={book.cover}
                    alt={book.title}
                    className={styles.carouselRectImage}
                  />
                  <div className={styles.carouselRectInfo}>
                    <p className={styles.carouselRectTitle}>{book.title}</p>
                    <p className={styles.carouselRectAuthor}>{book.author}</p>
                    <p className={styles.carouselRectExcerpt}>{book.excerpt}</p>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        <h2 className={styles.sectionTitle}>Sua lista de leitura</h2>
        <div className={styles.bookGrid}>
          {readingList.length > 0 ? (
            readingList.map((book) => (
              <BookCardMini key={book.id} book={book} />
            ))
          ) : (
            <div className={styles.emptyState}>
              <span className={styles.emptyIcon}>📖</span>
              <p className={styles.emptyText}>Nenhum livro sendo lido no momento.</p>
              <p className={styles.emptySubText}>Adicione um livro à sua lista de leitura!</p>
            </div>
          )}
        </div>

        {recommendations.length > 0 && (
          <>
            <h2 className={styles.sectionTitle}>Recomendações</h2>
            <div className={styles.bookGrid}>
              {recommendations.slice(0, 4).map((book) => (
                <BookCardMini key={book.id + "-rec"} book={book} />
              ))}
            </div>
          </>
        )}

        <button
          className={styles.moreBtn}
          onClick={() => navigate(isLibraryEmpty ? "/novo-livro" : "/listagem")}
        >
          {isLibraryEmpty ? "Cadastrar meu primeiro livro" : "Ver mais recomendações"}
        </button>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}