import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import api from '../services/api';
import styles from './LoginPage.module.css';

function validarSenha(s) {
  if (!s || s.length < 6) return 'A senha deve ter no mínimo 6 caracteres';
  if (/^\d+$/.test(s)) return 'A senha não pode conter apenas números';
  if (/^[a-zA-Z]+$/.test(s)) return 'A senha não pode conter apenas letras';
  return '';
}

export default function RedefinirSenhaPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!email.trim()) { setError('Informe o email da sua conta.'); return; }
    if (!currentPassword) { setError('Informe sua senha atual.'); return; }

    const erroSenha = validarSenha(newPassword);
    if (erroSenha) { setError(erroSenha); return; }

    if (newPassword !== confirmPassword) { setError('As senhas não coincidem.'); return; }

    setLoading(true);
    try {
      await api.post('/auth/redefinir-senha', {
        email: email.trim(),
        senhaAtual: currentPassword,
        novaSenha: newPassword,
      });
      setSuccess('Senha alterada com sucesso! Redirecionando para o login...');
      setTimeout(() => navigate('/login'), 2500);
    } catch (err) {
      if (err.response?.status === 404) {
        setError('Email não encontrado. Verifique e tente novamente.');
      } else if (err.response?.status === 401) {
        setError('Senha atual incorreta.');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Erro ao alterar senha. Tente novamente.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard title="Alterar senha">
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <FormInput
          id="reset-email"
          label="Email da conta"
          type="email"
          placeholder="Digite seu email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormInput
          id="reset-current-password"
          label="Senha atual"
          type="password"
          placeholder="Digite sua senha atual"
          value={currentPassword}
          onChange={(e) => { setCurrentPassword(e.target.value); setError(''); }}
        />
        <FormInput
          id="reset-new-password"
          label="Nova senha"
          type="password"
          placeholder="Mínimo 6 caracteres, letras e números"
          value={newPassword}
          onChange={(e) => { setNewPassword(e.target.value); setError(''); }}
        />
        <FormInput
          id="reset-confirm"
          label="Confirmar nova senha"
          type="password"
          placeholder="Digite novamente a nova senha"
          value={confirmPassword}
          onChange={(e) => { setConfirmPassword(e.target.value); setError(''); }}
        />
        {error && <p className={styles.errorMsg}>{error}</p>}
        {success && <p className={styles.successMsg}>{success}</p>}
        <button type="submit" className={styles.submitBtn} disabled={loading}>
          {loading ? 'Alterando...' : 'Alterar senha'}
        </button>
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Lembrou a senha? Entre por <Link to="/login">aqui</Link>!
      </p>
    </AuthCard>
  );
}
