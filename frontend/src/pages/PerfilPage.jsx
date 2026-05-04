import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import AppHeader from '../components/shared/AppHeader';
import Footer from '../components/Footer/Footer';
import api from '../services/api';
import { getUser, clearSession } from '../services/auth';
import styles from './PerfilPage.module.css';

// ── Toast component ──────────────────────────────────────────────
function Toast({ message, type = 'success', onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3000);
    return () => clearTimeout(t);
  }, [onClose]);
  return (
    <div className={`${styles.toast} ${styles[`toast_${type}`]}`}>
      <span>{message}</span>
      <button className={styles.toastClose} onClick={onClose} type="button">✕</button>
    </div>
  );
}

// ── Confirm Modal ────────────────────────────────────────────────
function ConfirmModal({ title, message, onConfirm, onCancel, confirmLabel = 'Confirmar', cancelLabel = 'Cancelar', danger = false }) {
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

export default function PerfilPage() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [usuario, setUsuario] = useState(null);
  const [livrosFavoritos, setLivrosFavoritos] = useState([]);
  const [estatisticas, setEstatisticas] = useState({ total: 0, lidos: 0, lendo: 0, queroLer: 0 });
  const [loading, setLoading] = useState(true);
  const [avatarUrl, setAvatarUrl] = useState(null);

  // Toast
  const [toast, setToast] = useState(null);
  const showToast = (message, type = 'success') => setToast({ message, type });
  const hideToast = () => setToast(null);

  // Confirm modal
  const [confirmModal, setConfirmModal] = useState(null);

  // Perfil edit
  const [editandoPerfil, setEditandoPerfil] = useState(false);
  const [perfilForm, setPerfilForm] = useState({ nome: '', email: '', nickname: '', telefones: [''], redesSociais: [''] });
  const [salvandoPerfil, setSalvandoPerfil] = useState(false);

  // Endereços
  const [enderecos, setEnderecos] = useState([]);
  const [editandoEnderecoIdx, setEditandoEnderecoIdx] = useState(null);
  const [enderecoForm, setEnderecoForm] = useState({ cep: '', logradouro: '', bairro: '', cidade: '', uf: '' });
  const [salvandoEndereco, setSalvandoEndereco] = useState(false);
  const [cepLoading, setCepLoading] = useState(false);

  // Excluir conta
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  useEffect(() => {
    const carregarDados = async () => {
      try {
        const resUsuario = await api.get('/usuarios/me');
        const u = resUsuario.data;
        setUsuario(u);
        setPerfilForm({
          nome: u.nome || '',
          email: u.email || '',
          nickname: u.nickname || '',
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

        const resLivros = await api.get('/livros');
        const todosLivros = resLivros.data;
        const favoritos = todosLivros.filter(l => l.status === 'QUERO LER');
        setLivrosFavoritos(favoritos);
        setEstatisticas({
          total: todosLivros.length,
          lidos: todosLivros.filter(l => l.status === 'LIDO').length,
          lendo: todosLivros.filter(l => l.status === 'LENDO').length,
          queroLer: favoritos.length,
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
    const reader = new FileReader();
    reader.onload = (ev) => {
      const dataUrl = ev.target.result;
      setAvatarUrl(dataUrl);
      localStorage.setItem('lybre_avatar', dataUrl);
      showToast('Foto atualizada com sucesso!');
    };
    reader.readAsDataURL(file);
  };

  // ── Perfil ──────────────────────────────────────────────────────
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

  // ── Endereços ────────────────────────────────────────────────────
  const handleCepChange = async (e) => {
    const raw = e.target.value.replace(/\D/g, '').slice(0, 8);
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
      } catch (_) { /* ignore */ }
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
        ? [...enderecos, { ...enderecoForm, cep: enderecoForm.cep.replace(/\D/g, '') }]
        : enderecos.map((e, i) => i === editandoEnderecoIdx ? { ...enderecoForm, cep: enderecoForm.cep.replace(/\D/g, '') } : e);
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

  // ── Favoritos ────────────────────────────────────────────────────
  const handleRemoverFavorito = (livro) => {
    setConfirmModal({
      title: 'Remover dos Favoritos',
      message: `Remover "${livro.title}" da lista de favoritos?`,
      confirmLabel: 'Remover',
      onConfirm: async () => {
        setConfirmModal(null);
        try {
          await api.put(`/livros/${livro.id}`, { ...livro, status: 'LIDO' });
          setLivrosFavoritos(prev => prev.filter(l => l.id !== livro.id));
          setEstatisticas(e => ({ ...e, queroLer: e.queroLer - 1, lidos: e.lidos + 1 }));
          showToast('Livro removido dos favoritos.');
        } catch (err) { console.error(err); showToast('Erro ao remover favorito.', 'error'); }
      },
      onCancel: () => setConfirmModal(null),
    });
  };

  const handleExcluirLivro = (livro) => {
    setConfirmModal({
      title: 'Excluir Livro',
      message: `Excluir "${livro.title}" permanentemente? Esta ação não pode ser desfeita.`,
      danger: true,
      confirmLabel: 'Excluir',
      onConfirm: async () => {
        setConfirmModal(null);
        try {
          await api.delete(`/livros/${livro.id}`);
          setLivrosFavoritos(prev => prev.filter(l => l.id !== livro.id));
          setEstatisticas(e => ({ ...e, queroLer: e.queroLer - 1, total: e.total - 1 }));
          showToast('Livro excluído com sucesso.');
        } catch (err) { console.error(err); showToast('Erro ao excluir livro.', 'error'); }
      },
      onCancel: () => setConfirmModal(null),
    });
  };

  // ── Excluir conta ────────────────────────────────────────────────
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
    <div className={styles.page}>
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
            {usuario?.telefones?.filter(t => t).map((tel, i) => (
              <p key={i} className={styles.userPhone}>📞 {tel}</p>
            ))}
            {usuario?.redesSociais?.filter(r => r).map((rs, i) => (
              <p key={i} className={styles.userSocial}>🔗 {rs}</p>
            ))}
          </div>
          <button className={styles.btnEdit} onClick={() => setEditandoPerfil(e => !e)} type="button" style={{ marginLeft: 'auto', alignSelf: 'flex-start' }}>
            {editandoPerfil ? 'Cancelar' : 'Editar Perfil'}
          </button>
        </div>

        {/* Formulário de edição de perfil */}
        {editandoPerfil && (
          <div className={styles.addressCard}>
            <h2 className={styles.sectionTitle}>Editar Informações</h2>
            <div className={styles.addressForm}>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Nome completo</label>
                <input className={styles.formInput} type="text" value={perfilForm.nome}
                  onChange={e => setPerfilForm(f => ({ ...f, nome: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Nickname</label>
                <input className={styles.formInput} type="text" value={perfilForm.nickname} placeholder="@seunome"
                  onChange={e => setPerfilForm(f => ({ ...f, nickname: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>E-mail</label>
                <input className={styles.formInput} type="email" value={perfilForm.email}
                  onChange={e => setPerfilForm(f => ({ ...f, email: e.target.value }))} />
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Telefones</label>
                {perfilForm.telefones.map((tel, i) => (
                  <div key={i} className={styles.multiInputRow}>
                    <input className={styles.formInput} type="tel" value={tel} placeholder="(11) 99999-9999"
                      onChange={e => updateTelefone(i, e.target.value)} />
                    {perfilForm.telefones.length > 1 && (
                      <button className={styles.btnRemoveItem} onClick={() => removeTelefone(i)} type="button">✕</button>
                    )}
                  </div>
                ))}
                <button className={styles.btnAddItem} onClick={addTelefone} type="button">+ Adicionar telefone</button>
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Redes Sociais</label>
                {perfilForm.redesSociais.map((rs, i) => (
                  <div key={i} className={styles.multiInputRow}>
                    <input className={styles.formInput} type="text" value={rs} placeholder="@instagram ou link"
                      onChange={e => updateRedeSocial(i, e.target.value)} />
                    {perfilForm.redesSociais.length > 1 && (
                      <button className={styles.btnRemoveItem} onClick={() => removeRedeSocial(i)} type="button">✕</button>
                    )}
                  </div>
                ))}
                <button className={styles.btnAddItem} onClick={addRedeSocial} type="button">+ Adicionar rede social</button>
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
          <div className={styles.statsGrid}>
            <div className={styles.statItem}><span className={styles.statNumber}>{estatisticas.total}</span><span className={styles.statLabel}>Total de Livros</span></div>
            <div className={styles.statItem}><span className={styles.statNumber}>{estatisticas.lidos}</span><span className={styles.statLabel}>Livros Lidos</span></div>
            <div className={styles.statItem}><span className={styles.statNumber}>{estatisticas.lendo}</span><span className={styles.statLabel}>Lendo Agora</span></div>
            <div className={styles.statItem}><span className={styles.statNumber}>{estatisticas.queroLer}</span><span className={styles.statLabel}>Quero Ler</span></div>
          </div>
        </div>

        {/* Endereços */}
        <div className={styles.addressCard}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Endereços</h2>
            <button className={styles.btnEdit} onClick={() => handleEditarEndereco('novo')} type="button">+ Adicionar</button>
          </div>

          {enderecos.map((end, idx) => (
            <div key={idx} className={styles.addressItem}>
              {editandoEnderecoIdx === idx ? (
                <div className={styles.addressForm}>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel}>CEP</label>
                    <input className={styles.formInput} type="text" value={enderecoForm.cep} onChange={handleCepChange} placeholder="00000-000" maxLength={9} />
                    {cepLoading && <span className={styles.cepHint}>Buscando...</span>}
                  </div>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel}>Logradouro</label>
                    <input className={styles.formInput} type="text" value={enderecoForm.logradouro} onChange={e => setEnderecoForm(f => ({ ...f, logradouro: e.target.value }))} placeholder="Rua, avenida..." />
                  </div>
                  <div className={styles.formRowDouble}>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel}>Número</label>
                      <input className={styles.formInput} type="text" value={enderecoForm.numero || ''} onChange={e => setEnderecoForm(f => ({ ...f, numero: e.target.value }))} placeholder="Ex: 123" />
                    </div>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel}>Complemento</label>
                      <input className={styles.formInput} type="text" value={enderecoForm.complemento || ''} onChange={e => setEnderecoForm(f => ({ ...f, complemento: e.target.value }))} placeholder="Apto, Bloco..." />
                    </div>
                  </div>
                  <div className={styles.formRow}>
                    <label className={styles.formLabel}>Bairro</label>
                    <input className={styles.formInput} type="text" value={enderecoForm.bairro} onChange={e => setEnderecoForm(f => ({ ...f, bairro: e.target.value }))} />
                  </div>
                  <div className={styles.formRowDouble}>
                    <div className={styles.formRow}>
                      <label className={styles.formLabel}>Cidade</label>
                      <input className={styles.formInput} type="text" value={enderecoForm.cidade} onChange={e => setEnderecoForm(f => ({ ...f, cidade: e.target.value }))} />
                    </div>
                    <div className={styles.formRow} style={{ maxWidth: '80px' }}>
                      <label className={styles.formLabel}>UF</label>
                      <input className={styles.formInput} type="text" value={enderecoForm.uf} onChange={e => setEnderecoForm(f => ({ ...f, uf: e.target.value.toUpperCase().slice(0, 2) }))} maxLength={2} />
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
                <label className={styles.formLabel}>CEP</label>
                <input className={styles.formInput} type="text" value={enderecoForm.cep} onChange={handleCepChange} placeholder="00000-000" maxLength={9} />
                {cepLoading && <span className={styles.cepHint}>Buscando...</span>}
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Logradouro</label>
                <input className={styles.formInput} type="text" value={enderecoForm.logradouro} onChange={e => setEnderecoForm(f => ({ ...f, logradouro: e.target.value }))} placeholder="Rua, avenida..." />
              </div>
              <div className={styles.formRowDouble}>
                <div className={styles.formRow}>
                  <label className={styles.formLabel}>Número</label>
                  <input className={styles.formInput} type="text" value={enderecoForm.numero || ''} onChange={e => setEnderecoForm(f => ({ ...f, numero: e.target.value }))} placeholder="Ex: 123" />
                </div>
                <div className={styles.formRow}>
                  <label className={styles.formLabel}>Complemento</label>
                  <input className={styles.formInput} type="text" value={enderecoForm.complemento || ''} onChange={e => setEnderecoForm(f => ({ ...f, complemento: e.target.value }))} placeholder="Apto, Bloco..." />
                </div>
              </div>
              <div className={styles.formRow}>
                <label className={styles.formLabel}>Bairro</label>
                <input className={styles.formInput} type="text" value={enderecoForm.bairro} onChange={e => setEnderecoForm(f => ({ ...f, bairro: e.target.value }))} />
              </div>
              <div className={styles.formRowDouble}>
                <div className={styles.formRow}>
                  <label className={styles.formLabel}>Cidade</label>
                  <input className={styles.formInput} type="text" value={enderecoForm.cidade} onChange={e => setEnderecoForm(f => ({ ...f, cidade: e.target.value }))} />
                </div>
                <div className={styles.formRow} style={{ maxWidth: '80px' }}>
                  <label className={styles.formLabel}>UF</label>
                  <input className={styles.formInput} type="text" value={enderecoForm.uf} onChange={e => setEnderecoForm(f => ({ ...f, uf: e.target.value.toUpperCase().slice(0, 2) }))} maxLength={2} />
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
            <button className={styles.btnEdit} onClick={() => navigate('/novo-livro')} type="button">+ Adicionar</button>
          </div>
          {livrosFavoritos.length > 0 ? (
            <div className={styles.bookGrid}>
              {livrosFavoritos.map(livro => (
                <div key={livro.id} className={styles.bookCardWrapper}>
                  <img src={livro.cover} alt={livro.title} className={styles.bookCover}
                    onClick={() => navigate(`/livro/${livro.id}`, { state: { bookData: livro } })} />
                  <div className={styles.bookInfo}>
                    <p className={styles.bookTitle}>{livro.title}</p>
                    <p className={styles.bookAuthor}>{livro.author}</p>
                    <div className={styles.bookActions}>
                      <button className={styles.btnSmallEdit} onClick={() => navigate(`/editar-livro/${livro.id}`, { state: { bookData: livro } })} type="button">Editar</button>
                      <button className={styles.btnSmallRemove} onClick={() => handleRemoverFavorito(livro)} type="button">Remover</button>
                      <button className={styles.btnSmallDelete} onClick={() => handleExcluirLivro(livro)} type="button">Excluir</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className={styles.emptyMessage}>Você ainda não tem livros favoritos. Adicione livros à sua lista de leitura!</p>
          )}
        </div>

        {/* Ações */}
        <div className={styles.actionsCard}>
          <button className={styles.btnDanger} onClick={handleLogout}>Sair da Conta</button>
          <button className={styles.btnDangerOutline} onClick={() => setShowDeleteModal(true)} type="button">Excluir Conta</button>
        </div>

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
