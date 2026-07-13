import { registerPlugin } from "@capacitor/core";
import type { CapacitorPresentationPlugin } from "./definitions";

const Presentation = registerPlugin<CapacitorPresentationPlugin>(
	"Presentation",
	{
		web: () => import("./web").then((m) => new m.CapacitorPresentationWeb()),
	},
);

export * from "./definitions";
export { Presentation };
