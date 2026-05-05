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
    setLoading(true);

    try {
      const response = await api.post('/auth/login', {
        email,
        senha: password,
      });
      saveSession(response.data);
      navigate('/home');
    } catch (err) {
      const mensagemErro = err.response?.data?.message || 
        'Erro ao fazer login. Verifique se o email e senha estão corretos e tente novamente.';
      setError(mensagemErro);
      console.error('Erro no login:', err);
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
        {error ? <p style={{ color: '#d14343', marginTop: '10px' }}>{error}</p> : null}
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Ainda não possui conta? Crie uma <Link to="/cadastro">aqui</Link>!
      </p>
    </AuthCard>
  );
}
