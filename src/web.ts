import { WebPlugin } from "@capacitor/core";
import type {
	CapacitorPresentationPlugin,
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
				case "video":
					return options.options?.url;
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

	async sendMessage(message: Record<string, unknown>): Promise<void> {
		this.presentationConnection?.send(JSON.stringify(message));
	}

	async terminate() {
		return this.presentationConnection?.terminate();
	}

	async getDisplays(): Promise<{ displays: number }> {
		const presentationRequest = new window.PresentationRequest([""]);
		const { value } = await presentationRequest.getAvailability().catch(() => ({
			value: false,
		}));

		return { displays: value ? 1 : 0 };
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
			this.notifyListeners("onMessage", event.data);
		});
	}
}
