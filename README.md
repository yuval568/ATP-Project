# 🧩 Interactive Multi-Threaded Maze Engine & Solver

[![Java 15](https://img.shields.io/badge/Java-15-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/) [![JavaFX 16](https://img.shields.io/badge/JavaFX-16-007396?style=flat-square&logo=java&logoColor=white)](https://openjfx.io/) [![MVVM & Sockets](https://img.shields.io/badge/Architecture-MVVM%20%7C%20Sockets-6f42c1?style=flat-square)](#) [![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

- 🌐 **Client-Server Architecture:** Multi-threaded TCP servers handling remote maze generation and pathfinding asynchronously via `ExecutorService` thread pools.
- 💾 **Smart Solution Caching:** Persists and compresses computed maze solutions to disk, serving instant cached results for recurring queries.
- 🎨 **Design Patterns & MVVM:** Built with Strategy (BFS/DFS/Best-First), Decorator (custom stream compression), Adapter, and Singleton patterns using JavaFX.
- 🛠️ **Tech Stack:** Java 15, JavaFX 16, Maven, JUnit 5, and Log4j2.
