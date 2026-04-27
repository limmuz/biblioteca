import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import styles from './NovoLivroPage.module.css';

export default function NovoLivroPage() {
  const navigate = useNavigate();
  const [carregando, setCarregando] = useState(false);
  
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

      await api.post('/livros', payload);
      navigate('/home');
    } catch (error) {
      console.error("Erro ao salvar:", error);
      alert('Falha ao salvar o livro.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <h2 className={styles.title}>Novo Livro</h2>
        
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

          <button type="submit" className={styles.submitBtn} disabled={carregando}>
            {carregando ? 'SALVANDO...' : 'SALVAR LIVRO'}
          </button>
        </form>
      </main>
      <div className={styles.footerWrap}>
        <Footer />
      </div>
    </div>
  );
}