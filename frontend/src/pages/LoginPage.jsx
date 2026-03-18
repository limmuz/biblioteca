import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import styles from './LoginPage.module.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    navigate('/home');
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
          Entrar
        </button>
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Ainda não possui conta? Crie uma <Link to="/cadastro">aqui</Link>!
      </p>
    </AuthCard>
  );
}
