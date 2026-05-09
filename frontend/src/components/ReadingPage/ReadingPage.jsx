import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import AppHeader from "../shared/AppHeader";
import Footer from "../Footer/Footer";
import LoadingSpinner from "../shared/LoadingSpinner";
import api from "../../services/api";
import { gerarTextoLivro, paginar } from "../../utils/gerarTextoLivro";
import styles from "./ReadingPage.module.css";

export default function ReadingPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [book, setBook] = useState(location.state?.bookData || null);
  const [paginas, setPaginas] = useState([]);
  const [paginaAtual, setPaginaAtual] = useState(0);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const cleanId = id.replaceAll("works", "").replaceAll("/", "");
    if (!book) {
      api
        .get(`/livros/${cleanId}`)
        .then((res) => setBook(res.data))
        .catch(() => navigate("/home"));
    }
  }, [id, book, navigate]);

  useEffect(() => {
    if (book) {
      const paragrafos = gerarTextoLivro(book);
      setPaginas(paginar(paragrafos, 300));
      setPaginaAtual(0);
    }
  }, [book]);

  const irParaPagina = useCallback((n) => {
    setPaginaAtual(n);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }, []);

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === "ArrowRight" && paginaAtual < paginas.length - 1)
        irParaPagina(paginaAtual + 1);
      if (e.key === "ArrowLeft" && paginaAtual > 0)
        irParaPagina(paginaAtual - 1);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [paginaAtual, paginas.length, irParaPagina]);

  const handleConfirmFinish = async () => {
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: "LIDO" });
    } catch (err) {
      console.error(err);
    }
    navigate(-1);
  };

  if (!book || paginas.length === 0) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <LoadingSpinner />
      </div>
    );
  }

  const total = paginas.length;
  const progresso = Math.round(((paginaAtual + 1) / total) * 100);
  const pagAtual = paginas[paginaAtual] || [];
  const isUltima = paginaAtual === total - 1;

  return (
    <div className={styles.page}>
      <AppHeader />

      <main className={styles.main}>
        <div className={styles.bookHeader}>
          <h1 className={styles.bookTitle}>{book.title}</h1>
          <p className={styles.bookAuthor}>por {book.author}</p>
        </div>

        <div className={styles.progressWrap}>
          <div className={styles.progressBar}>
            <div className={styles.progressFill} style={{ width: `${progresso}%` }} />
          </div>
          <span className={styles.progressLabel}>
            Página {paginaAtual + 1} de {total} · {progresso}% lido
          </span>
        </div>

        <div className={styles.contentCard}>
          {pagAtual.map((par, i) => {
            if (par === "—" || par === "* * *") {
              return <div key={i} className={styles.separator}>{par}</div>;
            }
            return (
              <p key={i} className={styles.paragraph}>
                {par}
              </p>
            );
          })}
        </div>

        <div className={styles.nav}>
          <button
            className={styles.navBtn}
            onClick={() => irParaPagina(paginaAtual - 1)}
            disabled={paginaAtual === 0}
          >
            ‹ Anterior
          </button>

          <div className={styles.pageNumbers}>
            {(() => {
              const range = [];
              const half = 3;
              let start = Math.max(0, paginaAtual - half);
              let end = Math.min(total - 1, start + 6);
              start = Math.max(0, end - 6);
              for (let i = start; i <= end; i++) range.push(i);
              return range.map((pg) => (
                <button
                  key={pg}
                  className={`${styles.pageNum} ${pg === paginaAtual ? styles.pageNumActive : ""}`}
                  onClick={() => irParaPagina(pg)}
                >
                  {pg + 1}
                </button>
              ));
            })()}
          </div>

          {isUltima ? (
            <button className={styles.finishBtn} onClick={() => setShowModal(true)}>
              Concluir ✓
            </button>
          ) : (
            <button
              className={styles.navBtn}
              onClick={() => irParaPagina(paginaAtual + 1)}
            >
              Próxima ›
            </button>
          )}
        </div>

        <p className={styles.footerNote}>
          Use as setas ← → do teclado para navegar
        </p>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>

      {showModal && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2 className={styles.modalTitle}>Concluir leitura?</h2>
            <p className={styles.modalText}>
              O livro será marcado como <strong>Lido</strong> na sua biblioteca.
            </p>
            <div className={styles.modalActions}>
              <button className={styles.btnModalConfirm} onClick={handleConfirmFinish}>
                Confirmar
              </button>
              <button className={styles.btnModalCancel} onClick={() => setShowModal(false)}>
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
