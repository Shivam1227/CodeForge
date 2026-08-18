import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import './AdminPages.css';

const EMPTY_PROBLEM = { title: '', description: '', difficulty: 'EASY', timeLimitMs: 2000, memoryLimitMb: 256 };
const EMPTY_TESTCASE = { input: '', expectedOutput: '', isSample: false };

export default function AdminProblemFormPage() {
    const { id } = useParams(); // undefined when creating a new problem
    const isEditing = Boolean(id);
    const navigate = useNavigate();

    const [problem, setProblem] = useState(EMPTY_PROBLEM);
    const [testCases, setTestCases] = useState([]);
    const [newTestCase, setNewTestCase] = useState(EMPTY_TESTCASE);

    useEffect(() => {
        if (isEditing) {
            axiosClient.get(`/problems/${id}`).then((res) => setProblem(res.data));
            loadTestCases();
        }
    }, [id]);

    const loadTestCases = () => axiosClient.get(`/admin/problems/${id}/testcases`).then((res) => setTestCases(res.data));

    const handleSaveProblem = async (e) => {
        e.preventDefault();
        if (isEditing) {
            await axiosClient.put(`/admin/problems/${id}`, problem);
        } else {
            const res = await axiosClient.post('/admin/problems', problem);
            navigate(`/admin/problems/${res.data.id}`); // switch into edit mode so test cases can be added
        }
    };

    const handleAddTestCase = async (e) => {
        e.preventDefault();
        await axiosClient.post(`/admin/problems/${id}/testcases`, newTestCase);
        setNewTestCase(EMPTY_TESTCASE);
        loadTestCases();
    };

    const handleDeleteTestCase = async (tcId) => {
        await axiosClient.delete(`/admin/testcases/${tcId}`);
        loadTestCases();
    };

    return (
        <div className="admin-page">
            <h1>{isEditing ? 'Edit Problem' : 'New Problem'}</h1>

            <form className="admin-form" onSubmit={handleSaveProblem}>
                <input placeholder="Title" value={problem.title}
                       onChange={(e) => setProblem({ ...problem, title: e.target.value })} required />
                <textarea placeholder="Description" rows={5} value={problem.description}
                          onChange={(e) => setProblem({ ...problem, description: e.target.value })} required />
                <div className="admin-form-row">
                    <select value={problem.difficulty} onChange={(e) => setProblem({ ...problem, difficulty: e.target.value })}>
                        <option value="EASY">Easy</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HARD">Hard</option>
                    </select>
                    <input type="number" placeholder="Time limit (ms)" value={problem.timeLimitMs}
                           onChange={(e) => setProblem({ ...problem, timeLimitMs: Number(e.target.value) })} required />
                    <input type="number" placeholder="Memory limit (MB)" value={problem.memoryLimitMb}
                           onChange={(e) => setProblem({ ...problem, memoryLimitMb: Number(e.target.value) })} required />
                </div>
                <button type="submit" className="admin-btn-primary">
                    {isEditing ? 'Save Changes' : 'Create Problem'}
                </button>
            </form>

            {isEditing && (
                <div className="admin-testcases">
                    <h2>Test Cases</h2>

                    {testCases.map((tc) => (
                        <div className="admin-testcase-row" key={tc.id}>
                            <pre className="mono">{tc.input}</pre>
                            <pre className="mono">{tc.expectedOutput}</pre>
                            <span className="mono">{tc.isSample ? 'sample' : 'hidden'}</span>
                            <button className="admin-btn-danger" onClick={() => handleDeleteTestCase(tc.id)}>Delete</button>
                        </div>
                    ))}

                    <form className="admin-form" onSubmit={handleAddTestCase}>
            <textarea placeholder="Input" rows={3} value={newTestCase.input}
                      onChange={(e) => setNewTestCase({ ...newTestCase, input: e.target.value })} required />
                        <textarea placeholder="Expected Output" rows={3} value={newTestCase.expectedOutput}
                                  onChange={(e) => setNewTestCase({ ...newTestCase, expectedOutput: e.target.value })} required />
                        <label className="admin-checkbox">
                            <input type="checkbox" checked={newTestCase.isSample}
                                   onChange={(e) => setNewTestCase({ ...newTestCase, isSample: e.target.checked })} />
                            Visible as a sample test case
                        </label>
                        <button type="submit" className="admin-btn-secondary">+ Add Test Case</button>
                    </form>
                </div>
            )}
        </div>
    );
}