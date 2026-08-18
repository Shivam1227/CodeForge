import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [menuOpen, setMenuOpen] = useState(false);

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">
                <span className="mono">&gt;_</span> CodeForge
            </Link>

            {user && (
                <div className="navbar-center">
                    <Link to="/" className="navbar-nav-link">Problems</Link>
                    <Link to="/profile" className="navbar-nav-link">Submissions</Link>
                </div>
            )}

            <div className="navbar-links">
                {user ? (
                    <>
                        {user.role === 'ADMIN' && (
                            <Link to="/admin" className="navbar-admin-link">
                                Admin
                            </Link>
                        )}

                        <div className="navbar-user-menu">
                            <button
                                className="navbar-user-trigger"
                                onClick={() => setMenuOpen((o) => !o)}
                            >
                                <span className="mono">{user.username}</span>
                                <span className="navbar-chevron">▾</span>
                            </button>

                            {menuOpen && (
                                <div className="navbar-dropdown">
                                    <Link
                                        to="/profile"
                                        className="navbar-dropdown-item"
                                        onClick={() => setMenuOpen(false)}
                                    >
                                        Profile
                                    </Link>

                                    <button
                                        className="navbar-dropdown-item"
                                        onClick={() => {
                                            setMenuOpen(false);
                                            logout();
                                            navigate('/login');
                                        }}
                                    >
                                        Logout
                                    </button>
                                </div>
                            )}
                        </div>
                    </>
                ) : (
                    <Link to="/login" className="navbar-btn">Log in</Link>
                )}
            </div>
        </nav>
    );
}