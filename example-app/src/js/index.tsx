import { Alert, Box, Button, Container, Typography } from "@mui/material";
import { type OpenOptions, Presentation } from "capacitor-presentation";
import { useCallback, useEffect, useState } from "react";
import { render } from "./shared";
import htmlTemplate from "./template.html?raw";

type EventHandlers = {
	onSuccessLoadUrl: (...args: unknown[]) => void;
	onFailLoadUrl: (...args: unknown[]) => void;
	onMessage: (value: Record<string, unknown>) => void;
};
const getEventHandler = (() => {
	class CapacitorEventHandler {
		private handlers: Partial<EventHandlers> = {};

		constructor() {
			Promise.all([
				Presentation.addListener("onSuccessLoadUrl", (...args) => {
					this.handlers.onSuccessLoadUrl?.(...args);
				}),
				Presentation.addListener("onFailLoadUrl", (...args) => {
					this.handlers.onFailLoadUrl?.(...args);
				}),
				Presentation.addListener("onMessage", (...args) => {
					this.handlers.onMessage?.(...args);
				}),
			]).catch(console.error);
		}

		setHandlers(handlers: Partial<EventHandlers>) {
			this.handlers = handlers;
		}

		removeHandlers() {
			this.handlers = {};
		}
	}

	let instance: CapacitorEventHandler | undefined;

	return () => {
		if (!instance) {
			instance = new CapacitorEventHandler();
		}

		return instance;
	};
})();

function App() {
	type Message = { id: number; type: "plain" | "error"; value: string };

	const [active, setActive] = useState(false);
	const [messages, setMessages] = useState<Message[]>([]);

	const addMessage = useCallback((message: Omit<Message, "id">) => {
		setMessages((prev) => [...prev, { id: Date.now(), ...message }]);
	}, []);

	const terminate = useCallback(() => {
		Presentation.terminate()
			.then(() => {
				setActive(false);
			})
			.catch((err) => {
				console.error(err);
				addMessage({ type: "error", value: err.message });
			});
	}, [addMessage]);

	const ping = useCallback(() => {
		Presentation.sendMessage({ data: { message: "Ping" } }).catch((err) => {
			console.error(err);
			addMessage({ type: "error", value: err.message });
		});
	}, [addMessage]);

	const load = (options: OpenOptions) => () => {
		Presentation.open(options)
			.then((result) => {
				if (result.success) {
					setActive(true);
				} else {
					throw new Error(result.error);
				}
			})
			.catch((err) => {
				console.error(err);
				addMessage({ type: "error", value: err.message });
			});
	};

	useEffect(() => {
		if (active) {
			const eventHandler = getEventHandler();

			eventHandler.setHandlers({
				onSuccessLoadUrl: () => {
					addMessage({ type: "plain", value: "Loaded display" });
				},
				onFailLoadUrl: () => {
					addMessage({ type: "error", value: "Failed to load display" });
				},
				onMessage: ({ data }) => {
					addMessage({ type: "plain", value: String(data) });
				},
			});

			return () => eventHandler.removeHandlers();
		}
	}, [active, addMessage]);

	return (
		<Container
			maxWidth="sm"
			sx={{
				width: "100vw",
				height: "100vh",
				pb: "env(safe-area-inset-bottom, 0px)",
				pt: "calc(env(safe-area-inset-top, 0px) + 32px)",
			}}
		>
			<Typography component="h1" variant="h5" sx={{ mb: 3 }}>
				Capacitor Presentation
			</Typography>

			<Typography gutterBottom>Types</Typography>
			<Box
				sx={{
					display: "grid",
					gridTemplateColumns: "repeat(2, 1fr)",
					gap: 2,
					mb: 2,
				}}
			>
				<Button
					variant="contained"
					onClick={load({ type: "url", url: "https://github.com" })}
				>
					Public URL
				</Button>
				<Button
					variant="contained"
					onClick={load({ type: "url", url: "/secondary_display.html" })}
				>
					Internal file
				</Button>
				<Button
					variant="contained"
					onClick={load({ type: "html", html: htmlTemplate })}
				>
					HTML string
				</Button>
			</Box>

			<Typography gutterBottom>Actions</Typography>
			<Box
				sx={{
					display: "grid",
					gridTemplateColumns: "repeat(2, 1fr)",
					gap: 2,
					mb: 2,
				}}
			>
				<Button variant="contained" disabled={!active} onClick={terminate}>
					Terminate
				</Button>
				<Button variant="contained" disabled={!active} onClick={ping}>
					Ping
				</Button>
			</Box>

			{!!messages.length && (
				<>
					<Typography gutterBottom>Messages</Typography>
					<Button
						size="small"
						sx={{ mb: 1 }}
						variant="outlined"
						onClick={() => setMessages([])}
					>
						Clear
					</Button>
					<div>
						{messages.map((m) => (
							<Alert
								key={m.id}
								severity={m.type === "error" ? "error" : "info"}
							>
								{m.value}
							</Alert>
						))}
					</div>
				</>
			)}
		</Container>
	);
}

render(<App />);
