import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import CadastroPage from './pages/CadastroPage';
import RedefinirSenhaPage from './pages/RedefinirSenhaPage';
import HomePage from './pages/HomePage';
import ListagemPage from './pages/ListagemPage';
import DetalhesLivroPage from './pages/DetalhesLivroPage';
import ReadingPage from './components/ReadingPage/ReadingPage';
import NovoLivroPage from './pages/NovoLivroPage';
import EditarLivroPage from './pages/EditarLivroPage';
import PerfilPage from './pages/PerfilPage';
import ProtectedRoute from './components/auth/ProtectedRoute';
import { isAuthenticated } from './services/auth';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={<Navigate to={isAuthenticated() ? '/home' : '/login'} replace />}
        />
        
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
        <Route path="/redefinir-senha" element={<RedefinirSenhaPage />} />
        
        <Route
          path="/home"
          element={
            <ProtectedRoute>
              <HomePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/listagem"
          element={
            <ProtectedRoute>
              <ListagemPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/novo-livro"
          element={
            <ProtectedRoute>
              <NovoLivroPage />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/livro/:id"
          element={
            <ProtectedRoute>
              <DetalhesLivroPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/editar-livro/:id"
          element={
            <ProtectedRoute>
              <EditarLivroPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/perfil"
          element={
            <ProtectedRoute>
              <PerfilPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/leitura/:id"
          element={
            <ProtectedRoute>
              <ReadingPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}