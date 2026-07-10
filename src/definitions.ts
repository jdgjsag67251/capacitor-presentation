import type { PluginListenerHandle } from "@capacitor/core";

export type OpenOptions =
	| {
			type: "url";
			url: string;
	  }
	| {
			type: "video";
			options: {
				url: string;
				showControls?: boolean;
			};
	  }
	| {
			type: "html";
			html: string;
	  };

export type OpenResponse =
	| { success: true }
	| { success: false; error: string };

export interface CapacitorPresentationPlugin {
	open(options: OpenOptions): Promise<OpenResponse>;
	sendMessage(message: Record<string, unknown>): Promise<void>;
	getDisplays(): Promise<{ displays: number }>;
	terminate(): Promise<void>;

	/**
	 * @param eventName
	 * @param listenerFunc <br>
	 *
	 * Works only if type html of url or if browser
	 */
	addListener(
		eventName: "onSuccessLoadUrl",
		listenerFunc: () => void,
	): Promise<PluginListenerHandle>;
	addListener(
		eventName: "onFailLoadUrl",
		listenerFunc: (error: string) => void,
	): Promise<PluginListenerHandle>;
	addListener(
		eventName: "onMessage",
		listenerFunc: (message: Record<string, unknown>) => void,
	): Promise<PluginListenerHandle>;
	addListener(
		eventName: "onClose",
		listenerFunc: () => void,
	): Promise<PluginListenerHandle>;
}
