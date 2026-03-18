import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import styles from './LoginPage.module.css';

export default function CadastroPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    navigate('/login');
  }

  return (
    <AuthCard title="Sejam bem vindos a Lybre!">
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <FormInput
          id="cadastro-email"
          label="Email"
          type="email"
          placeholder="Digite seu email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormInput
          id="cadastro-password"
          label="Senha"
          type="password"
          placeholder="••••••••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <FormInput
          id="cadastro-confirm"
          label="Repita a senha"
          type="password"
          placeholder="••••••••••••••"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <button type="submit" className={styles.submitBtn}>
          Cadastrar
        </button>
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Já possui conta? Entre por <Link to="/login">aqui</Link>!
      </p>
    </AuthCard>
  );
}
