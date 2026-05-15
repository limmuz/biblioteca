import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { getUser } from '../services/auth';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import Modal from '../components/shared/Modal';
import api from '../services/api';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import styles from './NovoLivroPage.module.css';

const BG_COVERS = [
  '/books/book-cabeca-santo.png',
  '/books/book-persepolis.png',
  '/books/book-maus.png',
  '/books/book-diario-zlata.png',
  '/books/book-diferenca.png',
  '/books/book-batalhas.png',
  '/books/book-noiva.png',
  '/books/book-sherlock.png',
];
const bgTiles = Array.from({ length: 300 }, (_, i) => BG_COVERS[i % BG_COVERS.length]);

export default function EditarLivroPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const currentUserEmail = getUser().email;

  const [carregando, setCarregando] = useState(false);
  const [loading, setLoading] = useState(!location.state?.bookData);
  const [modal, setModal] = useState(null);
  const [camposProtegidos, setCamposProtegidos] = useState(new Set());
  const [camposProtegidosPorCriador, setCamposProtegidosPorCriador] = useState(new Set());
  const [criadorEmail, setCriadorEmail] = useState(null);
  const [criadorNickname, setCriadorNickname] = useState(null);

  const toggleLock = (campo) => {
    setCamposProtegidos(prev => {
      const novo = new Set(prev);
      if (novo.has(campo)) novo.delete(campo); else novo.add(campo);
      return novo;
    });
  };

  const isCriadorBloqueado = (campo) =>
    camposProtegidosPorCriador.has(campo) && !camposProtegidos.has(campo);

  const isOriginalCreator = !criadorEmail || criadorEmail === currentUserEmail;
  const naoEditavel = (campo) => isCriadorBloqueado(campo);
  const somenteLeitura = (campo) => camposProtegidos.has(campo);

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
      setCamposProtegidos(new Set(book.camposProtegidos || []));
      setCamposProtegidosPorCriador(new Set(book.camposProtegidosPorCriador || []));
      setCriadorEmail(book.criadorEmail || null);
      setCriadorNickname(book.criadorNickname || null);
      setLoading(false);
      api.get(`/livros/${id}`)
        .then(res => {
          setCamposProtegidosPorCriador(new Set(res.data.camposProtegidosPorCriador || []));
          setCriadorEmail(res.data.criadorEmail || null);
          setCriadorNickname(res.data.criadorNickname || null);
        })
        .catch(() => {});
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
          setCamposProtegidos(new Set(book.camposProtegidos || []));
          setCamposProtegidosPorCriador(new Set(book.camposProtegidosPorCriador || []));
          setCriadorEmail(book.criadorEmail || null);
          setCriadorNickname(book.criadorNickname || null);
          setLoading(false);
        })
        .catch(() => {
          setModal({
            title: 'Erro',
            message: 'Não foi possível carregar o livro.',
            onConfirm: () => navigate('/home'),
            confirmLabel: 'Voltar',
            singleButton: true,
          });
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
          : ['Nao informado'],
        camposProtegidos: [...camposProtegidos],
      };

      await api.put(`/livros/${id}`, payload);
      localStorage.removeItem('lybre_livros_cache');
      setModal({
        title: 'Livro atualizado!',
        message: 'As alterações foram salvas com sucesso.',
        onConfirm: () => navigate(`/livro/${id}`),
        confirmLabel: 'Ver livro',
        singleButton: true,
      });
    } catch (error) {
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

      setModal({
        title: 'Ops!',
        message: mensagemErro,
        onConfirm: () => setModal(null),
        confirmLabel: 'Entendi',
        singleButton: true,
      });
    } finally {
      setCarregando(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <LoadingSpinner message="Carregando livro para edição..." />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.bgCovers} aria-hidden="true">
        {bgTiles.map((src, i) => (
          <img key={i} src={src} alt="" className={styles.bgCover} />
        ))}
      </div>
      <AppHeader />
      <main className={styles.main}>
        <div className={styles.formCard}>
          <h2 className={styles.title}>Editar Livro</h2>

          <form onSubmit={handleSubmit} className={styles.form}>
            <label>
              Título
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('title') || isCriadorBloqueado('title')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="title"
                  value={livro.title}
                  onChange={handleChange}
                  readOnly={somenteLeitura('title')}
                  disabled={naoEditavel('title')}
                  required
                />
                {(isOriginalCreator || isCriadorBloqueado('title')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('title') || isCriadorBloqueado('title')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('title') ? undefined : () => toggleLock('title')}
                    disabled={naoEditavel('title')}
                    title={isCriadorBloqueado('title') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('title') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Autor
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('author') || isCriadorBloqueado('author')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="author"
                  value={livro.author}
                  onChange={handleChange}
                  readOnly={somenteLeitura('author')}
                  disabled={naoEditavel('author')}
                  required
                />
                {(isOriginalCreator || isCriadorBloqueado('author')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('author') || isCriadorBloqueado('author')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('author') ? undefined : () => toggleLock('author')}
                    disabled={naoEditavel('author')}
                    title={isCriadorBloqueado('author') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('author') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              URL da Capa
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('cover') || isCriadorBloqueado('cover')) ? styles.inputLocked : ''}`}
                  type="url"
                  name="cover"
                  value={livro.cover}
                  onChange={handleChange}
                  readOnly={somenteLeitura('cover')}
                  disabled={naoEditavel('cover')}
                  required
                />
                {(isOriginalCreator || isCriadorBloqueado('cover')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('cover') || isCriadorBloqueado('cover')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('cover') ? undefined : () => toggleLock('cover')}
                    disabled={naoEditavel('cover')}
                    title={isCriadorBloqueado('cover') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('cover') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Sinopse
              <div className={styles.inputWrapper}>
                <textarea
                  className={`${styles.textarea} ${styles.inputWithLock} ${(camposProtegidos.has('excerpt') || isCriadorBloqueado('excerpt')) ? styles.inputLocked : ''}`}
                  name="excerpt"
                  value={livro.excerpt}
                  onChange={handleChange}
                  readOnly={somenteLeitura('excerpt')}
                  disabled={naoEditavel('excerpt')}
                  required
                />
                {(isOriginalCreator || isCriadorBloqueado('excerpt')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('excerpt') || isCriadorBloqueado('excerpt')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('excerpt') ? undefined : () => toggleLock('excerpt')}
                    disabled={naoEditavel('excerpt')}
                    title={isCriadorBloqueado('excerpt') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('excerpt') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Categorias (separe por virgula)
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('categories') || isCriadorBloqueado('categories')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="categoriesInput"
                  value={livro.categoriesInput}
                  onChange={handleChange}
                  readOnly={somenteLeitura('categories')}
                  disabled={naoEditavel('categories')}
                  placeholder="Ficcao, Fantasia, Aventura"
                />
                {(isOriginalCreator || isCriadorBloqueado('categories')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('categories') || isCriadorBloqueado('categories')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('categories') ? undefined : () => toggleLock('categories')}
                    disabled={naoEditavel('categories')}
                    title={isCriadorBloqueado('categories') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('categories') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Idioma
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('language') || isCriadorBloqueado('language')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="language"
                  value={livro.language}
                  onChange={handleChange}
                  readOnly={somenteLeitura('language')}
                  disabled={naoEditavel('language')}
                  placeholder="Portugues"
                />
                {(isOriginalCreator || isCriadorBloqueado('language')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('language') || isCriadorBloqueado('language')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('language') ? undefined : () => toggleLock('language')}
                    disabled={naoEditavel('language')}
                    title={isCriadorBloqueado('language') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('language') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Paginas
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('pages') || isCriadorBloqueado('pages')) ? styles.inputLocked : ''}`}
                  type="number"
                  min="0"
                  name="pages"
                  value={livro.pages}
                  onChange={handleChange}
                  readOnly={somenteLeitura('pages')}
                  disabled={naoEditavel('pages')}
                />
                {(isOriginalCreator || isCriadorBloqueado('pages')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('pages') || isCriadorBloqueado('pages')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('pages') ? undefined : () => toggleLock('pages')}
                    disabled={naoEditavel('pages')}
                    title={isCriadorBloqueado('pages') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('pages') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Editora
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('publisher') || isCriadorBloqueado('publisher')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="publisher"
                  value={livro.publisher}
                  onChange={handleChange}
                  readOnly={somenteLeitura('publisher')}
                  disabled={naoEditavel('publisher')}
                />
                {(isOriginalCreator || isCriadorBloqueado('publisher')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('publisher') || isCriadorBloqueado('publisher')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('publisher') ? undefined : () => toggleLock('publisher')}
                    disabled={naoEditavel('publisher')}
                    title={isCriadorBloqueado('publisher') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('publisher') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
            </label>

            <label>
              Publicacao
              <div className={styles.inputWrapper}>
                <input
                  className={`${styles.input} ${styles.inputWithLock} ${(camposProtegidos.has('publishedDate') || isCriadorBloqueado('publishedDate')) ? styles.inputLocked : ''}`}
                  type="text"
                  name="publishedDate"
                  value={livro.publishedDate}
                  onChange={handleChange}
                  readOnly={somenteLeitura('publishedDate')}
                  disabled={naoEditavel('publishedDate')}
                  placeholder="7 fevereiro 2014"
                />
                {(isOriginalCreator || isCriadorBloqueado('publishedDate')) && (
                  <button
                    type="button"
                    className={`${styles.lockIconInInput} ${(camposProtegidos.has('publishedDate') || isCriadorBloqueado('publishedDate')) ? styles.lockIconVisible : styles.lockIconHidden}`}
                    onClick={isCriadorBloqueado('publishedDate') ? undefined : () => toggleLock('publishedDate')}
                    disabled={naoEditavel('publishedDate')}
                    title={isCriadorBloqueado('publishedDate') ? 'Bloqueado pelo criador deste livro.' : camposProtegidos.has('publishedDate') ? 'Protegido. Clique para liberar.' : 'Clique para proteger.'}
                  >
                    🔒
                  </button>
                )}
              </div>
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
        </div>
      </main>
      <div className={styles.footerWrap}>
        <Footer />
      </div>

      {modal && (
        <Modal
          title={modal.title}
          message={modal.message}
          onConfirm={modal.onConfirm}
          confirmLabel={modal.confirmLabel}
          singleButton={modal.singleButton}
        />
      )}
    </div>
  );
}
