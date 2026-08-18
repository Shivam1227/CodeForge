import { createContext, useContext, useState } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const username = localStorage.getItem('username');
        const role = localStorage.getItem('role');
        return username ? { username, role } : null;
    });

    const login = async (username, password) => {
        const res = await axiosClient.post('/auth/login', { username, password });
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('username', res.data.username);
        localStorage.setItem('role', res.data.role);
        setUser({ username: res.data.username, role: res.data.role });
    };

    const register = async (username, email, password) => {
        const res = await axiosClient.post('/auth/register', { username, email, password });
        localStorage.setItem('token', res.data.token);
        localStorage.setItem('username', res.data.username);
        localStorage.setItem('role', res.data.role);
        setUser({ username: res.data.username, role: res.data.role });
    };

    const logout = () => {
        localStorage.clear();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);