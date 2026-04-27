import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthCard from '../components/auth/AuthCard';
import FormInput from '../components/auth/FormInput';
import api from '../services/api';
import { saveSession } from '../services/auth';
import styles from './LoginPage.module.css';

export default function CadastroPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [cep, setCep] = useState('');
  const [logradouro, setLogradouro] = useState('');
  const [numero, setNumero] = useState('');
  const [complemento, setComplemento] = useState('');
  const [bairro, setBairro] = useState('');
  const [cidade, setCidade] = useState('');
  const [uf, setUf] = useState('');
  const [cepLoading, setCepLoading] = useState(false);
  const [cepError, setCepError] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const normalizeCep = (value) => value.replace(/\D/g, '').slice(0, 8);

  const formatCep = (value) => {
    const digits = normalizeCep(value);
    if (digits.length <= 5) return digits;
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
  };

  const handleCepChange = (event) => {
    const masked = formatCep(event.target.value);
    setCep(masked);
    setCepError('');
  };

  useEffect(() => {
    const fetchAddress = async () => {
      const cleanedCep = normalizeCep(cep);
      
      if (cleanedCep.length === 8) {
        setCepLoading(true);
        setCepError('');
        try {
          const response = await fetch(`https://viacep.com.br/ws/${cleanedCep}/json/`);
          const data = await response.json();

          if (data.erro) {
            setCepError('CEP não encontrado.');
          } else {
            setLogradouro(data.logradouro || '');
            setBairro(data.bairro || '');
            setCidade(data.localidade || '');
            setUf(data.uf || '');
            document.getElementById('cadastro-numero')?.focus();
          }
        } catch (err) {
          setCepError('Falha ao consultar CEP.');
        } finally {
          setCepLoading(false);
        }
      }
    };

    fetchAddress();
  }, [cep]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('As senhas nao coincidem.');
      return;
    }

    setLoading(true);
    try {
      const nomePadrao = email.split('@')[0] || 'Leitor';
      const response = await api.post('/auth/register', {
        nome: nomePadrao,
        email,
        senha: password,
        cep: normalizeCep(cep),
        logradouro,
        numero,
        complemento,
        bairro,
        cidade,
        uf,
      });
      saveSession(response.data);
      navigate('/home');
    } catch (err) {
      setError(err.response?.data?.message || 'Falha ao cadastrar usuario.');
    } finally {
      setLoading(false);
    }
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
          placeholder="Digite sua senha"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <FormInput
          id="cadastro-confirm"
          label="Repita a senha"
          type="password"
          placeholder="Digite novamente a senha"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />

        <div className={styles.group}>
          <label className={styles.fieldLabel} htmlFor="cadastro-cep">CEP</label>
          <input
            id="cadastro-cep"
            className={styles.fieldInput}
            type="text"
            inputMode="numeric"
            placeholder="00000-000"
            value={cep}
            onChange={handleCepChange}
            maxLength={9}
          />
          {cepLoading ? <p className={styles.infoMsg}>Buscando endereco...</p> : null}
          {cepError ? <p className={styles.errorMsg}>{cepError}</p> : null}
        </div>

        <div className={styles.group}>
          <label className={styles.fieldLabel} htmlFor="cadastro-logradouro">Logradouro</label>
          <input
            id="cadastro-logradouro"
            className={styles.fieldInput}
            type="text"
            placeholder="Rua, avenida..."
            value={logradouro}
            onChange={(e) => setLogradouro(e.target.value)}
          />
        </div>

        <div className={styles.row}>
          <div className={`${styles.group} ${styles.groupNumero}`}>
            <label className={styles.fieldLabel} htmlFor="cadastro-numero">Número</label>
            <input
              id="cadastro-numero"
              className={styles.fieldInput}
              type="text"
              placeholder="Ex: 123"
              value={numero}
              onChange={(e) => setNumero(e.target.value)}
            />
          </div>

          <div className={`${styles.group} ${styles.groupComplemento}`}>
            <label className={styles.fieldLabel} htmlFor="cadastro-complemento">Complemento</label>
            <input
              id="cadastro-complemento"
              className={styles.fieldInput}
              type="text"
              placeholder="Apto, Bloco..."
              value={complemento}
              onChange={(e) => setComplemento(e.target.value)}
            />
          </div>
        </div>

        <div className={styles.group}>
          <label className={styles.fieldLabel} htmlFor="cadastro-bairro">Bairro</label>
          <input
            id="cadastro-bairro"
            className={styles.fieldInput}
            type="text"
            placeholder="Bairro"
            value={bairro}
            onChange={(e) => setBairro(e.target.value)}
          />
        </div>
        
        <div className={styles.row}>
          <div className={`${styles.group} ${styles.groupCidade}`}>
            <label className={styles.fieldLabel} htmlFor="cadastro-cidade">Cidade</label>
            <input
              id="cadastro-cidade"
              className={styles.fieldInput}
              type="text"
              placeholder="Cidade"
              value={cidade}
              onChange={(e) => setCidade(e.target.value)}
            />
          </div>

          <div className={`${styles.group} ${styles.groupUf}`}>
            <label className={styles.fieldLabel} htmlFor="cadastro-uf">UF</label>
            <input
              id="cadastro-uf"
              className={styles.fieldInput}
              type="text"
              placeholder="UF"
              value={uf}
              onChange={(e) => setUf(e.target.value.toUpperCase().slice(0, 2))}
              maxLength={2}
            />
          </div>
        </div>

        <button type="submit" className={styles.submitBtn} disabled={loading}>
          {loading ? 'Cadastrando...' : 'Cadastrar'}
        </button>
        {error ? <p className={styles.errorMsg}>{error}</p> : null}
      </form>
      <hr className={styles.divider} />
      <p className={styles.bottomLink}>
        Já possui conta? Entre por <Link to="/login">aqui</Link>!
      </p>
    </AuthCard>
  );
}