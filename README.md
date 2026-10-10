# rag

[![Java](https://img.shields.io/badge/Java-25-orange)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen)](https://docs.spring.io/spring-boot/3.5/reference/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.4-blue)](https://docs.spring.io/spring-ai/reference/index.html)
[![Maven](https://img.shields.io/badge/Maven-3.9.9-C71A36)](https://maven.apache.org/)
[![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-lightgrey)](pom.xml)
[![License](https://img.shields.io/badge/license-not%20declared-red)](#license)

An example Retrieval-Augmented Generation (RAG) service. Upload a PDF, store its chunks in PostgreSQL with PGvector, and ask questions that are answered from the retrieved passages.

## Table of contents

- [Project name](#project-name)
- [Project description](#project-description)
- [Tech stack](#tech-stack)
- [Getting started locally](#getting-started-locally)
- [Available scripts](#available-scripts)
- [Project scope](#project-scope)
- [Project status](#project-status)
- [License](#license)

## Project name

**rag** (`pl.mojezapiski:rag:0.0.1-SNAPSHOT`)

Packaged as a WAR. The main class is `pl.mojezapiski.rag.RagApplication`; `ServletInitializer` allows deployment to an external servlet container.

Repository: [https://github.com/beowoolf/rag](https://github.com/beowoolf/rag)

## Project description

The application reads uploaded PDF files page by page, splits the text into token-sized chunks, embeds them, and stores them in a PGvector table. A chat endpoint retrieves the four most similar chunks for a question, injects them into a system prompt together with the most recent conversation, and asks an OpenAI chat model for an answer.

- Uploaded document chunks are persisted in PostgreSQL.
- Chat history is kept in memory and is lost when the process restarts.
- The system prompt and answers are in Polish; the code and this README are in English.

Further reading:

- [Spring AI reference](https://docs.spring.io/spring-ai/reference/index.html)
- [OpenAI chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [PGvector store](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
- [Spring Boot 3.5 reference](https://docs.spring.io/spring-boot/3.5/reference/)
- [HELP.md](HELP.md): Spring Initializr notes. Its links point at Spring Boot 3.3.4; this project uses 3.5.16.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 3.5.16 |
| AI | Spring AI 1.1.4 (BOM-managed) |
| Chat model | OpenAI `gpt-4.1-nano` (`spring-ai-openai-spring-boot-starter`) |
| Embeddings | OpenAI embedding model via the same starter (Spring AI default) |
| HTTP | Spring Web (MVC) and Spring WebFlux |
| Documents | `spring-ai-pdf-document-reader` (`PagePdfDocumentReader`, `TokenTextSplitter`) |
| Vector store | PGvector on PostgreSQL 16 (`pgvector/pgvector:pg16`) |
| Local infrastructure | Docker Compose (`compose.yaml` and `docker-compose.yml`) |
| Boilerplate | Lombok, Spring Boot configuration processor |
| Build | Apache Maven 3.9.9 via the Maven Wrapper |
| Packaging | WAR; embedded Tomcat is `provided` |
| Tests | Spring Boot Test, Reactor Test, JUnit 5 (context load) |

## Getting started locally

### Prerequisites

- JDK 25
- Docker with the Docker Compose plugin
- An OpenAI API key

Maven does not need to be installed. The wrapper (`mvnw` / `mvnw.cmd`) downloads Maven 3.9.9 on first use.

### Configuration

Runtime settings live in `src/main/resources/application.properties`:

| Property | Value |
| --- | --- |
| `server.port` | `8888` |
| `spring.application.name` | `rag` |
| `spring.ai.openai.api-key` | `${OPENAI_API_KEY:set_env_var}` |
| `spring.ai.openai.chat.options.model` | `gpt-4.1-nano` |
| `spring.ai.vectorstore.pgvector.initialize-schema` | `true` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/mydatabase` |
| `spring.datasource.username` | `myuser` |
| `spring.datasource.password` | `secret` |
| `spring.servlet.multipart.max-file-size` | `5MB` |
| `spring.servlet.multipart.max-request-size` | `5MB` |

Set the API key in your shell before starting the application. `set_env_var` is only a placeholder and will not work against the OpenAI API.

**Windows (PowerShell)**

```powershell
$env:OPENAI_API_KEY = "your-key"
```

**Linux / macOS**

```bash
export OPENAI_API_KEY=your-key
```

The database credentials above are development defaults. They are committed to the repository, so change them before using this setup anywhere else.

### Running locally (application on the host, database in Docker)

`compose.yaml` defines a `pgvector` service that uses the same database name, user, and password as `application.properties`. It is labelled `org.springframework.boot.service-connection=postgres`, and `spring-boot-docker-compose` is on the runtime classpath, so Spring Boot starts it automatically when Docker is running.

From the repository root:

**Windows**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**

```bash
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8888`.

If you prefer to start the database yourself:

```bash
docker compose -f compose.yaml up -d
```

`compose.yaml` publishes port `5432` with a random host port. Spring Boot reads the mapped port automatically; for manual connections, check it with `docker compose -f compose.yaml port pgvector 5432`.

### Running the full stack in Docker

`docker-compose.yml` builds the application image (`Dockerfile`, multi-stage: JDK 25 build, JRE 25 runtime) and starts it next to PGvector. The application listens on port `8888`, and PostgreSQL is published on `5432`.

```bash
export OPENAI_API_KEY=your-key   # PowerShell: $env:OPENAI_API_KEY = "your-key"
docker compose -f docker-compose.yml up --build
```

Use `-f docker-compose.yml` explicitly. When both files exist, Docker Compose prefers `compose.yaml`, which only contains the database, so a bare `docker compose up` would not start the application.

Do not run the full stack and `spring-boot:run` at the same time: both bind host port `8888`, and the full stack also binds `5432`. Stop one before starting the other.

### Building a WAR

```powershell
.\mvnw.cmd clean package
```

```bash
./mvnw clean package
```

The artifact is `target/rag-0.0.1-SNAPSHOT.war`. It can be deployed to an external servlet container through `ServletInitializer`, or run with `java -jar target/rag-0.0.1-SNAPSHOT.war`.

### Try the API

Upload a PDF. Only files with content type `application/pdf` are indexed; other files are ignored. The call returns `204 No Content` in both cases.

```bash
curl -X POST http://localhost:8888/api/ai/upload -F "file=@document.pdf"
```

Ask a question. The body is a JSON object with a `content` field. The response is the full transcript, including the new answer.

```bash
curl -X POST http://localhost:8888/api/ai/chat/messages \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"What does the document say?\"}"
```

Read the transcript without asking a new question:

```bash
curl http://localhost:8888/api/ai/chat/messages
```

Each message looks like this:

```json
{
  "content": "What does the document say?",
  "type": "USER",
  "dateTime": "2026-09-27T19:00:00"
}
```

`type` is `USER` for questions and `SYSTEM` for model answers.

## Available scripts

There is no task runner besides Maven. Use the wrapper so the Maven version matches `.mvn/wrapper/maven-wrapper.properties`.

| Command (Windows) | Command (Linux / macOS) | Purpose |
| --- | --- | --- |
| `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` | Start the application on port 8888; starts `compose.yaml` services if Docker is running |
| `.\mvnw.cmd test` | `./mvnw test` | Run tests (`RagApplicationTests` loads the Spring context; needs the database to be reachable) |
| `.\mvnw.cmd clean package` | `./mvnw clean package` | Build `target/rag-0.0.1-SNAPSHOT.war` |
| `.\mvnw.cmd clean` | `./mvnw clean` | Remove build output |
| `docker compose -f docker-compose.yml up --build` | same | Build the image and run the application with PGvector |
| `docker compose -f compose.yaml up -d` | same | Start only PGvector |
| `docker compose -f docker-compose.yml down` | same | Stop the containers (add `-v` to also delete the `pgvector-data` volume) |

## Project scope

The code is organised into three packages, each exposing a facade interface to the others: `file`, `document`, and `chat`.

### PDF upload (`file` package)

`POST /api/ai/upload` accepts a multipart field named `file`.

- Content type must be `application/pdf`. Other types are skipped and the endpoint still returns `204`.
- `PagePdfDocumentReader` creates one document per page, with a top margin of `0` and no bottom lines removed.
- `TokenTextSplitter` splits the extracted text into chunks.
- Chunks are added to the PGvector store, which creates its schema on startup (`initialize-schema=true`).
- Request size is capped at 5 MB.

### Document retrieval (`document` package)

`DocumentService` runs a similarity search over the vector store for the user's question, returning the `topK` = `4` closest chunks.

### Chat (`chat` package)

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/api/ai/chat/messages` | Returns `{ "messages": [ ... ] }` |
| `POST` | `/api/ai/chat/messages` | Accepts `{ "content": "..." }`, answers it, and returns the full transcript |

Each question goes through these steps:

1. Retrieve the four most similar chunks for the question.
2. Build a system prompt that asks the model to answer briefly from the `<dokumentacja>` block and to decline when that block has no relevant data.
3. Add the last six stored messages inside a `<messages>` block.
4. Call `OpenAiChatModel` (`gpt-4.1-nano`).
5. Store the user message and the model reply (`SYSTEM` type) with timestamps.

The transcript is an in-memory list in `ChatService`. It is not written to the database.

## Project status

Version `0.0.1-SNAPSHOT`. This is an example application, not a production service.

Known limitations:

- **No authentication or authorization.** Any client that can reach port 8888 can upload documents and read the chat transcript.
- **Hard-coded database credentials** in `application.properties`, `compose.yaml`, and `docker-compose.yml`.
- **Chat history is process-local.** It is not shared between instances and is lost on restart. The list is a plain `ArrayList` and is not thread-safe.
- **Non-PDF uploads return `204`** and are silently ignored. The content type is taken from the client, so it is not verified against the file's contents.
- **No `.dockerignore` file.** The Docker build context includes the whole working tree, including any local `target/` directory.
- **`mvnw` is not committed as executable** (git mode `100644`). The Docker build runs `./mvnw` on Linux and will likely fail with "permission denied" until the file is made executable (`git update-index --chmod=+x mvnw`). This has not been verified by running the build.
- **Testing:** the only automated test is a `@SpringBootTest` that checks the application context loads. It needs a reachable PostgreSQL instance.
- **No CI workflow** is configured in this repository, so there is no build badge.
- The POM leaves `url`, `licenses`, `developers`, and `scm` empty. These empty blocks are intentional overrides so Maven does not inherit them from the Spring Boot parent (see [HELP.md](HELP.md)).

## License

No license is declared. There is no `LICENSE` file, and `pom.xml` has an empty `<license>` element. Until a license is added, the default copyright rules apply and others may not copy, modify, or distribute the code.
