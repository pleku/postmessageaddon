## PostMessageAddon - an add-on for Vaadin Framework v7 for using html5's window.postMessage

[Read about window.postMessage](https://developer.mozilla.org/en-US/docs/DOM/window.postMessage),
[HTML5 standard specification](http://www.whatwg.org/specs/web-apps/current-work/multipage/web-messaging.html#web-messaging)

window.postMessage enables you to do cross-origin communication safely. This add-on has three server side components that enable your Vaadin application to do "Post Messaging" easily.

Currently the only possible message type is String, but support for [more types](http://www.whatwg.org/specs/web-apps/current-work/multipage/web-messaging.html#posting-messages) is planned.

PostMessageReceiver - for receiving and responding to messages, ie. when your application is embedded in another application inside an iframe or is opened in a popup window.

PostMessageIFrame - when you need to embed and communicate with another app or page. Extends BrowserFrame component from core Vaadin Framework.

PostMessageWindow - for opening a location in another browser window/tab and posting/receiving messages to/from them.


## License

The project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).


## Browser Compatibility

[html5 window.postMessage compatibility](http://caniuse.com/#feat=x-doc-messaging),
[Vaadin 7 supported browsers](http://vaadin.com/download/release/7.0/7.0.0/release-notes.html#supportedversions)


## Online Demo

http://pekka.app.fi/postmessage-demo/

## Relase notes 1.1.0

- refactored demos
- added support for posting & receiving messages to & from popup windows with PostMessageWindowExtension
- added support for PostMessageReceiver to send messages to window.opener


## TODO (1.1.0)

- Support other message types than String, like data objects File, Blob, FileList and ArrayBuffer.
- Investigate IE support for popup windows using a workaround
