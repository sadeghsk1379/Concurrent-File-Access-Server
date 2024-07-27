# Concurrent File Access Server

This project implements a multi-threaded server in Java that handles client connections, reads from, and writes to a file concurrently using thread pools and read-write locks.

## Features

- **Multi-threaded Handling**: Utilizes a thread pool to handle multiple client connections concurrently.
- **Concurrent File Access**: Implements read-write locks to ensure safe concurrent access to a file.
- **Client Communication**: Sends a welcome message to clients and processes client messages.
- **File Operations**: Writes client messages to a file and reads the file content to send back to clients.

## Main Components

- **Thread Pool**: A fixed thread pool is created to manage concurrent client connections efficiently.
- **ReadWriteLock**: A read-write lock is used to synchronize access to the file, ensuring thread safety during read and write operations.
- **ServerSocket**: Listens for incoming client connections on a specified port.
- **ClientHandler**: Handles the interaction with each client in a separate thread, including sending a welcome message, reading client messages, writing to a file, and reading from a file.

## Example Client Interaction

1. Client connects to the server.
2. Server sends a welcome message to the client.
3. Client sends a message to the server.
4. Server writes the client's message to a file.
5. Server reads the file content and sends it back to the client.
