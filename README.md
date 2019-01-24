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

### SimpleChat (Server)
* sendMessage(String): Sends message to all users when connected
* sendMessage(String, String): Sends message to specified user
* incomingMessage: Saves the message and displayes it on GUI
* addClient: Adds client to `users` and sends it to GUI. Also finds unique username
* renameClient: Renames a client
* removeClient: removes a client
* shutdownClient: Method for GUI to shutdown Client

### Controller (Server)
events are consumed to stop further execution of handlers.  
When modifying the ListView or other GUI elements you need to wrap in in 
`Platform.runLater()` to be on the GUI thread.



## Quellen
