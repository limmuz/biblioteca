import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api from '../../services/api';
import styles from './ReadingPage.module.css';

export default function ReadingPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [book, setBook] = useState(location.state?.bookData || null);

  useEffect(() => {
    // Limpa o ID de barras e prefixos
    const cleanId = id.replace('works', '').replace(/\//g, '');
    
    // Se não temos os dados do livro, buscamos no banco
    if (!book) {
      api.get(`/livros/${cleanId}`)
        .then(res => setBook(res.data))
        .catch(() => {
          console.error("Livro não encontrado para leitura.");
          navigate('/home');
        });
    }
  }, [id, book, navigate]);

  if (!book) return <div className={styles.canvas} style={{color: 'white', padding: '100px'}}>Iniciando leitor...</div>;

  return (
    <div className={styles.readingContainer}>
      <header className={styles.readingHeader} style={{display: 'flex', alignItems: 'center', padding: '20px', gap: '20px', borderBottom: '1px solid var(--color-sage)'}}>
        <button onClick={() => navigate(-1)} style={{cursor: 'pointer', background: 'none', border: '1px solid var(--color-sage)', color: 'white', padding: '5px 15px', borderRadius: '20px'}}>Sair</button>
        <h1 style={{color: 'var(--color-forest)', fontSize: '22px'}}>{book.title}</h1>
      </header>

      <main className={styles.canvas}>
        <div className={styles.textWrapper} style={{maxWidth: '800px', margin: '40px auto', padding: '0 20px', color: 'var(--color-forest)', lineHeight: '1.8', fontSize: '19px', fontFamily: 'serif'}}>
          <p>{book.excerpt || book.summary || "O conteúdo integral deste livro não está disponível para leitura offline no momento."}</p>
        </div>
      </main>
    </div>
  );
}