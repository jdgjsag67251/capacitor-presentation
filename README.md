# Presentation API Capacitor Plugin

**Forked from [TheeMachine/presentation-capacitor](https://github.com/TheeMachine/presentation-capacitor).**

This Capacitor plugin provides seamless integration with the Presentation API, enabling developers to display content on secondary screens, such as projectors or external displays, directly from their mobile and web applications.

## Features

- **Multiple Screen Support:** Easily present content on external displays or projectors.
- **Customizable Content:** Display custom HTML, or load a url.
- **Cross-Platform Compatibility:** Works on Android and web platforms.
- **Simple Integration:** Easily integrate with Capacitor and your existing project.
- **Real-Time Updates:** Send real-time content updates to the external screen.

## Install

| Capacitor Version | Presentation Version |
| ----------------- | -------------------- |
| Capacitor v4      | 0.0.5                |
| Capacitor v6      | 0.1.x                |
| Capacitor v7      | 0.2.x                |

```bash
npm install presentation-capacitor
npx cap sync
```

### Example Video (Example App included in repo)

https://github.com/user-attachments/assets/a2dbb1f7-6075-4285-885d-39136bc90d9b

## Example

```typescript title=app.ts
import { Presentation } from "presentation-capacitor";

await Presentation.addListener("onMessage", console.log);
await Presentation.addListener("onSuccessLoadUrl", () => {
  Presentation.sendMessage({ data: "Ping" }).catch(console.error);
});
await Presentation.open({ type: "url", url: "/secondary_display" });
```

```typescript title=secondary_display.ts
window.onPresentationMessage = ({ data }) => {
  window.sendPresentationMessage({
    data: data === "Ping" ? "Pong" : "Unknown message",
  });
};
```

## API

<docgen-index>

* [`open(...)`](#open)
* [`sendMessage(...)`](#sendmessage)
* [`getDisplays()`](#getdisplays)
* [`terminate()`](#terminate)
* [`addListener('onSuccessLoadUrl', ...)`](#addlisteneronsuccessloadurl-)
* [`addListener('onFailLoadUrl', ...)`](#addlisteneronfailloadurl-)
* [`addListener('onMessage', ...)`](#addlisteneronmessage-)
* [`addListener('onClose', ...)`](#addlisteneronclose-)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### open(...)

```typescript
open(options: OpenOptions) => any
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#openoptions">OpenOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### sendMessage(...)

```typescript
sendMessage(message: { data: Record<string, unknown>; }) => any
```

| Param         | Type                        |
| ------------- | --------------------------- |
| **`message`** | <code>{ data: any; }</code> |

**Returns:** <code>any</code>

--------------------


### getDisplays()

```typescript
getDisplays() => any
```

**Returns:** <code>any</code>

--------------------


### terminate()

```typescript
terminate() => any
```

**Returns:** <code>any</code>

--------------------


### addListener('onSuccessLoadUrl', ...)

```typescript
addListener(eventName: "onSuccessLoadUrl", listenerFunc: () => void) => any
```

| Param              | Type                            | Description                                             |
| ------------------ | ------------------------------- | ------------------------------------------------------- |
| **`eventName`**    | <code>'onSuccessLoadUrl'</code> |                                                         |
| **`listenerFunc`** | <code>() =&gt; void</code>      | &lt;br&gt; Works only if type html of url or if browser |

**Returns:** <code>any</code>

--------------------


### addListener('onFailLoadUrl', ...)

```typescript
addListener(eventName: "onFailLoadUrl", listenerFunc: (error: string) => void) => any
```

| Param              | Type                                    |
| ------------------ | --------------------------------------- |
| **`eventName`**    | <code>'onFailLoadUrl'</code>            |
| **`listenerFunc`** | <code>(error: string) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener('onMessage', ...)

```typescript
addListener(eventName: "onMessage", listenerFunc: (message: { data: Record<string, unknown>; }) => void) => any
```

| Param              | Type                                              |
| ------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'onMessage'</code>                          |
| **`listenerFunc`** | <code>(message: { data: any; }) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener('onClose', ...)

```typescript
addListener(eventName: "onClose", listenerFunc: () => void) => any
```

| Param              | Type                       |
| ------------------ | -------------------------- |
| **`eventName`**    | <code>'onClose'</code>     |
| **`listenerFunc`** | <code>() =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |


### Type Aliases


#### OpenOptions

<code>{ displayId?: unknown } & ( 	| { 			type: "url"; 			url: string; 	 } 	| { 			type: "html"; 			html: string; 	 } )</code>


#### OpenResponse

<code>{ success: true } | { success: false; error: string }</code>


#### Display

<code>{ 	displayId: unknown; } & Partial&lt;<a href="#androiddisplaydetails">AndroidDisplayDetails</a>&gt;</code>


#### AndroidDisplayDetails

<code>{ 	name: string; 	isHdr?: boolean; 	rotation?: <a href="#androiddisplaydetailsrotation">AndroidDisplayDetailsRotation</a>; 	/** In pixels */ 	width?: number; 	/** In pixels */ 	height?: number; 	isDefaultDisplay?: boolean; 	cutout?: { 		safeInsetTop: number; 		safeInsetLeft: number; 		safeInsetBottom: number; 		safeInsetRight: number; 	}; 	productInfo?: { 		manufacturerPnpId: string; 		productUId: string; 		name?: string; 		modelYear?: number; 		manufactureYear?: number; 		manufactureWeek?: number; 	}; }</code>


### Enums


#### AndroidDisplayDetailsRotation

| Members             | Value            | Description |
| ------------------- | ---------------- | ----------- |
| **`None`**          | <code>0</code>   | In degrees  |
| **`SidewaysLeft`**  | <code>90</code>  | In degrees  |
| **`UpsideDown`**    | <code>180</code> | In degrees  |
| **`SidewaysRight`** | <code>270</code> | In degrees  |

</docgen-api>
