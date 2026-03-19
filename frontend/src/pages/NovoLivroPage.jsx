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
    status: 'QUERO LER'
  });

  const handleChange = (e) => {
    setLivro({ ...livro, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setCarregando(true);
    try {
      await api.post('/livros', livro);
      navigate('/'); 
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