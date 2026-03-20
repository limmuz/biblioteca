import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import styles from './DetalhesLivroPage.module.css';

export default function DetalhesLivroPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [book, setBook] = useState(location.state?.bookData || null);
  const [loading, setLoading] = useState(!book);
  const [error, setError] = useState(false);
  const [adicionando, setAdicionando] = useState(false);
  const [statusSelecionado, setStatusSelecionado] = useState(book?.status || "RECOMENDADO");

  // Carregar livro se não veio pelo estado
  useEffect(() => {
    if (book) {
      setLoading(false);
      return;
    }

    api.get(`/livros/${id}`)
      .then((res) => {
        setBook(res.data);
        setStatusSelecionado(res.data.status || "RECOMENDADO");
        setLoading(false);
      })
      .catch((err) => {
        console.error("Erro ao buscar detalhes:", err);
        setError(true);
        setLoading(false);
      });
  }, [id, book]);

  // Função para adicionar ou atualizar status do livro
  const handleAdicionarBiblioteca = async (newStatus) => {
    if (!book) return;
    setAdicionando(true);

    try {
      const payload = {
        title: book.title,
        author: book.author,
        cover: book.cover,
        excerpt: book.excerpt || "Sinopse não disponível",
        status: newStatus,
        pages: book.pages,
        language: book.language,
      };

      await api.put('/livros/${book.id}', payload);

      alert(`📚 "${book.title}" adicionado/atualizado com status "${newStatus}"`);
      navigate('/home');
    } catch (err) {
      console.error(err);
      alert("Erro ao adicionar livro. Verifique a conexão com o backend.");
    } finally {
      setAdicionando(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main} style={{ display: 'flex', justifyContent: 'center', color: 'white', paddingTop: '150px' }}>
          <h2>Carregando detalhes...</h2>
        </main>
      </div>
    );
  }

  if (error || !book) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: 'white', paddingTop: '150px' }}>
          <h2>Livro não encontrado</h2>
          <button onClick={() => navigate('/home')} className={styles.btnLer} style={{ marginTop: '20px' }}>Voltar para Home</button>
        </main>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main} style={{ paddingTop: '100px' }}>
        <div className={styles.hero}>
          <img src={book.cover} alt={book.title} className={styles.bookCover} />
          <div className={styles.heroInfo}>
            <h1 className={styles.bookTitle}>{book.title}</h1>
            <p className={styles.bookAuthor}>por {book.author}</p>

            <div className={styles.heroButtons}>
              <button className={styles.btnLer} onClick={() => navigate(`/leitura/${book.id}`)}>
                Ler
              </button>
            </div>
          </div>
        </div>

        <div className={styles.detailsCard}>
          {/* Coluna Sinopse */}
          <div className={styles.sinopseCol}>
            <h2 className={styles.sinopseTitle}>Sinopse</h2>
            <hr className={styles.sinopseDivider} />
            <p className={styles.sinopseText}>{book.excerpt || "Sinopse não disponível."}</p>
          </div>

          {/* Coluna Meta */}
          <div className={styles.metaCol}>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Status</span>
              <select
                value={statusSelecionado}
                onChange={(e) => setStatusSelecionado(e.target.value)}
                style={{ marginLeft: '10px', padding: '4px 8px', borderRadius: '8px', cursor: 'pointer' }}
              >
                <option value="LIDO">LIDO</option>
                <option value="LENDO">LENDO</option>
                <option value="QUERO LER">QUERO LER</option>
                <option value="RECOMENDADO">RECOMENDADO</option>
              </select>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Idioma</span>
              <span className={styles.metaValue}>{book.language || "Desconhecido"}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Páginas</span>
              <span className={styles.metaValue}>{book.pages || "--"}</span>
            </div>

            <div className={styles.metaButtons}>
              <button className={styles.btnAdicionar} onClick={handleAdicionarBiblioteca} disabled={adicionando}>
                {adicionando ? 'Adicionando...' : 'Adicionar/Atualizar na lista'}
              </button>
            </div>
          </div>
        </div>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>
    </div>
  );
}