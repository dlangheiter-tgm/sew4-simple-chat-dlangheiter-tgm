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

## Quellen
