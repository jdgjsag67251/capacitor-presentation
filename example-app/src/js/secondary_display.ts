window.onPresentationMessage = ({ data }) => {
	const p = document.createElement("p");
	p.textContent = data;
	document.body.appendChild(p);

	window.sendPresentationMessage({
		data: data === "Ping" ? "Pong" : "Unknown message",
	});
};
