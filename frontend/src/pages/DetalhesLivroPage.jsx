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

  useEffect(() => {
    if (book) {
      setLoading(false);
      return;
    }

    api.get(`/livros/${id}`)
      .then((response) => {
        setBook(response.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Erro ao buscar detalhes:", err);
        setError(true);
        setLoading(false);
      });
  }, [id, book]);

  // FUNÇÃO PARA ADICIONAR AO MONGODB + POP-UP
  const handleAdicionarBiblioteca = async () => {
    setAdicionando(true);
    try {
      const payload = {
        title: book.title,
        author: book.author,
        cover: book.cover,
        excerpt: book.excerpt || book.summary || "Sinopse não disponível.",
        status: "QUERO LER"
      };

      await api.post('/livros', payload);
      
      // Pop-up de aviso
      alert(`📚 Sucesso! "${book.title}" foi adicionado à sua biblioteca.`);
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
              <button
                className={styles.btnLer}
                onClick={() => navigate(`/leitura/${book.id}`)}
              >
                Ler
              </button>
              <button 
                className={styles.btnAdicionar} 
                onClick={handleAdicionarBiblioteca}
                disabled={adicionando}
              >
                {adicionando ? 'Adicionando...' : 'Adicionar a lista de leitura'}
              </button>
            </div>
          </div>
        </div>

        <div className={styles.detailsCard}>
          <div className={styles.sinopseCol}>
            <h2 className={styles.sinopseTitle}>Sinopse</h2>
            <hr className={styles.sinopseDivider} />
            <p className={styles.sinopseText}>
              {book.excerpt || book.summary || "Sinopse não disponível para este título."}
            </p>
          </div>

          <div className={styles.metaCol}>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Status</span>
              <span className={styles.metaValue} style={{ color: 'var(--color-sage)' }}>
                {book.status || "RECOMENDADO"}
              </span>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Idioma</span>
              <span className={styles.metaValue}>{book.language || 'Português'}</span>
            </div>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Páginas</span>
              <span className={styles.metaValue}>{book.pages || '--'}</span>
            </div>
            
            <div className={styles.metaButtons}>
               <button className={styles.btnLer} onClick={() => navigate(`/leitura/${book.id}`)}>Ler</button>
               <button className={styles.btnAdicionar} onClick={handleAdicionarBiblioteca}>Adicionar à lista</button>
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