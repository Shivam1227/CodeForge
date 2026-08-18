import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';

// Opens one STOMP connection, subscribes to a single submission's topic,
// calls onMessage for every update. Returns a cleanup function.
export function subscribeToSubmission(submissionId, onMessage) {
    const socket = new SockJS('http://localhost/ws');
    const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        onConnect: () => {
            client.subscribe(`/topic/submissions/${submissionId}`, (message) => {
                onMessage(JSON.parse(message.body));
            });
        },
    });
    client.activate();

    return () => client.deactivate();
}

export function subscribeToRun(runId, onMessage) {
    const socket = new SockJS('http://localhost/ws');
    const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        onConnect: () => {
            client.subscribe(`/topic/runs/${runId}`, (message) => {
                onMessage(JSON.parse(message.body));
            });
        },
    });
    client.activate();
    return () => client.deactivate();
}