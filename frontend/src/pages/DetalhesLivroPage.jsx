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
  const [toastVisible, setToastVisible] = useState(false);

  useEffect(() => {
    if (book) { setLoading(false); return; }
    api.get(`/livros/${id}`)
      .then((res) => { setBook(res.data); setLoading(false); })
      .catch(() => { setError(true); setLoading(false); });
  }, [id, book]);

  const handleAdicionarFavoritos = async () => {
    if (!book || adicionando) return;
    setAdicionando(true);
    try {
      await api.put(`/livros/${book.id}`, {
        title: book.title,
        author: book.author,
        cover: book.cover,
        excerpt: book.excerpt || 'Sinopse não disponível',
        status: 'QUERO LER',
        pages: book.pages,
        language: book.language,
        categories: book.categories,
        publisher: book.publisher,
        publishedDate: book.publishedDate,
      });
      setBook({ ...book, status: 'QUERO LER' });
      setToastVisible(true);
    } catch (err) {
      console.error(err);
      alert('Erro ao adicionar aos favoritos');
    } finally {
      setAdicionando(false);
    }
  };

  const handleComecarLer = async () => {
    setToastVisible(false);
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: 'LENDO' });
    } catch (err) {
      console.error(err);
    }
    navigate(`/leitura/${book.id}`);
  };

  const handleRemoverFavoritos = async () => {
    setToastVisible(false);
    try {
      await api.put(`/livros/${book.id}`, { ...book, status: 'LIDO' });
      navigate('/home');
    } catch (err) {
      console.error(err);
      alert('Erro ao remover dos favoritos');
    }
  };

  const handleEditar = () => {
    navigate(`/editar-livro/${book.id}`, { state: { bookData: book } });
  };

  const handleExcluirPermanente = async () => {
    const confirmar = window.confirm(
      'Tem certeza que deseja excluir este livro permanentemente? Esta ação não pode ser desfeita.'
    );
    if (!confirmar) return;

    try {
      await api.delete(`/livros/${book.id}`);
      alert('Livro excluído com sucesso!');
      navigate('/home');
    } catch (err) {
      console.error(err);
      alert('Erro ao excluir o livro');
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

  const categorias = Array.isArray(book.categories) && book.categories.length > 0
    ? book.categories
    : ['Nao informado'];
  const idioma = book.language || 'Nao informado';
  const editora = book.publisher || 'Nao informado';
  const publicacao = book.publishedDate || 'Nao informado';
  const paginas = book.pages || '--';

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>

        <div className={styles.hero}>
          <img src={book.cover} alt={book.title} className={styles.bookCover} />
          <div className={styles.heroInfo}>
            <h1 className={styles.bookTitle}>{book.title}</h1>
            <p className={styles.bookAuthor}>por {book.author}</p>
            <div className={styles.heroButtons}>
              <button className={styles.btnLer} onClick={() => navigate(`/leitura/${book.id}`)}>
                Ler
              </button>
              <button className={styles.btnAdicionar} onClick={handleAdicionarFavoritos} disabled={adicionando}>
                {adicionando ? 'Adicionando...' : 'Adicionar a lista de leitura'}
              </button>
            </div>
          </div>
        </div>

        <div className={styles.detailsCard}>

          <div className={styles.sinopseCol}>
            <h2 className={styles.sinopseTitle}>Sinopse</h2>
            <hr className={styles.sinopseDivider} />
            <p className={styles.sinopseText}>{book.excerpt || 'Sinopse não disponível.'}</p>
          </div>

          <div className={styles.metaCol}>
            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Categorias</span>
              <div className={styles.metaTagsWrap}>
                {categorias.map((cat) => (
                  <span key={cat} className={styles.metaTag}>{cat}</span>
                ))}
              </div>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Idioma</span>
              <span className={styles.metaValue}>{idioma}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Editora</span>
              <span className={styles.metaValue}>{editora}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Publicação</span>
              <span className={styles.metaValue}>{publicacao}</span>
            </div>

            <div className={styles.metaRow}>
              <span className={styles.metaLabel}>Páginas</span>
              <span className={styles.metaValue}>{paginas}</span>
            </div>

            <div className={styles.metaButtons}>
              <button className={styles.btnEditar} onClick={handleEditar}>
                Editar
              </button>
              <button className={styles.btnExcluir} onClick={handleExcluirPermanente}>
                Excluir
              </button>
            </div>
          </div>
        </div>

        <div className={styles.footerWrap}>
          <Footer />
        </div>
      </main>

      {toastVisible && (
        <div className={styles.toast}>
          <div className={styles.toastContent}>
            <strong className={styles.toastTitle}>Adicionado!</strong>
            <p className={styles.toastMsg}>O livro foi adicionado à sua lista de leituras</p>
          </div>
          <div className={styles.toastActions}>
            <button className={styles.btnToastPrimary} onClick={handleComecarLer}>
              Começar a ler
            </button>
            <button className={styles.btnToastSecondary} onClick={handleRemoverFavoritos}>
              Remover da lista
            </button>
          </div>
        </div>
      )}
    </div>
  );
}