import { Capacitor, registerPlugin } from "@capacitor/core";
import type { CapacitorPresentationPlugin } from "./definitions";

const Presentation = registerPlugin<CapacitorPresentationPlugin>(
	"Presentation",
	{
		web: () => import("./web").then((m) => new m.CapacitorPresentationWeb()),
	},
);

if (!Capacitor.isNativePlatform() && navigator.presentation?.receiver) {
	import("./web")
		.then((m) => m.setupWebPresentationReceiverAPI())
		.catch(console.error);
}

export * from "./definitions";
export { Presentation };
