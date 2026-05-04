import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import AppHeader from "../shared/AppHeader";
import Footer from "../Footer/Footer";
import api from "../../services/api";
import styles from "./ReadingPage.module.css";

export default function ReadingPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [book, setBook] = useState(location.state?.bookData || null);
  const [showFinishModal, setShowFinishModal] = useState(false);

  useEffect(() => {
    const cleanId = id.replaceAll('works', '').replaceAll('/', '');
    if (!book) {
      api
        .get(`/livros/${cleanId}`)
        .then((res) => setBook(res.data))
        .catch(() => {
          console.error("Livro não encontrado para leitura.");
          navigate("/home");
        });
    }
  }, [id, book, navigate]);

  const handleFinishClick = () => setShowFinishModal(true);

  const handleConfirmFinish = async () => {
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: 'LIDO' });
    } catch (err) {
      console.error(err);
    }
    navigate(-1);
  };

  const handleCancelFinish = () => setShowFinishModal(false);

  if (!book) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <p className={styles.loading}>Iniciando leitor...</p>
        </main>
      </div>
    );
  }

  const paragraphs = (book.excerpt || book.summary || "O conteúdo integral deste livro não está disponível para leitura offline no momento.")
    .split(/\n+/)
    .filter((p) => p.trim().length > 0);

  return (
    <div className={styles.page}>
      <AppHeader />

      <main className={styles.main}>
        <div className={styles.bookHeader}>
          <h1 className={styles.bookTitle}>{book.title}</h1>
          <p className={styles.bookAuthor}>por {book.author}</p>
        </div>

        {/* Card de leitura com fundo amarelo/bege */}
        <div className={styles.contentCard}>
          {paragraphs.map((para, idx) => (
            <p key={idx} className={styles.paragraph}>
              {para}
            </p>
          ))}
        </div>

        <button
          className={styles.finishBtn}
          onClick={handleFinishClick}
          type="button"
        >
          Concluir leitura
        </button>

        <p className={styles.footerNote}>Histórias sem limites, cada a leitura é livre!</p>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>

      {/* Modal de confirmação */}
      {showFinishModal && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2 className={styles.modalTitle}>Atenção!</h2>
            <p className={styles.modalText}>
              Ao terminar a leitura o livro sairá da sua lista de leituras
            </p>
            <div className={styles.modalActions}>
              <button
                className={styles.btnModalConfirm}
                onClick={handleConfirmFinish}
                type="button"
              >
                Terminar leitura
              </button>
              <button
                className={styles.btnModalCancel}
                onClick={handleCancelFinish}
                type="button"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
