import { Alert, Button, Container, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { render } from "./shared";

// Required for the custom web presentation API to be available
import "capacitor-presentation";

interface TypedWindow extends Window {
	onPresentationMessage?: (value: { data: any }) => void;
	sendPresentationMessage(value: { data: any }): void;
}

const typedWindow = window as unknown as TypedWindow;

function App() {
	const [messages, setMessages] = useState<{ id: number; data: string }[]>([]);

	useEffect(() => {
		typedWindow.onPresentationMessage = ({ data }) => {
			setMessages((prevMessages) => [
				...prevMessages,
				{ id: Date.now(), data: String(data.message) },
			]);

			if (
				typeof data.message === "string" &&
				data.message.toLowerCase() === "ping"
			) {
				typedWindow.sendPresentationMessage({ data: "Pong" });
			}
		};

		return () => {
			typedWindow.onPresentationMessage = undefined;
		};
	}, []);

	return (
		<Container
			maxWidth="sm"
			sx={{
				width: "100vw",
				height: "100vh",
				display: "flex",
				alignItems: "center",
				justifyContent: "center",
			}}
		>
			<div>
				<Typography>Hello from the secondary display!</Typography>
				{!!messages.length && (
					<>
						<Button
							size="small"
							sx={{ my: 2 }}
							variant="outlined"
							onClick={() => setMessages([])}
						>
							Clear
						</Button>
						{messages.map((message) => (
							<Alert key={message.id} severity="info">
								{message.data}
							</Alert>
						))}
					</>
				)}
			</div>
		</Container>
	);
}

render(<App />);
