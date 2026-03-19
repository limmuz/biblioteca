import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import BookCardMini from '../components/shared/BookCardMini';
import api from '../services/api';
import styles from './ListagemPage.module.css';

export default function ListagemPage() {
  const location = useLocation();
  const [livros, setLivros] = useState([]);
  const [loading, setLoading] = useState(false);
  
  const queryBusca = location.state?.query || "";

  useEffect(() => {
    const carregarResultados = async () => {
      if (!queryBusca) {
        
        setLoading(true);
        try {
          const res = await api.get('/livros');
          setLivros(res.data);
        } catch (err) {
          console.error(err);
        } finally {
          setLoading(false);
        }
        return;
      }

      setLoading(true);
      try {
       
        const response = await api.get(`/livros/externos/${queryBusca}`);
        setLivros(response.data);
      } catch (err) {
        console.error("Erro na busca:", err);
      } finally {
        setLoading(false);
      }
    };

    carregarResultados();
  }, [queryBusca]);

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main} style={{ paddingTop: '120px' }}>
        <h1 className={styles.title}>
          {queryBusca ? `Resultados para: "${queryBusca}"` : "Sua Biblioteca"}
        </h1>

        {loading ? (
          <p style={{ color: 'white', textAlign: 'center', marginTop: '2rem' }}>Buscando livros...</p>
        ) : (
          <div className={styles.bookGrid}>
            {livros.length > 0 ? (
              livros.map(book => <BookCardMini key={book.id} book={book} />)
            ) : (
              <p style={{ color: 'var(--color-sage)', textAlign: 'center', width: '100%' }}>
                Nenhum livro encontrado para esta busca.
              </p>
            )}
          </div>
        )}
      </main>
      <div className={styles.footerWrap}>
        <Footer />
      </div>
    </div>
  );
}