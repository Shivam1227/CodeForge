import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import Heatmap from '../components/Heatmap';
import VerdictBadge from '../components/VerdictBadge';
import './ProfilePage.css';

export default function ProfilePage() {
    const [profile, setProfile] = useState(null);
    const [history, setHistory] = useState([]);
    const [heatmapData, setHeatmapData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            axiosClient.get('/profile'),
            axiosClient.get('/profile/submissions'),
            axiosClient.get('/profile/heatmap'),
        ]).then(([profileRes, historyRes, heatmapRes]) => {
            setProfile(profileRes.data);
            setHistory(historyRes.data);
            setHeatmapData(heatmapRes.data);
        }).finally(() => setLoading(false));
    }, []);

    if (loading) return <p className="profile-loading">Loading…</p>;

    const memberSince = profile.memberSince
        ? new Date(profile.memberSince).toLocaleDateString(undefined, { year: 'numeric', month: 'long' })
        : '—';

    return (
        <div className="profile-page">
            <div className="profile-header">
                <div>
                    <h1>{profile.username}</h1>
                    <p className="profile-meta mono">{profile.email} · member since {memberSince}</p>
                </div>
                <div className="profile-stats">
                    <div className="stat-card">
                        <span className="stat-value mono">{profile.solvedCount}</span>
                        <span className="stat-label">Solved</span>
                    </div>
                    <div className="stat-card">
                        <span className="stat-value mono">{profile.totalSubmissions}</span>
                        <span className="stat-label">Submissions</span>
                    </div>
                </div>
            </div>

            <section className="profile-section">
                <h2>Activity</h2>
                <div className="heatmap-card">
                    <Heatmap data={heatmapData} />
                </div>
            </section>

            <section className="profile-section">
                <h2>Submission History</h2>
                {history.length === 0 && <p className="profile-empty">No submissions yet — go solve something.</p>}
                {history.length > 0 && (
                    <div className="history-table">
                        {history.map((h) => (
                            <div className="history-row" key={h.submissionId}>
                                <Link to={`/problems/${h.problemId}`} className="history-title">{h.problemTitle}</Link>
                                <span className="mono history-lang">{h.language}</span>
                                <span className="mono history-pass">
                  {h.passedCount != null ? `${h.passedCount}/${h.totalCount}` : '—'}
                </span>
                                <VerdictBadge status={h.status} />
                                <span className="mono history-date">{new Date(h.createdAt).toLocaleString()}</span>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}