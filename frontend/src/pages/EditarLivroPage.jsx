import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import styles from './NovoLivroPage.module.css';

export default function EditarLivroPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [carregando, setCarregando] = useState(false);
  const [loading, setLoading] = useState(!location.state?.bookData);
  
  const [livro, setLivro] = useState({
    title: '',
    author: '',
    cover: '',
    excerpt: '',
    status: 'QUERO LER',
    language: '',
    pages: '',
    publisher: '',
    publishedDate: '',
    categoriesInput: ''
  });

  useEffect(() => {
    if (location.state?.bookData) {
      const book = location.state.bookData;
      setLivro({
        title: book.title || '',
        author: book.author || '',
        cover: book.cover || '',
        excerpt: book.excerpt || '',
        status: book.status || 'QUERO LER',
        language: book.language || '',
        pages: book.pages || '',
        publisher: book.publisher || '',
        publishedDate: book.publishedDate || '',
        categoriesInput: Array.isArray(book.categories) ? book.categories.join(', ') : ''
      });
      setLoading(false);
    } else {
      api.get(`/livros/${id}`)
        .then((res) => {
          const book = res.data;
          setLivro({
            title: book.title || '',
            author: book.author || '',
            cover: book.cover || '',
            excerpt: book.excerpt || '',
            status: book.status || 'QUERO LER',
            language: book.language || '',
            pages: book.pages || '',
            publisher: book.publisher || '',
            publishedDate: book.publishedDate || '',
            categoriesInput: Array.isArray(book.categories) ? book.categories.join(', ') : ''
          });
          setLoading(false);
        })
        .catch((err) => {
          console.error(err);
          alert('Erro ao carregar livro');
          navigate('/home');
        });
    }
  }, [id, location.state, navigate]);

  const handleChange = (e) => {
    setLivro({ ...livro, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setCarregando(true);
    try {
      const payload = {
        title: livro.title,
        author: livro.author,
        cover: livro.cover,
        excerpt: livro.excerpt,
        status: livro.status,
        language: livro.language || 'Nao informado',
        pages: Number(livro.pages) || 0,
        publisher: livro.publisher || 'Nao informado',
        publishedDate: livro.publishedDate || 'Nao informado',
        categories: livro.categoriesInput
          ? livro.categoriesInput.split(',').map((c) => c.trim()).filter(Boolean)
          : ['Nao informado']
      };

      await api.put(`/livros/${id}`, payload);
      alert('Livro atualizado com sucesso!');
      navigate(`/livro/${id}`);
    } catch (error) {
      console.error("Erro ao atualizar:", error);
      let mensagemErro = 'Erro ao atualizar o livro. ';
      
      if (error.response?.status === 400) {
        mensagemErro += 'Verifique se todos os campos obrigatórios estão preenchidos corretamente.';
      } else if (error.response?.status === 404) {
        mensagemErro += 'Livro não encontrado.';
      } else if (error.message === 'Network Error') {
        mensagemErro += 'Verifique se o backend está rodando.';
      } else {
        mensagemErro += 'Tente novamente.';
      }
      
      alert(mensagemErro);
    } finally {
      setCarregando(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <h2 className={styles.title}>Carregando...</h2>
        </main>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <h2 className={styles.title}>Editar Livro</h2>
        
        <form onSubmit={handleSubmit} className={styles.form}>
          <label>
            Título
            <input className={styles.input} type="text" name="title" value={livro.title} onChange={handleChange} required />
          </label>

          <label>
            Autor
            <input className={styles.input} type="text" name="author" value={livro.author} onChange={handleChange} required />
          </label>

          <label>
            URL da Capa
            <input className={styles.input} type="url" name="cover" value={livro.cover} onChange={handleChange} required />
          </label>

          <label>
            Sinopse
            <textarea className={styles.textarea} name="excerpt" value={livro.excerpt} onChange={handleChange} required />
          </label>

          <label>
            Categorias (separe por virgula)
            <input className={styles.input} type="text" name="categoriesInput" value={livro.categoriesInput} onChange={handleChange} placeholder="Ficcao, Fantasia, Aventura" />
          </label>

          <label>
            Idioma
            <input className={styles.input} type="text" name="language" value={livro.language} onChange={handleChange} placeholder="Portugues" />
          </label>

          <label>
            Paginas
            <input className={styles.input} type="number" min="0" name="pages" value={livro.pages} onChange={handleChange} />
          </label>

          <label>
            Editora
            <input className={styles.input} type="text" name="publisher" value={livro.publisher} onChange={handleChange} />
          </label>

          <label>
            Publicacao
            <input className={styles.input} type="text" name="publishedDate" value={livro.publishedDate} onChange={handleChange} placeholder="7 fevereiro 2014" />
          </label>

          <label>
            Status
            <select className={styles.select} name="status" value={livro.status} onChange={handleChange}>
              <option value="QUERO LER">Quero Ler</option>
              <option value="LENDO">Lendo</option>
              <option value="LIDO">Lido</option>
              <option value="RECOMENDADO">Recomendação</option>
            </select>
          </label>

          <div style={{ display: 'flex', gap: '12px' }}>
            <button type="submit" className={styles.submitBtn} disabled={carregando}>
              {carregando ? 'SALVANDO...' : 'SALVAR ALTERAÇÕES'}
            </button>
            <button 
              type="button" 
              className={styles.cancelBtn} 
              onClick={() => navigate(`/livro/${id}`)}
            >
              CANCELAR
            </button>
          </div>
        </form>
      </main>
      <div className={styles.footerWrap}>
        <Footer />
      </div>
    </div>
  );
}
