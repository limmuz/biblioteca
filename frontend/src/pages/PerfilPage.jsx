import React, { useState, useEffect, useRef, useMemo } from 'react';
import PropTypes from 'prop-types';
import { useNavigate } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import { getUser, clearSession } from '../services/auth';
import AdBanner from '../components/shared/AdBanner';
import styles from './PerfilPage.module.css';

function Toast({ message, type, onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3000);
    return () => clearTimeout(t);
  }, [onClose]);
  const toastClass = [styles.toast, styles[`toast_${type}`]].filter(Boolean).join(' ');
  return (
    <div className={toastClass}>
      <span>{message}</span>
      <button className={styles.toastClose} onClick={onClose} type="button">✕</button>
    </div>
  );
}

Toast.propTypes = {
  message: PropTypes.string.isRequired,
  type: PropTypes.string,
  onClose: PropTypes.func.isRequired,
};

Toast.defaultProps = {
  type: 'success',
};

function ConfirmModal({ title, message, onConfirm, onCancel, confirmLabel, cancelLabel, danger }) {
  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modal}>
        <h2 className={styles.modalTitle}>{title}</h2>
        <p className={styles.modalText}>{message}</p>
        <div className={styles.modalActions}>
          <button className={danger ? styles.btnDanger : styles.btnPrimary} onClick={onConfirm} type="button">{confirmLabel}</button>
          <button className={styles.btnEdit} onClick={onCancel} type="button">{cancelLabel}</button>
        </div>
      </div>
    </div>
  );
}

ConfirmModal.propTypes = {
  title: PropTypes.string.isRequired,
  message: PropTypes.string.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  confirmLabel: PropTypes.string,
  cancelLabel: PropTypes.string,
  danger: PropTypes.bool,
};

ConfirmModal.defaultProps = {
  confirmLabel: 'Confirmar',
  cancelLabel: 'Cancelar',
  danger: false,
};

const BG_FALLBACK = [
  '/books/book-cabeca-santo.png',
  '/books/book-persepolis.png',
  '/books/book-maus.png',
  '/books/book-diario-zlata.png',
  '/books/book-diferenca.png',
  '/books/book-batalhas.png',
  '/books/book-noiva.png',
  '/books/book-sherlock.png',
];

export default function PerfilPage() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [usuario, setUsuario] = useState(null);
  const [livrosFavoritos, setLivrosFavoritos] = useState([]);
  const [todosPorStatus, setTodosPorStatus] = useState({ LIDO: [], LENDO: [], 'QUERO LER': [], RECOMENDADO: [] });
  const [estatisticas, setEstatisticas] = useState({ total: 0, lidos: 0, lendo: 0, queroLer: 0 });
  const [statExpandida, setStatExpandida] = useState(null);
  const [loading, setLoading] = useState(true);
  const [avatarUrl, setAvatarUrl] = useState(null);

  const [toast, setToast] = useState(null);
  const showToast = (message, type = 'success') => setToast({ message, type });
  const hideToast = () => setToast(null);

  const [confirmModal, setConfirmModal] = useState(null);

  const [editandoPerfil, setEditandoPerfil] = useState(false);
  const [perfilForm, setPerfilForm] = useState({ nome: '', email: '', nickname: '', bio: '', metaLeitura: '', perfilPublico: true, telefones: [''], redesSociais: [''] });
  const [salvandoPerfil, setSalvandoPerfil] = useState(false);

  const [outrosLeitores, setOutrosLeitores] = useState([]);
  const leitoresCarouselRef = useRef(null);

  const [perfisAdicionados, setPerfisAdicionados] = useState(() => {
    try { return JSON.parse(localStorage.getItem('lybre_perfis_add') || '[]'); }
    catch { return []; }
  });

  const [buscaNickname, setBuscaNickname] = useState('');
  const [leitoresEncontrados, setLeitoresEncontrados] = useState([]);
  const [buscandoLeitor, setBuscandoLeitor] = useState(false);
  const [buscaErro, setBuscaErro] = useState('');

  const [medias, setMedias] = useState({});

  const bgInputRef = useRef(null);
  const [bgImage, setBgImage] = useState(() => localStorage.getItem('lybre_bg') || null);
  const [bgOpacity, setBgOpacity] = useState(() => Number.parseFloat(localStorage.getItem('lybre_bg_opacity') || '0.45'));
  const [perfilFonte, setPerfilFonte] = useState(() => localStorage.getItem('lybre_fonte') || 'serif');

  const [enderecos, setEnderecos] = useState([]);
  const [editandoEnderecoIdx, setEditandoEnderecoIdx] = useState(null);
  const [enderecoForm, setEnderecoForm] = useState({ cep: '', logradouro: '', bairro: '', cidade: '', uf: '' });
  const [salvandoEndereco, setSalvandoEndereco] = useState(false);
  const [cepLoading, setCepLoading] = useState(false);

  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    const carregarDados = async () => {
      try {
        const resUsuario = await api.get('/usuarios/me');
        const u = resUsuario.data;
        setUsuario(u);
        if (u.avatarBase64) {
          setAvatarUrl(u.avatarBase64);
          localStorage.setItem('lybre_avatar', u.avatarBase64);
        }
        if (u.bgBase64 && !localStorage.getItem('lybre_bg')) {
          setBgImage(u.bgBase64);
          localStorage.setItem('lybre_bg', u.bgBase64);
        }
        setPerfilForm({
          nome: u.nome || '',
          email: u.email || '',
          nickname: u.nickname || '',
          bio: u.bio || '',
          metaLeitura: u.metaLeitura || '',
          perfilPublico: u.perfilPublico ?? true,
          telefones: u.telefones?.length ? u.telefones : [''],
          redesSociais: u.redesSociais?.length ? u.redesSociais : [''],
        });
        if (u.enderecos?.length) {
          setEnderecos(u.enderecos);
        } else if (u.cep) {
          setEnderecos([{ cep: u.cep, logradouro: u.logradouro || '', bairro: u.bairro || '', cidade: u.cidade || '', uf: u.uf || '' }]);
        } else {
          setEnderecos([]);
        }

        const [resLivros, resMedias, resLeitores] = await Promise.all([
          api.get('/livros'),
          api.get('/avaliacoes/medias').catch(() => ({ data: [] })),
          api.get('/usuarios/leitores').catch(() => ({ data: [] })),
        ]);
        setOutrosLeitores(resLeitores.data);
        const mediaMap = {};
        resMedias.data.forEach(m => {
          const key = `${m.livroTitulo.toLowerCase()}||${m.livroAutor.toLowerCase()}`;
          mediaMap[key] = m;
        });
        setMedias(mediaMap);
        const todosLivros = resLivros.data;
        const queroLer = todosLivros.filter(l => l.status === 'QUERO LER');
        const favoritos = todosLivros.filter(l => {
          const key = `${(l.title || '').toLowerCase()}||${(l.author || '').toLowerCase()}`;
          return mediaMap[key]?.media >= 4;
        });
        setLivrosFavoritos(favoritos);
        setTodosPorStatus({
          'LIDO': todosLivros.filter(l => l.status === 'LIDO'),
          'LENDO': todosLivros.filter(l => l.status === 'LENDO'),
          'QUERO LER': queroLer,
          'RECOMENDADO': todosLivros.filter(l => l.status === 'RECOMENDADO'),
        });
        setEstatisticas({
          total: todosLivros.length,
          lidos: todosLivros.filter(l => l.status === 'LIDO').length,
          lendo: todosLivros.filter(l => l.status === 'LENDO').length,
          queroLer: queroLer.length,
        });
      } catch (err) {
        console.error('Erro ao carregar dados:', err);
      } finally {
        setLoading(false);
      }
    };
    carregarDados();
    const savedAvatar = localStorage.getItem('lybre_avatar');
    if (savedAvatar) setAvatarUrl(savedAvatar);
  }, []);

  const handleLogout = () => { clearSession(); navigate('/login'); };

  const handleAvatarChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
      showToast('A imagem deve ter no máximo 2MB.', 'error');
      return;
    }
    const reader = new FileReader();
    reader.onload = async (ev) => {
      const dataUrl = ev.target.result;
      setAvatarUrl(dataUrl);
      localStorage.setItem('lybre_avatar', dataUrl);
      try {
        await api.put('/usuarios/me', { avatarBase64: dataUrl });
        showToast('Foto atualizada com sucesso!');
      } catch (err) {
        console.error('Erro ao salvar avatar:', err);
        showToast('Foto salva localmente, mas falhou no servidor.', 'error');
      }
    };
    reader.readAsDataURL(file);
  };

  const handleBgChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) { showToast('A imagem deve ter no máximo 5MB.', 'error'); return; }
    const reader = new FileReader();
    reader.onload = async (ev) => {
      const dataUrl = ev.target.result;
      setBgImage(dataUrl);
      setUsuario(u => ({ ...u, bgBase64: dataUrl }));
      localStorage.setItem('lybre_bg', dataUrl);
      try {
        await api.put('/usuarios/me', { bgBase64: dataUrl });
        showToast('Plano de fundo atualizado!');
      } catch {
        showToast('Fundo salvo localmente, mas falhou no servidor.', 'error');
      }
    };
    reader.readAsDataURL(file);
  };

  const handleRemoverBg = async () => {
    setBgImage(null);
    setUsuario(u => ({ ...u, bgBase64: null }));
    localStorage.removeItem('lybre_bg');
    try {
      await api.put('/usuarios/me', { bgBase64: null });
    } catch { /* silencioso */ }
    showToast('Plano de fundo removido.');
  };

  const handleBgOpacity = (val) => {
    setBgOpacity(val);
    localStorage.setItem('lybre_bg_opacity', String(val));
  };

  const handleAdicionarLeitor = (leitor) => {
    if (perfisAdicionados.some(p => p.id === leitor.id)) return;
    const novo = [...perfisAdicionados, leitor];
    setPerfisAdicionados(novo);
    localStorage.setItem('lybre_perfis_add', JSON.stringify(novo));
    showToast(`${leitor.nome} adicionado aos seus perfis!`);
  };

  const handleRemoverLeitor = (id) => {
    const novo = perfisAdicionados.filter(p => p.id !== id);
    setPerfisAdicionados(novo);
    localStorage.setItem('lybre_perfis_add', JSON.stringify(novo));
  };

  const addTelefone = () => setPerfilForm(f => ({ ...f, telefones: [...f.telefones, ''] }));
  const removeTelefone = (i) => setPerfilForm(f => ({ ...f, telefones: f.telefones.filter((_, idx) => idx !== i) }));
  const updateTelefone = (i, v) => setPerfilForm(f => { const t = [...f.telefones]; t[i] = v; return { ...f, telefones: t }; });
  const addRedeSocial = () => setPerfilForm(f => ({ ...f, redesSociais: [...f.redesSociais, ''] }));
  const removeRedeSocial = (i) => setPerfilForm(f => ({ ...f, redesSociais: f.redesSociais.filter((_, idx) => idx !== i) }));
  const updateRedeSocial = (i, v) => setPerfilForm(f => { const r = [...f.redesSociais]; r[i] = v; return { ...f, redesSociais: r }; });

  const handleSalvarPerfil = async () => {
    setSalvandoPerfil(true);
    try {
      await api.put('/usuarios/me', {
        ...usuario,
        nome: perfilForm.nome,
        email: perfilForm.email,
        nickname: perfilForm.nickname,
        bio: perfilForm.bio,
        metaLeitura: Number(perfilForm.metaLeitura) || 0,
        perfilPublico: perfilForm.perfilPublico,
        telefones: perfilForm.telefones.filter(t => t.trim()),
        redesSociais: perfilForm.redesSociais.filter(r => r.trim()),
      });
      setUsuario(u => ({ ...u, ...perfilForm }));
      setEditandoPerfil(false);
      showToast('Perfil salvo com sucesso!');
    } catch (err) {
      console.error(err);
      showToast('Erro ao salvar perfil.', 'error');
    } finally {
      setSalvandoPerfil(false);
    }
  };

  const handleCepChange = async (e) => {
    const raw = e.target.value.replaceAll(/\D/g, '').slice(0, 8);
    const formatted = raw.length > 5 ? `${raw.slice(0, 5)}-${raw.slice(5)}` : raw;
    setEnderecoForm(f => ({ ...f, cep: formatted }));
    if (raw.length === 8) {
      setCepLoading(true);
      try {
        const res = await fetch(`https://viacep.com.br/ws/${raw}/json/`);
        const data = await res.json();
        if (!data.erro) {
          setEnderecoForm(f => ({
            ...f,
            logradouro: data.logradouro || f.logradouro,
            bairro: data.bairro || f.bairro,
            cidade: data.localidade || f.cidade,
            uf: data.uf || f.uf,
          }));
        }
      } catch (err) { console.warn('ViaCEP lookup failed:', err); }
      finally { setCepLoading(false); }
    }
  };

  const handleEditarEndereco = (idx) => {
    setEditandoEnderecoIdx(idx === 'novo' ? 'novo' : idx);
    setEnderecoForm(idx === 'novo'
      ? { cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '' }
      : { cep: '', logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '', ...enderecos[idx] }
    );
  };

  const handleSalvarEndereco = async () => {
    setSalvandoEndereco(true);
    try {
      const novosEnderecos = editandoEnderecoIdx === 'novo'
        ? [...enderecos, { ...enderecoForm, cep: enderecoForm.cep.replaceAll(/\D/g, '') }]
        : enderecos.map((e, i) => i === editandoEnderecoIdx ? { ...enderecoForm, cep: enderecoForm.cep.replaceAll(/\D/g, '') } : e);
      await api.put('/usuarios/me', { ...usuario, enderecos: novosEnderecos });
      setEnderecos(novosEnderecos);
      setEditandoEnderecoIdx(null);
      showToast('Endereço salvo com sucesso!');
    } catch (err) {
      console.error(err);
      showToast('Erro ao salvar endereço.', 'error');
    } finally {
      setSalvandoEndereco(false);
    }
  };

  const handleExcluirEndereco = (idx) => {
    setConfirmModal({
      title: 'Excluir Endereço',
      message: 'Tem certeza que deseja excluir este endereço?',
      danger: true,
      confirmLabel: 'Excluir',
      onConfirm: async () => {
        setConfirmModal(null);
        const novosEnderecos = enderecos.filter((_, i) => i !== idx);
        try {
          await api.put('/usuarios/me', { ...usuario, enderecos: novosEnderecos });
          setEnderecos(novosEnderecos);
          showToast('Endereço excluído.');
        } catch (err) {
          console.error(err);
          showToast('Erro ao excluir endereço.', 'error');
        }
      },
      onCancel: () => setConfirmModal(null),
    });
  };


  const statusToCampo = (status) => {
    if (status === 'LIDO') return 'lidos';
    if (status === 'LENDO') return 'lendo';
    if (status === 'QUERO LER') return 'queroLer';
    return null;
  };

  const removerLivroDoEstado = (livroId, livroStatus) => {
    const semLivro = (lista) => lista.filter(l => l.id !== livroId);
    setLivrosFavoritos(semLivro);
    setTodosPorStatus(prev => Object.fromEntries(Object.entries(prev).map(([s, lista]) => [s, semLivro(lista)])));
    const campo = statusToCampo(livroStatus);
    setEstatisticas(e => ({
      ...e,
      total: e.total - 1,
      ...(campo ? { [campo]: Math.max(0, e[campo] - 1) } : {}),
    }));
  };

  const handleExcluirLivro = (livro) => {
    const confirmarExclusao = async () => {
      setConfirmModal(null);
      try {
        await api.delete(`/livros/${livro.id}`);
        removerLivroDoEstado(livro.id, livro.status);
        showToast('Livro excluído com sucesso.');
      } catch (err) { console.error(err); showToast('Erro ao excluir livro.', 'error'); }
    };
    setConfirmModal({
      title: 'Excluir Livro',
      message: `Excluir "${livro.title}" permanentemente? Esta ação não pode ser desfeita.`,
      danger: true,
      confirmLabel: 'Excluir',
      onConfirm: confirmarExclusao,
      onCancel: () => setConfirmModal(null),
    });
  };

  const handleBuscarLeitor = async (e) => {
    e.preventDefault();
    const termo = buscaNickname.trim();
    if (!termo) return;
    setBuscandoLeitor(true);
    setBuscaErro('');
    setLeitoresEncontrados([]);
    try {
      const res = await api.get(`/usuarios/pesquisar?q=${encodeURIComponent(termo)}`);
      if (res.data.length === 0) {
        setBuscaErro('Nenhum leitor encontrado.');
      } else {
        setLeitoresEncontrados(res.data);
      }
    } catch {
      setBuscaErro('Erro ao buscar leitor.');
    } finally {
      setBuscandoLeitor(false);
    }
  };

  const getRatingPerfil = (livro) => {
    const key = `${livro.title.toLowerCase()}||${livro.author.toLowerCase()}`;
    return medias[key] || { media: 0, total: 0 };
  };

  const handleExcluirConta = async () => {
    setShowDeleteModal(false);
    try {
      await api.delete('/usuarios/me');
      clearSession();
      navigate('/cadastro');
    } catch (err) {
      console.error(err);
      showToast('Erro ao excluir conta.', 'error');
    }
  };

  const bgTiles = useMemo(() => {
    const todos = [
      ...todosPorStatus['LIDO'],
      ...todosPorStatus['LENDO'],
      ...todosPorStatus['QUERO LER'],
      ...todosPorStatus['RECOMENDADO'],
    ];
    const covers = todos.map(l => l.cover).filter(Boolean);
    const source = covers.length >= 4 ? covers : BG_FALLBACK;
    return Array.from({ length: 300 }, (_, i) => ({ id: `bg-${i}`, src: source[i % source.length] }));
  }, [todosPorStatus]);

  const generoMaisLido = useMemo(() => {
    const todos = [
      ...todosPorStatus['LIDO'],
      ...todosPorStatus['LENDO'],
      ...todosPorStatus['QUERO LER'],
      ...todosPorStatus['RECOMENDADO'],
    ];
    const freq = {};
    todos.forEach(l => {
      if (Array.isArray(l.categories)) {
        l.categories.forEach(c => { if (c && c !== 'Nao informado') freq[c] = (freq[c] || 0) + 1; });
      }
    });
    const entries = Object.entries(freq);
    if (!entries.length) return null;
    return entries.reduce((a, b) => (b[1] > a[1] ? b : a), entries[0])[0];
  }, [todosPorStatus]);

  if (loading) {
    return (
      <div className={styles.page}>
        <AppHeader />
        <main className={styles.main}>
          <h2 style={{ color: 'white', textAlign: 'center', marginTop: '100px' }}>Carregando perfil...</h2>
        </main>
      </div>
    );
  }

  const userLocal = getUser();
  const iniciais = userLocal.nome
    ? userLocal.nome.split(' ').slice(0, 2).map(n => n[0].toUpperCase()).join('')
    : '?';

  return (
    <div className={styles.page} style={{ fontFamily: perfilFonte }}>
      {bgImage ? (
        <div
          className={styles.profileBg}
          aria-hidden="true"
          style={{ backgroundImage: `url(${bgImage})`, opacity: bgOpacity }}
        />
      ) : (
        <div className={styles.bgCovers} aria-hidden="true">
          {bgTiles.map(tile => (
            <img key={tile.id} src={tile.src} alt="" className={styles.bgCover}
              onError={(e) => { e.target.style.visibility = 'hidden'; }} />
          ))}
        </div>
      )}
      <AppHeader />
      <main className={styles.main}>

        {/* Cabeçalho do Perfil */}
        <div className={styles.profileHeader}>
          <div className={styles.avatarWrapper}>
            {avatarUrl
              ? <img src={avatarUrl} alt="Avatar" className={styles.avatarImg} />
              : <div className={styles.avatarLarge}>{iniciais}</div>
            }
            <button className={styles.avatarEditBtn} onClick={() => fileInputRef.current?.click()} title="Alterar foto" type="button">✏️</button>
            <input ref={fileInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleAvatarChange} />
          </div>
          <div className={styles.profileInfo}>
            <h1 className={styles.userName}>{usuario?.nome || userLocal.nome}</h1>
            {usuario?.nickname && <p className={styles.userNickname}>@{usuario.nickname}</p>}
            <p className={styles.userEmail}>{usuario?.email || userLocal.email}</p>
            {usuario?.bio && <p className={styles.userBio}>"{usuario.bio}"</p>}
            {generoMaisLido && (
              <span className={styles.genreBadge}>📖 {generoMaisLido}</span>
            )}
            {usuario?.telefones?.filter(t => t).map(tel => (
              <p key={tel} className={styles.userPhone}>📞 {tel}</p>
            ))}
            {usuario?.redesSociais?.filter(r => r).map(rs => (
              <p key={rs} className={styles.userSocial}>🔗 {rs}</p>
            ))}
          </div>
          <div className={styles.headerActions}>
            <button
              className={usuario?.perfilPublico ? styles.btnPublico : styles.btnPrivado}
              type="button"
              title={usuario?.perfilPublico ? 'Perfil público — clique para tornar privado' : 'Perfil privado — clique para tornar público'}
              onClick={async () => {
                const novo = !usuario?.perfilPublico;
                setUsuario(u => ({ ...u, perfilPublico: novo }));
                setPerfilForm(f => ({ ...f, perfilPublico: novo }));
                try { await api.put('/usuarios/me', { perfilPublico: novo }); }
                catch { setUsuario(u => ({ ...u, perfilPublico: !novo })); }
              }}
            >
              {usuario?.perfilPublico ? '🌐 Público' : '🔒 Privado'}
            </button>
            <button className={styles.btnEdit} onClick={() => setEditandoPerfil(e => !e)} type="button">
              {editandoPerfil ? 'Cancelar' : 'Editar Perfil'}
            </button>
          </div>
        </div>

        {/* Formulário de edição de perfil */}
        {editandoPerfil && (
          <div className={styles.addressCard}>
            <h2 className={styles.sectionTitle}>Editar Informações</h2>
            <div className={styles.addressForm}>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-nome">Nome completo</label>
                <input id="pf-nome" className={styles.formInput} type="text" value={perfilForm.nome}
                  onChange={e => setPerfilForm(f => ({ ...f, nome: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-nick">Nickname</label>
                <input id="pf-nick" className={styles.formInput} type="text" value={perfilForm.nickname} placeholder="@seunome"
                  onChange={e => setPerfilForm(f => ({ ...f, nickname: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-email">E-mail</label>
                <input id="pf-email" className={styles.formInput} type="email" value={perfilForm.email}
                  onChange={e => setPerfilForm(f => ({ ...f, email: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-bio">Bio (frase sobre você)</label>
                <input id="pf-bio" className={styles.formInput} type="text" value={perfilForm.bio} maxLength={120}
                  placeholder="Ex: Apaixonada por ficção científica..."
                  onChange={e => setPerfilForm(f => ({ ...f, bio: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-meta">Meta de leitura (livros em 2026)</label>
                <input id="pf-meta" className={styles.formInput} type="number" min="1" max="365" value={perfilForm.metaLeitura}
                  placeholder="Ex: 24"
                  onChange={e => setPerfilForm(f => ({ ...f, metaLeitura: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <span className={styles.formLabel}>Telefones</span>
                {perfilForm.telefones.map((tel, i) => (
                  <div key={i} className={styles.multiInputRow}>{/* NOSONAR - editable list without stable IDs */}
                    <input className={styles.formInput} type="tel" value={tel} placeholder="(11) 99999-9999"
                      aria-label={`Telefone ${i + 1}`}
                      onChange={e => updateTelefone(i, e.target.value)} />
                    {perfilForm.telefones.length > 1 && (
                      <button className={styles.btnRemoveItem} onClick={() => removeTelefone(i)} type="button">✕</button>
                    )}
                  </div>
                ))}
                <button className={styles.btnAddItem} onClick={addTelefone} type="button">+ Adicionar telefone</button>
              </div>
              <div className={styles.formRow}>
                <span className={styles.formLabel}>Redes Sociais</span>
                {perfilForm.redesSociais.map((rs, i) => (
                  <div key={i} className={styles.multiInputRow}>{/* NOSONAR - editable list without stable IDs */}
                    <input className={styles.formInput} type="text" value={rs} placeholder="@instagram ou link"
                      aria-label={`Rede social ${i + 1}`}
                      onChange={e => updateRedeSocial(i, e.target.value)} />
                    {perfilForm.redesSociais.length > 1 && (
                      <button className={styles.btnRemoveItem} onClick={() => removeRedeSocial(i)} type="button">✕</button>
                    )}
                  </div>
                ))}
                <button className={styles.btnAddItem} onClick={addRedeSocial} type="button">+ Adicionar rede social</button>
              </div>
              <div className={styles.formRow}>
                <span className={styles.formLabel}>Perfil público</span>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                  <input type="checkbox" checked={perfilForm.perfilPublico}
                    onChange={e => setPerfilForm(f => ({ ...f, perfilPublico: e.target.checked }))} />
                  <span style={{ fontSize: 13, color: 'var(--color-sage)' }}>
                    {perfilForm.perfilPublico ? 'Visível para outros leitores' : 'Somente você vê'}
                  </span>
                </label>
              </div>
              <div className={styles.formRow}>
                <span className={styles.formLabel}>Plano de fundo</span>
                <input ref={bgInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleBgChange} />
                <div className={styles.bgUploadRow}>
                  <button className={styles.btnEdit} type="button" onClick={() => bgInputRef.current?.click()}>
                    {bgImage ? 'Trocar imagem' : 'Escolher imagem'}
                  </button>
                  {bgImage && (
                    <>
                      <img src={bgImage} alt="Prévia do fundo" className={styles.bgPreview} />
                      <button className={styles.bgRemoveBtn} type="button" onClick={handleRemoverBg}>Remover</button>
                    </>
                  )}
                </div>
                {bgImage && (
                  <div className={styles.opacityRow}>
                    <span className={styles.opacityLabel}>Opacidade</span>
                    <input type="range" min="0.05" max="0.9" step="0.01" value={bgOpacity}
                      className={styles.opacitySlider}
                      onChange={e => handleBgOpacity(Number.parseFloat(e.target.value))} />
                    <span className={styles.opacityLabel}>{Math.round(bgOpacity * 100)}%</span>
                  </div>
                )}
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="pf-fonte">Fonte do perfil</label>
                <select
                  id="pf-fonte"
                  className={styles.formInput}
                  value={perfilFonte}
                  onChange={e => {
                    setPerfilFonte(e.target.value);
                    localStorage.setItem('lybre_fonte', e.target.value);
                  }}
                >
                  <option value="serif">Serifada (padrão)</option>
                  <option value="sans-serif">Sem serifa</option>
                  <option value="'Georgia', serif">Georgia</option>
                  <option value="'Palatino', serif">Palatino</option>
                  <option value="'Courier New', monospace">Mono</option>
                </select>
              </div>
              <button className={styles.btnPrimary} onClick={handleSalvarPerfil} disabled={salvandoPerfil} type="button">
                {salvandoPerfil ? 'Salvando...' : 'Salvar Perfil'}
              </button>
            </div>
          </div>
        )}

        {/* Estatísticas */}
        <div className={styles.statsCard}>
          <h2 className={styles.sectionTitle}>Estatísticas de Leitura</h2>
          <div className={styles.statsPills}>
            {[
              { key: 'LIDO',      label: 'Lidos',      icon: '✅', count: estatisticas.lidos,   color: styles.pillLido },
              { key: 'LENDO',     label: 'Lendo agora', icon: '📖', count: estatisticas.lendo,   color: styles.pillLendo },
              { key: 'QUERO LER', label: 'Quero Ler',  icon: '📚', count: estatisticas.queroLer, color: styles.pillQuero },
              { key: 'total',     label: 'Total',       icon: '📕', count: estatisticas.total,   color: styles.pillTotal },
            ].map(({ key, label, icon, count, color }) => (
              <button
                key={key}
                className={`${styles.statPill} ${color} ${statExpandida === key ? styles.statPillActive : ''}`}
                onClick={() => setStatExpandida(statExpandida === key ? null : key)}
                type="button"
              >
                <span className={styles.statPillIcon}>{icon}</span>
                <span className={styles.statPillCount}>{count}</span>
                <span className={styles.statPillLabel}>{label}</span>
                <span className={styles.statPillArrow}>{statExpandida === key ? '▲' : '▼'}</span>
              </button>
            ))}
          </div>

          {usuario?.metaLeitura > 0 && (
            <div className={styles.metaWrap}>
              <div className={styles.metaHeader}>
                <span className={styles.metaLabel}>🎯 Meta de leitura 2026</span>
                <span className={styles.metaCount}>{estatisticas.lidos} / {usuario.metaLeitura} livros</span>
              </div>
              <div className={styles.metaBar}>
                <div
                  className={styles.metaFill}
                  style={{ width: `${Math.min(100, Math.round((estatisticas.lidos / usuario.metaLeitura) * 100))}%` }}
                />
              </div>
              <span className={styles.metaPct}>
                {Math.min(100, Math.round((estatisticas.lidos / usuario.metaLeitura) * 100))}% concluído
              </span>
            </div>
          )}

          {statExpandida && statExpandida !== 'total' && (
            <div className={styles.statBooks}>
              {(todosPorStatus[statExpandida] || []).length === 0 ? (
                <p className={styles.emptyMessage}>Nenhum livro nesta categoria ainda.</p>
              ) : (
                <div className={styles.statBookGrid}>
                  {(todosPorStatus[statExpandida] || []).map(livro => (
                    <div key={livro.id} className={styles.statBookCard}>
                      <button type="button" className={styles.statBookBtn}
                        onClick={() => navigate(`/livro/${livro.id}`, { state: { bookData: livro } })}>
                        <img src={livro.cover} alt={livro.title} className={styles.statBookCover}
                          onError={(e) => { e.target.src = 'https://via.placeholder.com/60x90?text=Capa'; }} />
                        <div className={styles.statBookInfo}>
                          <p className={styles.statBookTitle}>{livro.title}</p>
                          <p className={styles.statBookAuthor}>{livro.author}</p>
                        </div>
                      </button>
                      <button
                        className={styles.statBookDelete}
                        onClick={(e) => { e.stopPropagation(); handleExcluirLivro(livro); }}
                        title="Excluir da biblioteca"
                        type="button"
                      >×</button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {statExpandida === 'total' && (
            <div className={styles.statBooks}>
              <div className={styles.statBookGrid}>
                {[...todosPorStatus['LIDO'], ...todosPorStatus['LENDO'], ...todosPorStatus['QUERO LER'], ...todosPorStatus['RECOMENDADO']].map(livro => (
                  <div key={livro.id} className={styles.statBookCard}>
                    <button type="button" className={styles.statBookBtn}
                      onClick={() => navigate(`/livro/${livro.id}`, { state: { bookData: livro } })}>
                      <img src={livro.cover} alt={livro.title} className={styles.statBookCover}
                        onError={(e) => { e.target.src = 'https://via.placeholder.com/60x90?text=Capa'; }} />
                      <div className={styles.statBookInfo}>
                        <p className={styles.statBookTitle}>{livro.title}</p>
                        <p className={styles.statBookAuthor}>{livro.author}</p>
                      </div>
                    </button>
                    <button
                      className={styles.statBookDelete}
                      onClick={(e) => { e.stopPropagation(); handleExcluirLivro(livro); }}
                      title="Excluir da biblioteca"
                      type="button"
                    >×</button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Endereços */}
        <div className={styles.addressCard}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Endereços</h2>
            <button className={styles.btnEdit} onClick={() => handleEditarEndereco('novo')} type="button">+ Adicionar</button>
          </div>

          {enderecos.map((end, idx) => (
            <div key={`${end.cep || ''}-${idx}`} className={styles.addressItem}>
              {editandoEnderecoIdx === idx ? (
                <div className={styles.addressForm}>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel} htmlFor="end-cep">CEP</label>
                    <input id="end-cep" className={styles.formInput} type="text" value={enderecoForm.cep} onChange={handleCepChange} placeholder="00000-000" maxLength={9} />
                    {cepLoading && <span className={styles.cepHint}>Buscando...</span>}
                  </div>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel} htmlFor="end-log">Logradouro</label>
                    <input id="end-log" className={styles.formInput} type="text" value={enderecoForm.logradouro} onChange={e => setEnderecoForm(f => ({ ...f, logradouro: e.target.value }))} placeholder="Rua, avenida..." />
                  </div>
                  <div className={styles.formRowDouble}>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel} htmlFor="end-num">Número</label>
                      <input id="end-num" className={styles.formInput} type="text" value={enderecoForm.numero || ''} onChange={e => setEnderecoForm(f => ({ ...f, numero: e.target.value }))} placeholder="Ex: 123" />
                    </div>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel} htmlFor="end-comp">Complemento</label>
                      <input id="end-comp" className={styles.formInput} type="text" value={enderecoForm.complemento || ''} onChange={e => setEnderecoForm(f => ({ ...f, complemento: e.target.value }))} placeholder="Apto, Bloco..." />
                    </div>
                  </div>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel} htmlFor="end-bairro">Bairro</label>
                    <input id="end-bairro" className={styles.formInput} type="text" value={enderecoForm.bairro} onChange={e => setEnderecoForm(f => ({ ...f, bairro: e.target.value }))} />
                  </div>
                  <div className={styles.formRowDouble}>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel} htmlFor="end-cidade">Cidade</label>
                      <input id="end-cidade" className={styles.formInput} type="text" value={enderecoForm.cidade} onChange={e => setEnderecoForm(f => ({ ...f, cidade: e.target.value }))} />
                    </div>
                    <div className={styles.formRow} style={{ maxWidth: '80px' }}>
                      <label className={styles.formLabel} htmlFor="end-uf">UF</label>
                      <input id="end-uf" className={styles.formInput} type="text" value={enderecoForm.uf} onChange={e => setEnderecoForm(f => ({ ...f, uf: e.target.value.toUpperCase().slice(0, 2) }))} maxLength={2} />
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button className={styles.btnPrimary} onClick={handleSalvarEndereco} disabled={salvandoEndereco} type="button">
                      {salvandoEndereco ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button className={styles.btnEdit} onClick={() => setEditandoEnderecoIdx(null)} type="button">Cancelar</button>
                  </div>
                </div>
              ) : (
                <div className={styles.addressInfo}>
                  <p><strong>CEP:</strong> {end.cep}</p>
                  <p><strong>Logradouro:</strong> {end.logradouro || 'Não informado'}{end.numero ? `, nº ${end.numero}` : ''}{end.complemento ? ` — ${end.complemento}` : ''}</p>
                  <p><strong>Bairro:</strong> {end.bairro || 'Não informado'}</p>
                  <p><strong>Cidade:</strong> {end.cidade || 'Não informado'} — <strong>UF:</strong> {end.uf || 'Não informado'}</p>
                  <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                    <button className={styles.btnEdit} onClick={() => handleEditarEndereco(idx)} type="button">Editar</button>
                    <button className={styles.btnSmallDelete} onClick={() => handleExcluirEndereco(idx)} type="button">Excluir</button>
                  </div>
                </div>
              )}
            </div>
          ))}

          {editandoEnderecoIdx === 'novo' && (
            <div className={styles.addressForm} style={{ marginTop: '16px' }}>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="novo-cep">CEP</label>
                <input id="novo-cep" className={styles.formInput} type="text" value={enderecoForm.cep} onChange={handleCepChange} placeholder="00000-000" maxLength={9} />
                {cepLoading && <span className={styles.cepHint}>Buscando...</span>}
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="novo-log">Logradouro</label>
                <input id="novo-log" className={styles.formInput} type="text" value={enderecoForm.logradouro} onChange={e => setEnderecoForm(f => ({ ...f, logradouro: e.target.value }))} placeholder="Rua, avenida..." />
              </div>
              <div className={styles.formRowDouble}>
                <div className={styles.formRow}>
                  <label className={styles.formLabel} htmlFor="novo-num">Número</label>
                  <input id="novo-num" className={styles.formInput} type="text" value={enderecoForm.numero || ''} onChange={e => setEnderecoForm(f => ({ ...f, numero: e.target.value }))} placeholder="Ex: 123" />
                </div>
                <div className={styles.formRow}>
                  <label className={styles.formLabel} htmlFor="novo-comp">Complemento</label>
                  <input id="novo-comp" className={styles.formInput} type="text" value={enderecoForm.complemento || ''} onChange={e => setEnderecoForm(f => ({ ...f, complemento: e.target.value }))} placeholder="Apto, Bloco..." />
                </div>
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel} htmlFor="novo-bairro">Bairro</label>
                <input id="novo-bairro" className={styles.formInput} type="text" value={enderecoForm.bairro} onChange={e => setEnderecoForm(f => ({ ...f, bairro: e.target.value }))} />
              </div>
              <div className={styles.formRowDouble}>
                <div className={styles.formRow}>
                  <label className={styles.formLabel} htmlFor="novo-cidade">Cidade</label>
                  <input id="novo-cidade" className={styles.formInput} type="text" value={enderecoForm.cidade} onChange={e => setEnderecoForm(f => ({ ...f, cidade: e.target.value }))} />
                </div>
                <div className={styles.formRow} style={{ maxWidth: '80px' }}>
                  <label className={styles.formLabel} htmlFor="novo-uf">UF</label>
                  <input id="novo-uf" className={styles.formInput} type="text" value={enderecoForm.uf} onChange={e => setEnderecoForm(f => ({ ...f, uf: e.target.value.toUpperCase().slice(0, 2) }))} maxLength={2} />
                </div>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button className={styles.btnPrimary} onClick={handleSalvarEndereco} disabled={salvandoEndereco} type="button">
                  {salvandoEndereco ? 'Salvando...' : 'Salvar'}
                </button>
                <button className={styles.btnEdit} onClick={() => setEditandoEnderecoIdx(null)} type="button">Cancelar</button>
              </div>
            </div>
          )}

          {enderecos.length === 0 && editandoEnderecoIdx === null && (
            <p className={styles.emptyMessage}>Nenhum endereço cadastrado.</p>
          )}
        </div>

        {/* Livros Favoritos */}
        <div className={styles.favoritesSection}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Meus Favoritos ({livrosFavoritos.length})</h2>
            <span className={styles.sectionHint}>Livros avaliados com ★ 4 ou 5</span>
          </div>
          {livrosFavoritos.length > 0 ? (
            <div className={styles.bookGrid}>
              {livrosFavoritos.map(livro => {
                const r = getRatingPerfil(livro);
                return (
                  <div key={livro.id} className={styles.bookCardWrapper}>
                    <button type="button" className={styles.bookCoverBtn}
                      onClick={() => navigate(`/livro/${livro.id}`, { state: { bookData: livro } })}>
                      <img src={livro.cover} alt={livro.title} className={styles.bookCover} />
                    </button>
                    <div className={styles.bookInfo}>
                      <p className={styles.bookTitle}>{livro.title}</p>
                      <p className={styles.bookAuthor}>{livro.author}</p>
                      {r.media > 0 && (
                        <div className={styles.bookRatingRow}>
                          {[1,2,3,4,5].map(s => (
                            <span key={s} className={s <= Math.round(r.media) ? styles.starFilled : styles.starEmpty}>★</span>
                          ))}
                          <span className={styles.bookRatingCount}>{r.media.toFixed(1)} ({r.total})</span>
                        </div>
                      )}
                      <div className={styles.bookActions}>
                        <button className={styles.btnSmallEdit} onClick={() => navigate(`/livro/${livro.id}`, { state: { bookData: livro } })} type="button">Ver</button>
                        <button className={styles.btnSmallDelete} onClick={() => handleExcluirLivro(livro)} type="button">Excluir</button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className={styles.emptyMessage}>Nenhum favorito ainda. Avalie um livro com 4 ou 5 estrelas e ele aparecerá aqui!</p>
          )}
        </div>

        {/* Outros Leitores */}
        {outrosLeitores.length > 0 && (
          <div className={styles.communitySection}>
            <h2 className={styles.sectionTitle}>Conheça outros leitores</h2>
            <p className={styles.communitySubtitle}>Perfis públicos da comunidade</p>
            <div className={styles.leitoresCarouselWrap} ref={leitoresCarouselRef}>
              {outrosLeitores.map(leitor => {
                const jaAdicionado = perfisAdicionados.some(p => p.id === leitor.id);
                const inicLetras = leitor.nome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || '?';
                return (
                  <div key={leitor.id} className={styles.leitorCard}>
                    {/* Fundo do card */}
                    <div
                      className={styles.leitorCardBg}
                      style={leitor.bgBase64 ? { backgroundImage: `url(${leitor.bgBase64})` } : {}}
                      aria-hidden="true"
                    />
                    <div className={styles.leitorCardOverlay} aria-hidden="true" />

                    {/* Avatar */}
                    <div className={styles.leitorCardAvatar}>
                      {leitor.avatarBase64
                        ? <img src={leitor.avatarBase64} alt={leitor.nome} className={styles.leitorCardAvatarImg} />
                        : inicLetras
                      }
                    </div>

                    {/* Info */}
                    <div className={styles.leitorCardInfo}>
                      <p className={styles.leitorCardNome}>{leitor.nome}</p>
                      {leitor.nickname && <p className={styles.leitorCardNick}>@{leitor.nickname}</p>}
                      {leitor.bio && <p className={styles.leitorCardBio}>{leitor.bio}</p>}
                    </div>

                    {/* Ações */}
                    <div className={styles.leitorCardActions}>
                      {leitor.nickname && (
                        <button
                          type="button"
                          className={styles.btnVerPerfilCard}
                          onClick={() => navigate(`/leitor/${leitor.nickname}`)}
                        >
                          Ver perfil
                        </button>
                      )}
                      <button
                        type="button"
                        className={jaAdicionado ? styles.btnAdicionadoCard : styles.btnAdicionarCard}
                        onClick={() => jaAdicionado ? handleRemoverLeitor(leitor.id) : handleAdicionarLeitor(leitor)}
                      >
                        {jaAdicionado ? '✓ Adicionado' : '+ Adicionar'}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Perfis Adicionados */}
        {perfisAdicionados.length > 0 && (
          <div className={styles.communitySection}>
            <h2 className={styles.sectionTitle}>Perfis adicionados</h2>
            <p className={styles.communitySubtitle}>Leitores que você acompanha</p>
            <div className={styles.perfisAdicionadosGrid}>
              {perfisAdicionados.map(leitor => {
                const inicLetras = leitor.nome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || '?';
                return (
                  <div key={leitor.id} className={styles.perfilAdicionadoCard}>
                    <div
                      className={styles.perfilAdicionadoBg}
                      style={leitor.bgBase64 ? { backgroundImage: `url(${leitor.bgBase64})` } : {}}
                      aria-hidden="true"
                    />
                    <div className={styles.perfilAdicionadoOverlay} aria-hidden="true" />
                    <div className={styles.perfilAdicionadoAvatar}>
                      {leitor.avatarBase64
                        ? <img src={leitor.avatarBase64} alt={leitor.nome} className={styles.leitorCardAvatarImg} />
                        : inicLetras
                      }
                    </div>
                    <div className={styles.perfilAdicionadoInfo}>
                      <p className={styles.perfilAdicionadoNome}>{leitor.nome}</p>
                      {leitor.nickname && <p className={styles.perfilAdicionadoNick}>@{leitor.nickname}</p>}
                    </div>
                    <div className={styles.perfilAdicionadoAcoes}>
                      {leitor.nickname && (
                        <button
                          type="button"
                          className={styles.btnVerPerfilCard}
                          onClick={() => navigate(`/leitor/${leitor.nickname}`)}
                        >
                          Ver perfil
                        </button>
                      )}
                      <button
                        type="button"
                        className={styles.btnRemoverPerfilAdd}
                        onClick={() => handleRemoverLeitor(leitor.id)}
                      >
                        Remover
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Comunidades */}
        <div className={styles.communitySection}>
          <h2 className={styles.sectionTitle}>Comunidades de Leitura</h2>
          <p className={styles.communitySubtitle}>Grupos do Facebook para leitores</p>
          <div className={styles.communityGrid}>
            {[
              { nome: 'Leitores Brasileiros', membros: '1,2M membros', emoji: '📚', cor: '#1877f2' },
              { nome: 'Ficção Científica Brasil', membros: '450K membros', emoji: '🔮', cor: '#6c5ce7' },
              { nome: 'Clube do Livro Brasil', membros: '890K membros', emoji: '🏛️', cor: '#00b894' },
              { nome: 'Romance & Amor em Livros', membros: '320K membros', emoji: '🌹', cor: '#e84393' },
            ].map((g) => (
              <div key={g.nome} className={styles.communityCard}>
                <div className={styles.communityIcon} style={{ background: g.cor }}>{g.emoji}</div>
                <div className={styles.communityInfo}>
                  <p className={styles.communityName}>{g.nome}</p>
                  <p className={styles.communityMembers}>{g.membros}</p>
                </div>
                <span className={styles.communityBadge}>Facebook</span>
              </div>
            ))}
          </div>

          <div className={styles.communitySearch}>
            <h3 className={styles.communitySearchTitle}>Encontrar leitor</h3>
            <p className={styles.searchHint}>Busque por nome ou @nickname</p>
            <form onSubmit={handleBuscarLeitor} className={styles.searchForm}>
              <input
                className={styles.searchInput}
                type="text"
                value={buscaNickname}
                onChange={e => { setBuscaNickname(e.target.value); setBuscaErro(''); setLeitoresEncontrados([]); }}
                placeholder="Ex: Sofia ou @sofia_reads"
              />
              <button className={styles.btnSearch} type="submit" disabled={buscandoLeitor}>
                {buscandoLeitor ? 'Buscando...' : 'Buscar'}
              </button>
            </form>
            {buscaErro && <p className={styles.searchError}>{buscaErro}</p>}
            {leitoresEncontrados.length > 0 && (
              <div className={styles.leitorResultados}>
                {leitoresEncontrados.map(leitor => (
                  <div key={leitor.id} className={styles.leitorCard}>
                    <div className={styles.leitorAvatar}>
                      {leitor.avatarBase64
                        ? <img src={leitor.avatarBase64} alt="avatar" className={styles.leitorAvatarImg} />
                        : <div className={styles.leitorAvatarInitials}>
                            {leitor.nome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase()}
                          </div>
                      }
                    </div>
                    <div className={styles.leitorInfo}>
                      <p className={styles.leitorNome}>{leitor.nome}</p>
                      {leitor.nickname && <p className={styles.leitorNickname}>@{leitor.nickname}</p>}
                      {leitor.bio && <p className={styles.leitorBio}>"{leitor.bio}"</p>}
                    </div>
                    {leitor.nickname && (
                      <button
                        className={styles.btnVerPerfil}
                        type="button"
                        onClick={() => navigate(`/leitor/${leitor.nickname}`)}
                      >
                        Ver Perfil
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Ações */}
        <div className={styles.actionsCard}>
          <button className={styles.btnDanger} onClick={handleLogout}>Sair da Conta</button>
          <button className={styles.btnDangerOutline} onClick={() => setShowDeleteModal(true)} type="button">Excluir Conta</button>
        </div>

        <AdBanner variant="banner" />
        <div className={styles.footerWrap}><Footer /></div>
      </main>

      {/* Toast */}
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}

      {/* Confirm Modal */}
      {confirmModal && (
        <ConfirmModal
          title={confirmModal.title}
          message={confirmModal.message}
          onConfirm={confirmModal.onConfirm}
          onCancel={confirmModal.onCancel}
          confirmLabel={confirmModal.confirmLabel}
          danger={confirmModal.danger}
        />
      )}

      {/* Modal excluir conta */}
      {showDeleteModal && (
        <ConfirmModal
          title="Excluir Conta"
          message="Tem certeza? Esta ação é irreversível e todos os seus dados serão apagados."
          onConfirm={handleExcluirConta}
          onCancel={() => setShowDeleteModal(false)}
          confirmLabel="Sim, excluir"
          danger
        />
      )}
    </div>
  );
}
