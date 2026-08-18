import './Heatmap.css';

function colorLevel(count) {
    if (count === 0) return 0;
    if (count <= 1) return 1;
    if (count <= 3) return 2;
    if (count <= 6) return 3;
    return 4;
}

export default function Heatmap({ data }) {
    const countByDate = new Map(data.map((d) => [d.date, d.count]));

    const today = new Date();
    const days = [];
    for (let i = 370; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(d.getDate() - i);
        const iso = d.toISOString().slice(0, 10);
        days.push({ date: iso, count: countByDate.get(iso) || 0 });
    }

    // pad to start on a Sunday so weeks line up into clean columns
    const leadingEmpty = days.length ? new Date(days[0].date).getDay() : 0;
    const padded = [...Array(leadingEmpty).fill(null), ...days];

    const weeks = [];
    for (let i = 0; i < padded.length; i += 7) {
        weeks.push(padded.slice(i, i + 7));
    }

    const totalSubmissions = data.reduce((sum, d) => sum + d.count, 0);

    return (
        <div className="heatmap">
            <div className="heatmap-grid">
                {weeks.map((week, wi) => (
                    <div className="heatmap-week" key={wi}>
                        {week.map((day, di) =>
                            day ? (
                                <div
                                    key={di}
                                    className={`heatmap-cell level-${colorLevel(day.count)}`}
                                    title={`${day.date}: ${day.count} submission${day.count === 1 ? '' : 's'}`}
                                />
                            ) : (
                                <div key={di} className="heatmap-cell heatmap-cell-empty" />
                            )
                        )}
                    </div>
                ))}
            </div>
            <p className="heatmap-summary mono">{totalSubmissions} submissions in the last year</p>
        </div>
    );
}