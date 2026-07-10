type PresentationConnectionState =
	| "connecting"
	| "connected"
	| "closed"
	| "terminated";

type PresentationConnectionCloseReason = "error" | "closed" | "wentaway";

interface PresentationConnectionEventMap {
	connect: Event;
	close: PresentationConnectionCloseEvent;
	terminate: Event;
	message: MessageEvent;
}

interface PresentationConnectionCloseEvent extends Event {
	readonly reason: PresentationConnectionCloseReason;
	readonly message: string;
}

interface PresentationConnectionAvailableEvent extends Event {
	readonly connection: PresentationConnection;
}

interface PresentationConnection extends EventTarget {
	readonly id: string;
	readonly url: string;
	readonly state: PresentationConnectionState;
	binaryType: BinaryType;

	onconnect: ((this: PresentationConnection, ev: Event) => void) | null;
	onclose:
		| ((
				this: PresentationConnection,
				ev: PresentationConnectionCloseEvent,
		  ) => void)
		| null;
	onterminate: ((this: PresentationConnection, ev: Event) => void) | null;
	onmessage: ((this: PresentationConnection, ev: MessageEvent) => void) | null;

	close(): void;
	terminate(): void;
	send(data: string | Blob | ArrayBuffer | ArrayBufferView): void;

	addEventListener<K extends keyof PresentationConnectionEventMap>(
		type: K,
		listener: (
			this: PresentationConnection,
			ev: PresentationConnectionEventMap[K],
		) => void,
		options?: boolean | AddEventListenerOptions,
	): void;
	addEventListener(
		type: string,
		listener: EventListenerOrEventListenerObject,
		options?: boolean | AddEventListenerOptions,
	): void;
	removeEventListener<K extends keyof PresentationConnectionEventMap>(
		type: K,
		listener: (
			this: PresentationConnection,
			ev: PresentationConnectionEventMap[K],
		) => void,
		options?: boolean | EventListenerOptions,
	): void;
	removeEventListener(
		type: string,
		listener: EventListenerOrEventListenerObject,
		options?: boolean | EventListenerOptions,
	): void;
}

interface PresentationAvailability extends EventTarget {
	readonly value: boolean;
	onchange: ((this: PresentationAvailability, ev: Event) => void) | null;
}

interface PresentationRequestEventMap {
	connectionavailable: PresentationConnectionAvailableEvent;
}

interface PresentationRequest extends EventTarget {
	onconnectionavailable:
		| ((
				this: PresentationRequest,
				ev: PresentationConnectionAvailableEvent,
		  ) => void)
		| null;

	start(): Promise<PresentationConnection>;
	reconnect(presentationId: string): Promise<PresentationConnection>;
	getAvailability(): Promise<PresentationAvailability>;

	addEventListener<K extends keyof PresentationRequestEventMap>(
		type: K,
		listener: (
			this: PresentationRequest,
			ev: PresentationRequestEventMap[K],
		) => void,
		options?: boolean | AddEventListenerOptions,
	): void;
	addEventListener(
		type: string,
		listener: EventListenerOrEventListenerObject,
		options?: boolean | AddEventListenerOptions,
	): void;
	removeEventListener<K extends keyof PresentationRequestEventMap>(
		type: K,
		listener: (
			this: PresentationRequest,
			ev: PresentationRequestEventMap[K],
		) => void,
		options?: boolean | EventListenerOptions,
	): void;
	removeEventListener(
		type: string,
		listener: EventListenerOrEventListenerObject,
		options?: boolean | EventListenerOptions,
	): void;
}

declare var PresentationRequest: {
	prototype: PresentationRequest;
	new (urls: string | string[]): PresentationRequest;
};

interface Window {
	PresentationRequest: typeof PresentationRequest;
}

interface PresentationConnectionList extends EventTarget {
	readonly connections: ReadonlyArray<PresentationConnection>;
	onconnectionavailable:
		| ((
				this: PresentationConnectionList,
				ev: PresentationConnectionAvailableEvent,
		  ) => void)
		| null;
}

interface PresentationReceiver {
	readonly connectionList: Promise<PresentationConnectionList>;
}

interface Presentation {
	defaultRequest: PresentationRequest | null;
	readonly receiver: PresentationReceiver | null;
}

interface Navigator {
	readonly presentation?: Presentation;
}
