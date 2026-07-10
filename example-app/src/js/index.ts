import { Capacitor } from "@capacitor/core";
import { CapacitorPresentation } from "capacitor-presentation";

const htmlExample = `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Example</title>
    <style>
        body, html {
            width: 100%;
            height: 100%;
        }
        .container {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100dvh;
            width: 100dvw;
            font-size: 1.5rem;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        This is example html Content!
    </div>
</body>
</html>`;

document.getElementById("url")?.addEventListener("click", () => {
	CapacitorPresentation.open({
		type: "url",
		url: "https://github.com",
	}).catch(console.error);
});

{
	const htmlElement = document.getElementById("html");
	if (!Capacitor.isNativePlatform()) {
		htmlElement?.setAttribute("disabled", "true");
	}
	htmlElement?.addEventListener("click", () => {
		CapacitorPresentation.open({
			type: "html",
			html: htmlExample,
		}).catch(console.error);
	});
}

document.getElementById("terminate")?.addEventListener("click", () => {
	CapacitorPresentation.terminate().catch(console.error);
});
