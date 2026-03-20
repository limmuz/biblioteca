import React, { useState, useEffect } from "react";
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

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. Busca seus livros do MongoDB
        const resMeusLivros = await api.get("/livros");
        const meusLivros = resMeusLivros.data;

        // Filtra o que é seu por status
        setRecentlyRead(meusLivros.filter((b) => b.status === "LIDO"));
        setReadingList(
          meusLivros.filter(
            (b) => b.status === "LENDO" || b.status === "QUERO LER",
          ),
        );

        // 2. Busca recomendações reais da API externa (Open Library) via seu backend
        // Vamos usar "Love" (Romance) como padrão para recomendações
        const resRec = await api.get("/livros/externos/love");
        setRecommendations(resRec.data);
      } catch (err) {
        console.error("Erro ao carregar dados:", err);
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
          <h2 style={{ color: "white", padding: "100px" }}>Carregando...</h2>
        </main>
      </div>
    );

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>

        {/* ── Últimas leituras (Carrossel) ─────────────────── */}
        {recentlyRead.length > 0 && (
          <>
            <h2 className={styles.sectionTitle}>Últimas leituras</h2>
            <div className={styles.carouselRect} aria-label="Últimas leituras">
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
        
        {/* ── Sua lista de leitura (Grid) ──────────────────── */}
        <h2 className={styles.sectionTitle}>Sua lista de leitura</h2>
        <div className={styles.bookGrid}>
          {readingList.length > 0 ? (
            readingList.map((book) => (
              <BookCardMini key={book.id} book={book} />
            ))
          ) : (
            <p style={{ color: "var(--color-sage)", padding: "20px" }}>
              Sua lista está vazia. Adicione livros!
            </p>
          )}
        </div>

        {/* ── Recomendações (API Open Library) ─────────────────────── */}
        <h2 className={styles.sectionTitle}>Recomendações da Open Library</h2>
        <div className={styles.bookGrid}>
          {recommendations.slice(0, 4).map((book) => (
            <BookCardMini key={book.id + "-rec"} book={book} />
          ))}
        </div>

        <button
          className={styles.moreBtn}
          onClick={() =>
            navigate("/listagem", { state: { query: "trending" } })
          }
        >
          Ver mais recomendações
        </button>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}
