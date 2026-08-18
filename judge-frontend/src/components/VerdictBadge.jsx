import './VerdictBadge.css';

const VERDICT_STYLES = {
    PENDING: { label: 'Pending', color: 'dim' },
    RUNNING: { label: 'Running', color: 'warning' },
    ACCEPTED: { label: 'Accepted', color: 'success' },
    WRONG_ANSWER: { label: 'Wrong Answer', color: 'danger' },
    TIME_LIMIT_EXCEEDED: { label: 'Time Limit Exceeded', color: 'danger' },
    MEMORY_LIMIT_EXCEEDED: { label: 'Memory Limit Exceeded', color: 'danger' },
    RUNTIME_ERROR: { label: 'Runtime Error', color: 'danger' },
    COMPILE_ERROR: { label: 'Compile Error', color: 'danger' },
};

export default function VerdictBadge({ status }) {
    const style = VERDICT_STYLES[status] || { label: status, color: 'dim' };
    return <span className={`verdict-badge verdict-${style.color} mono`}>{style.label}</span>;
}