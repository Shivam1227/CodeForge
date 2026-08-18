import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import './ProblemListPage.css';

const DIFFICULTY_COLOR = { EASY: 'success', MEDIUM: 'warning', HARD: 'danger' };
const DIFFICULTY_FILTERS = ['ALL', 'EASY', 'MEDIUM', 'HARD'];

// Consecutive-day streak, computed from heatmap dates that already carry a
// submission count > 0. If nothing was submitted today yet, we still count
// the streak from yesterday backward — same convention LeetCode-style
// trackers use, so a streak isn't lost just for not having submitted *yet* today.
function computeStreak(heatmapData) {
    const activeDates = new Set(heatmapData.map((d) => d.date));
    const toISO = (d) => d.toISOString().slice(0, 10);

    let cursor = new Date();
    if (!activeDates.has(toISO(cursor))) {
        cursor.setDate(cursor.getDate() - 1);
    }

    let streak = 0;
    while (activeDates.has(toISO(cursor))) {
        streak++;
        cursor.setDate(cursor.getDate() - 1);
    }
    return streak;
}

export default function ProblemListPage() {
    const { user } = useAuth();
    const [problems, setProblems] = useState([]);
    const [profile, setProfile] = useState(null);
    const [streak, setStreak] = useState(0);
    const [solvedIds, setSolvedIds] = useState(new Set());
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [difficulty, setDifficulty] = useState('ALL');

    useEffect(() => {
        Promise.all([
            axiosClient.get('/problems'),
            axiosClient.get('/profile'),
            axiosClient.get('/profile/heatmap'),
            axiosClient.get('/profile/solved-problems'),
        ]).then(([problemsRes, profileRes, heatmapRes, solvedRes]) => {
            setProblems(problemsRes.data);
            setProfile(profileRes.data);
            setStreak(computeStreak(heatmapRes.data));
            setSolvedIds(new Set(solvedRes.data));
        }).finally(() => setLoading(false));
    }, []);

    if (loading) return <p className="problem-list-status">Loading…</p>;

    const filtered = problems.filter((p) => {
        const matchesDifficulty = difficulty === 'ALL' || p.difficulty === difficulty;
        const matchesSearch = p.title.toLowerCase().includes(search.toLowerCase());
        return matchesDifficulty && matchesSearch;
    });

    return (
        <div className="home-page">
            <div className="welcome-section">
                <h1>Welcome back, {user?.username} 👋</h1>
                <p className="welcome-subtitle">Keep solving. Keep improving.</p>
            </div>

            <div className="stats-row">
                <div className="stat-card">
                    <span className="stat-value mono">{profile.solvedCount}</span>
                    <span className="stat-label">Problems Solved</span>
                </div>
                <div className="stat-card">
                    <span className="stat-value mono">{profile.totalSubmissions}</span>
                    <span className="stat-label">Submissions</span>
                </div>
                <div className="stat-card">
                    <span className="stat-value mono">{profile.accuracy}%</span>
                    <span className="stat-label">Accuracy</span>
                </div>
                <div className="stat-card">
                    <span className="stat-value mono">{streak} 🔥</span>
                    <span className="stat-label">Current Streak</span>
                </div>
            </div>

            <div className="problems-toolbar">
                <h2>Problems</h2>
                <input
                    className="problem-search"
                    placeholder="Search problems..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className="difficulty-filters">
                {DIFFICULTY_FILTERS.map((d) => (
                    <button
                        key={d}
                        className={`filter-pill ${difficulty === d ? 'active' : ''}`}
                        onClick={() => setDifficulty(d)}
                    >
                        {d === 'ALL' ? 'All' : d.charAt(0) + d.slice(1).toLowerCase()}
                    </button>
                ))}
            </div>

            {filtered.length === 0 && <p className="problem-list-status">No problems match.</p>}

            {filtered.length > 0 && (
                <div className="problem-table">
                    {filtered.map((p) => (
                        <Link to={`/problems/${p.id}`} key={p.id} className="problem-row">
              <span className="problem-row-left">
                {solvedIds.has(p.id) && <span className="solved-check">✓</span>}
                  <span className="problem-title">{p.title}</span>
              </span>
                            <span className={`problem-difficulty diff-${DIFFICULTY_COLOR[p.difficulty] || 'dim'}`}>
                {p.difficulty}
              </span>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}