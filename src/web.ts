import { WebPlugin } from "@capacitor/core";
import type {
	CapacitorPresentationPlugin,
	Display,
	OpenOptions,
	OpenResponse,
} from "./definitions";

export class CapacitorPresentationWeb
	extends WebPlugin
	implements CapacitorPresentationPlugin
{
	private presentationConnection: PresentationConnection | undefined;

	async open(options: OpenOptions): Promise<OpenResponse> {
		const data = (() => {
			switch (options.type) {
				case "url":
					return options.url;
				case "html":
					throw new Error("Not supported on web");
				default:
					return null;
			}
		})();

		if (!data) {
			throw new Error("Please enter all required values!");
		}

		try {
			await this.startDisplay(data);

			this.notifyListeners("onSuccessLoadUrl", undefined);
			return { success: true };
		} catch (error) {
			this.notifyListeners("onFailLoadUrl", error);

			return {
				error: error instanceof Error ? error.message : String(error),
				success: false,
			};
		}
	}

	async sendMessage(message: { data: Record<string, unknown> }): Promise<void> {
		if (!message.data) {
			throw new Error("Invalid arguments");
		}

		this.presentationConnection?.send(JSON.stringify({ data: message.data }));
	}

	async terminate() {
		return this.presentationConnection?.terminate();
	}

	async getDisplays(): Promise<{ displays: Display[] }> {
		const presentationRequest = new window.PresentationRequest([""]);
		const { value } = await presentationRequest.getAvailability().catch(() => ({
			value: false,
		}));

		return { displays: value ? [{ displayId: 0 }] : [] };
	}

	private async startDisplay(data: string): Promise<void> {
		this.presentationConnection?.terminate();

		const presentationRequest = new window.PresentationRequest([data]);
		const connectionPromise = new Promise<PresentationConnectionAvailableEvent>(
			(resolve) =>
				presentationRequest.addEventListener("connectionavailable", resolve),
		);

		await presentationRequest.start();

		const connectionEvent = await connectionPromise;
		this.presentationConnection = connectionEvent.connection;

		this.presentationConnection.addEventListener("close", () => {
			this.notifyListeners("onClose", undefined);
		});
		this.presentationConnection.addEventListener("terminate", () => {
			this.notifyListeners("onClose", undefined);
		});
		this.presentationConnection.addEventListener("message", (event) => {
			const data = (() => {
				try {
					return JSON.parse(event.data)?.data;
				} catch {
					return null;
				}
			})();

			if (data) {
				this.notifyListeners("onMessage", { data });
			}
		});
	}
}

export const setupWebPresentationReceiverAPI = async () => {
	if (!navigator.presentation?.receiver) {
		throw new Error("Presentation receiver is not available");
	}

	const setupReceiverMessaging = (connection: PresentationConnection) => {
		connection.onmessage = (event) => {
			// @ts-expect-error: missing types
			window.onPresentationMessage?.(JSON.parse(event.data));
		};

		// @ts-expect-error: missing types
		window.sendPresentationMessage = (value: {
			data: Record<string, unknown>;
		}) => {
			connection.send(JSON.stringify(value));
		};
	};

	const list = await navigator.presentation.receiver.connectionList;

	if (list.connections.length) {
		setupReceiverMessaging(list.connections[0]);
	} else {
		list.onconnectionavailable = (event) => {
			list.onconnectionavailable = null;
			setupReceiverMessaging(event.connection);
		};
	}
};
