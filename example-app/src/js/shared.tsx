import { CssBaseline } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

const darkTheme = createTheme({
	palette: {
		mode: "dark",
	},
});

function Wrapper({ children }: { children: React.ReactNode }) {
	return (
		<StrictMode>
			<ThemeProvider theme={darkTheme}>
				<CssBaseline />
				{children}
			</ThemeProvider>
		</StrictMode>
	);
}

export const render = (children: React.ReactNode) => {
	const root = document.getElementById("root");

	if (root) {
		createRoot(root).render(<Wrapper>{children}</Wrapper>);
	}
};
