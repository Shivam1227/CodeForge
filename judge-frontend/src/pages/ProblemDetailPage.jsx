import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import ReactMarkdown from 'react-markdown';
import axiosClient from '../api/axiosClient';
import { subscribeToSubmission, subscribeToRun } from '../api/websocket';
import VerdictBadge from '../components/VerdictBadge';
import './ProblemDetailPage.css';

const LANGUAGE_DEFAULTS = {
    java: '// write your solution here\npublic class Main {\n    public static void main(String[] args) {\n\n    }\n}',
    python: '# write your solution here\n',
    cpp: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n\n    return 0;\n}',
};
const MONACO_LANG = { java: 'java', python: 'python', cpp: 'cpp' };

const MIN_LEFT_PCT = 25;
const MAX_LEFT_PCT = 65;
const MIN_EDITOR_H = 180;
const MIN_RESULT_H = 140;
const TOOLBAR_AND_TABS_H = 90; // approx combined height of the toolbar + result-panel tab bar

export default function ProblemDetailPage() {
    const { id } = useParams();
    const [problem, setProblem] = useState(null);
    const [language, setLanguage] = useState('java');
    const [code, setCode] = useState(LANGUAGE_DEFAULTS.java);
    const [busy, setBusy] = useState(false);
    const [activeTab, setActiveTab] = useState('testcases');
    const [activeCase, setActiveCase] = useState(0);
    const [runResults, setRunResults] = useState(null);
    const [runError, setRunError] = useState(null);
    const [log, setLog] = useState([]);
    const [lastResult, setLastResult] = useState(null);
    const cleanupRef = useRef(null);

    // --- Resizable pane state ---
    const containerRef = useRef(null);
    const editorColRef = useRef(null);
    const [leftWidthPct, setLeftWidthPct] = useState(45);
    const [editorHeightPx, setEditorHeightPx] = useState(420);

    useEffect(() => {
        axiosClient.get(`/problems/${id}`).then((res) => setProblem(res.data));
        return () => cleanupRef.current?.();
    }, [id]);

    const handleLanguageChange = (lang) => {
        setLanguage(lang);
        setCode(LANGUAGE_DEFAULTS[lang]);
    };

    const appendLog = (text) => {
        const time = new Date().toLocaleTimeString();
        setLog((prev) => [...prev, { time, text }]);
    };

    const handleRun = async () => {
        setBusy(true);
        setRunResults(null);
        setRunError(null);
        setActiveTab('testcases');
        try {
            const res = await axiosClient.post(`/run/${id}`, { problemId: Number(id), language, sourceCode: code });
            cleanupRef.current?.();
            cleanupRef.current = subscribeToRun(res.data.runId, (update) => {
                if (update.overallStatus === 'COMPILE_ERROR' || update.overallStatus === 'RUNTIME_ERROR') {
                    setRunError(update.errorMessage);
                } else {
                    setRunResults(update.results);
                    setActiveCase(0);
                }
                setBusy(false);
            });
        } catch (err) {
            setRunError(err.response?.data?.error || 'Run failed');
            setBusy(false);
        }
    };

    const handleSubmit = async () => {
        setBusy(true);
        setLog([]);
        setLastResult(null);
        setActiveTab('console');
        try {
            const res = await axiosClient.post('/submissions', { problemId: Number(id), language, sourceCode: code });
            appendLog(`submission #${res.data.submissionId} queued`);

            cleanupRef.current?.();
            cleanupRef.current = subscribeToSubmission(res.data.submissionId, (update) => {
                appendLog(`status → ${update.status}`);
                setLastResult(update);
                if (update.status !== 'PENDING' && update.status !== 'RUNNING') {
                    setBusy(false);
                }
            });
        } catch (err) {
            appendLog(err.response?.data?.error || 'submission failed');
            setBusy(false);
        }
    };

    // --- Vertical divider: problem panel width vs editor column width ---
    const startColumnDrag = (e) => {
        e.preventDefault();
        const startX = e.clientX;
        const startPct = leftWidthPct;
        const containerWidth = containerRef.current.getBoundingClientRect().width;
        document.body.style.cursor = 'col-resize';
        document.body.style.userSelect = 'none';

        const onMove = (moveEvent) => {
            const deltaPct = ((moveEvent.clientX - startX) / containerWidth) * 100;
            const next = Math.min(MAX_LEFT_PCT, Math.max(MIN_LEFT_PCT, startPct + deltaPct));
            setLeftWidthPct(next);
        };
        const onUp = () => {
            document.body.style.cursor = '';
            document.body.style.userSelect = '';
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
        };
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    };

    // --- Horizontal divider: editor height vs result panel height ---
    const startRowDrag = (e) => {
        e.preventDefault();
        const startY = e.clientY;
        const startHeight = editorHeightPx;
        const colHeight = editorColRef.current.getBoundingClientRect().height;
        const maxEditorH = colHeight - MIN_RESULT_H - TOOLBAR_AND_TABS_H;
        document.body.style.cursor = 'row-resize';
        document.body.style.userSelect = 'none';

        const onMove = (moveEvent) => {
            const deltaY = moveEvent.clientY - startY;
            const next = Math.min(maxEditorH, Math.max(MIN_EDITOR_H, startHeight + deltaY));
            setEditorHeightPx(next);
        };
        const onUp = () => {
            document.body.style.cursor = '';
            document.body.style.userSelect = '';
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
        };
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    };

    if (!problem) return <p className="problem-detail-loading">Loading…</p>;

    const samples = problem.sampleTestCases || [];
    const selectedCaseResult = runResults ? runResults[activeCase] : null;

    return (
        <div className="problem-detail" ref={containerRef}>
            <div className="problem-panel" style={{ width: `${leftWidthPct}%` }}>
                <h1>{problem.title}</h1>
                <p className="problem-meta mono">
                    {problem.difficulty} · {problem.timeLimitMs}ms · {problem.memoryLimitMb}MB
                </p>
                <div className="problem-description">
                    <ReactMarkdown>{problem.description}</ReactMarkdown>
                </div>
            </div>

            <div className="resizer resizer-vertical" onMouseDown={startColumnDrag} />

            <div className="editor-panel" style={{ width: `${100 - leftWidthPct}%` }} ref={editorColRef}>
                <div className="editor-toolbar">
                    <select value={language} onChange={(e) => handleLanguageChange(e.target.value)}>
                        <option value="java">Java</option>
                        <option value="python">Python</option>
                        <option value="cpp">C++</option>
                    </select>
                    <div className="editor-toolbar-right">
                        {lastResult && (
                            <span className="pass-count mono">{lastResult.passedCount ?? 0}/{lastResult.totalCount ?? '?'} passed</span>
                        )}
                        {lastResult && <VerdictBadge status={lastResult.status} />}
                        <button className="run-btn" onClick={handleRun} disabled={busy}>Run</button>
                        <button className="submit-btn" onClick={handleSubmit} disabled={busy}>
                            {busy ? 'Judging…' : 'Submit'}
                        </button>
                    </div>
                </div>

                <div className="editor-wrapper" style={{ height: `${editorHeightPx}px` }}>
                    <Editor
                        height="100%"
                        language={MONACO_LANG[language]}
                        theme="vs-dark"
                        value={code}
                        onChange={(value) => setCode(value ?? '')}
                        options={{ fontSize: 14, minimap: { enabled: false }, fontFamily: 'JetBrains Mono', automaticLayout: true }}
                    />
                </div>

                <div className="resizer resizer-horizontal" onMouseDown={startRowDrag} />

                <div className="result-panel">
                    <div className="result-tabs">
                        <button className={activeTab === 'testcases' ? 'active' : ''} onClick={() => setActiveTab('testcases')}>Test Cases</button>
                        <button className={activeTab === 'console' ? 'active' : ''} onClick={() => setActiveTab('console')}>Console</button>
                    </div>

                    {activeTab === 'testcases' && (
                        <div className="testcases-body">
                            {samples.length === 0 && <p className="result-empty">No sample test cases for this problem.</p>}

                            {samples.length > 0 && (
                                <>
                                    <div className="case-pills">
                                        {samples.map((_, i) => {
                                            const r = runResults?.[i];
                                            const pillClass = r ? (r.status === 'PASSED' ? 'pill-pass' : 'pill-fail') : '';
                                            return (
                                                <button key={i} className={`case-pill ${pillClass} ${activeCase === i ? 'active' : ''}`}
                                                        onClick={() => setActiveCase(i)}>
                                                    Case {i + 1}
                                                </button>
                                            );
                                        })}
                                    </div>

                                    <div className="case-detail mono">
                                        <div className="case-block">
                                            <span className="case-label">Input</span>
                                            <pre>{samples[activeCase].input}</pre>
                                        </div>
                                        <div className="case-block">
                                            <span className="case-label">Expected Output</span>
                                            <pre>{samples[activeCase].expectedOutput}</pre>
                                        </div>
                                        {selectedCaseResult && (
                                            <div className="case-block">
                        <span className={`case-label ${selectedCaseResult.status === 'PASSED' ? 'label-pass' : 'label-fail'}`}>
                          Actual Output — {selectedCaseResult.status}
                        </span>
                                                <pre>{selectedCaseResult.actualOutput || '(no output)'}</pre>
                                            </div>
                                        )}
                                    </div>

                                    {runError && <pre className="run-error mono">{runError}</pre>}
                                </>
                            )}
                        </div>
                    )}

                    {activeTab === 'console' && (
                        <div className="console-body mono">
                            {log.length === 0 && <p className="result-empty">$ awaiting submission…</p>}
                            {log.map((line, i) => (
                                <p key={i}><span className="console-time">[{line.time}]</span> {line.text}</p>
                            ))}
                            {lastResult?.errorMessage && <pre className="run-error mono">{lastResult.errorMessage}</pre>}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}