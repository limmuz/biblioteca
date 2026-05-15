import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import api from '../services/api';
import { saveSession } from '../services/auth';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('Preencha o email e a senha para entrar.');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/auth/login', {
        email,
        senha: password,
      });
      saveSession(response.data);
      navigate('/home');
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        setError('Email ou senha incorretos. Verifique seus dados e tente novamente.');
      } else if (err.message === 'Network Error') {
        setError('Erro de conexão. Verifique sua internet e tente novamente.');
      } else {
        setError('Erro ao fazer login. Tente novamente em instantes.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard title="Sejam bem vindos a Lybre!">
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <FormInput
          id="login-email"
          label="Email"
          type="email"
          placeholder="Digite seu email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormInput
          id="login-password"
          label="Senha"
          type="password"
          placeholder="••••••••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <p className={styles.forgotLink}>
          Esqueceu senha? Clique <Link to="/redefinir-senha">aqui</Link>!
        </p>
        <button type="submit" className={styles.submitBtn}>
          {loading ? 'Entrando...' : 'Entrar'}
        </button>
        {error && (
          <div className={styles.errorBox}>
            <span className={styles.errorIcon}>⚠️</span>
            <p className={styles.errorText}>{error}</p>
          </div>
        )}
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Ainda não possui conta? Crie uma <Link to="/cadastro">aqui</Link>!
      </p>
    </AuthCard>
  );
}
