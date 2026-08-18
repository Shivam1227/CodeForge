import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import './AdminPages.css';

export default function AdminProblemListPage() {
    const [problems, setProblems] = useState([]);

    const load = () => axiosClient.get('/problems').then((res) => setProblems(res.data));

    useEffect(() => { load(); }, []);

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this problem and all its test cases?')) return;
        await axiosClient.delete(`/admin/problems/${id}`);
        load();
    };

    return (
        <div className="admin-page">
            <div className="admin-header">
                <h1>Manage Problems</h1>
                <Link to="/admin/problems/new" className="admin-btn-primary">+ New Problem</Link>
            </div>
            <div className="admin-table">
                {problems.map((p) => (
                    <div className="admin-row" key={p.id}>
                        <span>{p.title}</span>
                        <span className="mono admin-row-diff">{p.difficulty}</span>
                        <div className="admin-row-actions">
                            <Link to={`/admin/problems/${p.id}`} className="admin-btn-secondary">Edit</Link>
                            <button className="admin-btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}