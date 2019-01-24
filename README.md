# "Java GUI & Socket Programmierung - Simple Chat"

## Aufgabenstellung
Die detaillierte [Aufgabenstellung](TASK.md) beschreibt die notwendigen Schritte zur Realisierung.

## Implementierung


### ClientWorker 
* ClientWorker: create Input/Output stream. Don't forget autoFlush: true
* run: while listening reads line from input stream and checks if it is 
    a cmd and executed else it will be given to `SimpleChatServer`
* shutdown: Send !STOP over socket and closes all resources.
* send: out.println, if Exception shutdown ClientWorker

### SimpleChatServer
* run: Initialites `ServerSocket` and start accepting new clients.
* recieved: Get the message from `ClientWorker` and adds the username in
    front of it. Notify `SimpleChat` about it and send it to the other
    clients.
* send(String message): Sends the supplied message to all clients.
* send(String message, String receiver): Sends a message to a specific
    `ClientWorker` if it was found by name
* getWorker: Finds an `ClientWorker` by ChatName and returns it.
* setName: Sets an name for a `ClientWorker`
* removeClient(ClientWorker): Shutdowns and removes client
* removeClient(String): remove client by ChatName
* shutdown: Shutdown all clients an closes thread.

## Quellen
