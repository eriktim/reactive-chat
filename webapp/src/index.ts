import { RSocketClient, JsonSerializer, IdentitySerializer } from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

const userElement = <HTMLInputElement>document.getElementById('user');
const newMessageElement = <HTMLInputElement>document.getElementById('new-message');
const sendButtonElement = document.getElementById('send-button');
const messagesElement = document.getElementById('messages');

if (!newMessageElement || !sendButtonElement || !messagesElement) {
    throw Error('cannot bootstrap missing elements');
}

const route = (value: string) => String.fromCharCode(value.length) + value

const client = new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: new RSocketWebSocketClient({ url: 'ws://localhost:7000' }),
});

client.connect().subscribe({
    onComplete: socket => {
        socket.requestStream({
            metadata: route('readMessages')
        }).subscribe(payload => {
            const message = payload.data;
            const element = document.createElement('div');
            const textElement = document.createTextNode(`[${message.name}] ${message.message}`);
            element.appendChild(textElement);
            messagesElement.appendChild(element);
        });
        sendButtonElement.onclick = () => {
            socket.fireAndForget({
                data: {
                    name: userElement.value || '',
                    message: newMessageElement.value || ''
                },
                metadata: route('sendMessage'),
            });
        };
    },
    onError: error => console.error('RSocket error: ' + error),
    onSubscribe: cancel => {/* call cancel() to abort */ }
});