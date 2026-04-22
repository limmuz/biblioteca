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

  const stopScroll = () => {
    clearInterval(scrollIntervalRef.current);
    scrollIntervalRef.current = null;
  };

  const handleCarouselMouseMove = (e) => {
    const el = carouselRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const threshold = 80;

    if (x < threshold) {
      if (scrollIntervalRef.current) return;
      scrollIntervalRef.current = setInterval(() => {
        el.scrollLeft -= 5;
      }, 16);
    } else if (x > rect.width - threshold) {
      if (scrollIntervalRef.current) return;
      scrollIntervalRef.current = setInterval(() => {
        el.scrollLeft += 5;
      }, 16);
    } else {
      stopScroll();
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. Busca centralizada no seu banco MongoDB (Spring Boot)
        const res = await api.get("/livros");
        const todosOsLivros = res.data;

        // 2. Filtragem por Status conforme o Banco de Dados e Layout
        // Últimas leituras: Apenas os que já foram lidos
        setRecentlyRead(todosOsLivros.filter((b) => b.status === "LIDO"));

        // Sua lista de leitura: O que está em progresso ou planejado
        setReadingList(
          todosOsLivros.filter(
            (b) => b.status === "LENDO" || b.status === "QUERO LER"
          )
        );

        // Recomendações: Filtra livros marcados como RECOMENDADO no banco
        // Se não houver nenhum, ele pega os 4 primeiros para não deixar vazio
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
        
        {/* ── Últimas leituras (Carrossel Retangular) ─────────────────── */}
        {recentlyRead.length > 0 && (
          <>
            <h2 className={styles.sectionTitle}>Últimas leituras</h2>
            <div
              className={styles.carouselRect}
              ref={carouselRef}
              onMouseMove={handleCarouselMouseMove}
              onMouseLeave={stopScroll}
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

        {/* ── Sua lista de leitura (Grid de Cards) ──────────────────── */}
        <h2 className={styles.sectionTitle}>Sua lista de leitura</h2>
        <div className={styles.bookGrid}>
          {readingList.length > 0 ? (
            readingList.map((book) => (
              <BookCardMini key={book.id} book={book} />
            ))
          ) : (
            <p style={{ color: "var(--color-sage)", padding: "20px" }}>
              Nenhum livro sendo lido no momento. 📖
            </p>
          )}
        </div>

        {/* ── Recomendações (Fiel ao Figma) ─────────────────────── */}
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
          onClick={() => navigate("/listagem")}
        >
          Ver mais recomendações ✨
        </button>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}