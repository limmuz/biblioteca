import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import styles from './LoginPage.module.css';

export default function RedefinirSenhaPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    navigate('/login');
  }

  return (
    <AuthCard title="Esqueceu sua senha?">
      <form className={styles.form} onSubmit={handleSubmit} noValidate>
        <FormInput
          id="reset-email"
          label="Email"
          type="email"
          placeholder="Digite seu email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormInput
          id="reset-new-password"
          label="Nova senha"
          type="password"
          placeholder="••••••••••••••"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <FormInput
          id="reset-confirm"
          label="Confirmar nova senha"
          type="password"
          placeholder="••••••••••••••"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <button type="submit" className={styles.submitBtn}>
          Redefinir
        </button>
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Já possui conta? Entre por <Link to="/login">aqui</Link>!
      </p>
    </AuthCard>
  );
}
