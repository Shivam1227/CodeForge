import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProblemListPage from './pages/ProblemListPage';
import ProblemDetailPage from './pages/ProblemDetailPage';
import AdminProblemListPage from './pages/AdminProblemListPage';
import AdminProblemFormPage from './pages/AdminProblemFormPage';
import ProfilePage from './pages/ProfilePage';

export default function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Navbar />
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/" element={<ProtectedRoute><ProblemListPage /></ProtectedRoute>} />
                    <Route path="/problems/:id" element={<ProtectedRoute><ProblemDetailPage /></ProtectedRoute>} />
                    <Route path="/admin" element={<AdminRoute><AdminProblemListPage /></AdminRoute>} />
                    <Route path="/admin/problems/new" element={<AdminRoute><AdminProblemFormPage /></AdminRoute>} />
                    <Route path="/admin/problems/:id" element={<AdminRoute><AdminProblemFormPage /></AdminRoute>} />
                    <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}