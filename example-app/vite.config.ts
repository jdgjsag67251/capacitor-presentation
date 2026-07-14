import { resolve } from "node:path";
import react from "@vitejs/plugin-react-swc";
import { defineConfig } from "vite";

export default defineConfig({
	plugins: [react()],
	root: "./src",
	build: {
		outDir: "../dist",
		emptyOutDir: true,
		rollupOptions: {
			input: {
				index: resolve(__dirname, "src/index.html"),
				secondary_display: resolve(__dirname, "src/secondary_display.html"),
			},
		},
	},
});
