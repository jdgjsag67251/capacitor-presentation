import type { PluginListenerHandle } from "@capacitor/core";

export type OpenOptions = { displayId?: unknown } & (
	| {
			type: "url";
			url: string;
	  }
	| {
			type: "html";
			html: string;
	  }
);

export type OpenResponse =
	| { success: true }
	| { success: false; error: string };

enum AndroidDisplayDetailsRotation {
	/** In degrees */
	None = 0,
	/** In degrees */
	SidewaysLeft = 90,
	/** In degrees */
	UpsideDown = 180,
	/** In degrees */
	SidewaysRight = 270,
}
type AndroidDisplayDetails = {
	name: string;
	isHdr?: boolean;
	rotation?: AndroidDisplayDetailsRotation;
	/** In pixels */
	width?: number;
	/** In pixels */
	height?: number;
	isDefaultDisplay?: boolean;
	cutout?: {
		safeInsetTop: number;
		safeInsetLeft: number;
		safeInsetBottom: number;
		safeInsetRight: number;
	};
	productInfo?: {
		manufacturerPnpId: string;
		productUId: string;
		name?: string;
		modelYear?: number;
		manufactureYear?: number;
		manufactureWeek?: number;
	};
};

export type Display = {
	displayId: unknown;
} & Partial<AndroidDisplayDetails>;

export interface CapacitorPresentationPlugin {
	open(options: OpenOptions): Promise<OpenResponse>;
	sendMessage(message: { data: Record<string, unknown> }): Promise<void>;
	getDisplays(): Promise<{ displays: Display[] }>;
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
		listenerFunc: (message: { data: Record<string, unknown> }) => void,
	): Promise<PluginListenerHandle>;
	addListener(
		eventName: "onClose",
		listenerFunc: () => void,
	): Promise<PluginListenerHandle>;
}
