import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import CadastroPage from './pages/CadastroPage';
import RedefinirSenhaPage from './pages/RedefinirSenhaPage';
import HomePage from './pages/HomePage';
import ListagemPage from './pages/ListagemPage';
import DetalhesLivroPage from './pages/DetalhesLivroPage';
import ReadingPage from './components/ReadingPage/ReadingPage';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/redefinir-senha" element={<RedefinirSenhaPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/listagem" element={<ListagemPage />} />
        <Route path="/livro/:id" element={<DetalhesLivroPage />} />
        <Route path="/leitura/:id" element={<ReadingPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
