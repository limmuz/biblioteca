import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import CadastroPage from './pages/CadastroPage';
import RedefinirSenhaPage from './pages/RedefinirSenhaPage';
import HomePage from './pages/HomePage';
import ListagemPage from './pages/ListagemPage';
import DetalhesLivroPage from './pages/DetalhesLivroPage';
import ReadingPage from './components/ReadingPage/ReadingPage';
import NovoLivroPage from './pages/NovoLivroPage';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Inicia no Login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/redefinir-senha" element={<RedefinirSenhaPage />} />
        
        {/* Rotas Internas */}
        <Route path="/home" element={<HomePage />} />
        <Route path="/listagem" element={<ListagemPage />} />
        <Route path="/novo-livro" element={<NovoLivroPage />} />
        
        {/* Visualização e Leitura */}
        <Route path="/livro/:id" element={<DetalhesLivroPage />} />
        <Route path="/leitura/:id" element={<ReadingPage />} />

        {/* Se digitar rota errada, volta pro login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}