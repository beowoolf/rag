# rag

[![Java](https://img.shields.io/badge/Java-25-orange)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen)](https://docs.spring.io/spring-boot/3.5/reference/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.4-blue)](https://docs.spring.io/spring-ai/reference/index.html)
[![Maven](https://img.shields.io/badge/Maven-3.9.9-C71A36)](https://maven.apache.org/)
[![Version](https://img.shields.io/badge/version-0.0.1--SNAPSHOT-lightgrey)](pom.xml)

Example Retrieval-Augmented Generation (RAG) service. Upload a PDF, store its chunks in PostgreSQL with PGvector, and ask questions that are answered from the retrieved passages.

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

Packaged as a WAR. The main class is `pl.mojezapiski.rag.RagApplication`. `ServletInitializer` allows deployment to an external servlet container.

Repository: [https://github.com/beowoolf/rag](https://github.com/beowoolf/rag)

## Project description

The application loads PDF files, splits each page into token-sized chunks, and writes those chunks to a PGvector store. A chat endpoint retrieves the four closest chunks for a question, injects them into a system prompt together with the recent conversation, and calls an OpenAI chat model.

Uploaded documents stay in PostgreSQL. Chat history is kept in memory for the life of the process.

Further reading:

- [Spring AI reference](https://docs.spring.io/spring-ai/reference/index.html)
- [OpenAI chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [PGvector store](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
- [Spring Boot 3.5 reference](https://docs.spring.io/spring-boot/3.5/reference/)
- [HELP.md](HELP.md) — Spring Initializr notes. Its guide links still point at Spring Boot 3.3.4; this project uses 3.5.16.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 3.5.16 |
| AI | Spring AI 1.1.4 |
| Chat model | OpenAI `gpt-4.1-nano` (`spring-ai-openai-spring-boot-starter`) |
| HTTP | Spring Web (MVC) and Spring WebFlux |
| Documents | `spring-ai-pdf-document-reader` (`PagePdfDocumentReader`, `TokenTextSplitter`) |
| Vector store | PGvector on PostgreSQL 16 (`pgvector/pgvector:pg16`) |
| Local database | Docker Compose, started by Spring Boot Docker Compose support |
| Boilerplate | Lombok, Spring Boot configuration processor |
| Build | Apache Maven 3.9.9 via the Maven Wrapper |
| Packaging | WAR; embedded Tomcat is `provided` |
| Tests | Spring Boot Test, Reactor Test, JUnit (context load) |

## Getting started locally

### Prerequisites

- JDK 25
- Docker, with the Docker Compose plugin, so the PGvector container can start
- An OpenAI API key

Maven does not need to be installed. The wrapper (`mvnw` / `mvnw.cmd`) downloads Maven 3.9.9.

### Configuration

Runtime settings live in `src/main/resources/application.properties`.

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

`compose.yaml` defines a `pgvector` service with the same database name, user, and password, and labels it for Spring Boot service connections (`org.springframework.boot.service-connection=postgres`). `spring-boot-docker-compose` and `spring-ai-spring-boot-docker-compose` are optional runtime dependencies, so a local run starts that Compose file when Docker is available.

Set the API key before starting the application. The default `set_env_var` is only a placeholder.

**Windows (PowerShell)**

```powershell
$env:OPENAI_API_KEY = "your-key"
```

**Linux / macOS**

```bash
export OPENAI_API_KEY=your-key
```

### Run

From the repository root, with Docker running:

**Windows**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**

```bash
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8888`.

To build a deployable WAR instead:

```powershell
.\mvnw.cmd clean package
```

The artifact is `target/rag-0.0.1-SNAPSHOT.war`.

### Try the API

Upload a PDF (only `application/pdf` is indexed; other types are ignored). The call returns `204 No Content`.

```bash
curl -X POST http://localhost:8888/api/ai/upload -F "file=@document.pdf"
```

Ask a question. The body is a JSON object with a `content` field.

```bash
curl -X POST http://localhost:8888/api/ai/chat/messages \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"What does the document say?\"}"
```

Read the in-memory transcript:

```bash
curl http://localhost:8888/api/ai/chat/messages
```

A message looks like this:

```json
{
  "content": "What does the document say?",
  "type": "USER",
  "dateTime": "2026-09-27T19:00:00"
}
```

`type` is `USER` or `SYSTEM`.

## Available scripts

There is no task runner besides Maven. Use the wrapper so the Maven version matches `.mvn/wrapper/maven-wrapper.properties`.

| Command (Windows) | Command (Linux / macOS) | Purpose |
| --- | --- | --- |
| `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` | Start the application on port 8888 and bring up Compose |
| `.\mvnw.cmd test` | `./mvnw test` | Run tests (`RagApplicationTests` loads the Spring context) |
| `.\mvnw.cmd clean package` | `./mvnw clean package` | Build `target/rag-0.0.1-SNAPSHOT.war` |
| `.\mvnw.cmd clean` | `./mvnw clean` | Remove build output |

Start PGvector on its own when you do not want the application to launch Compose:

```bash
docker compose up -d
```

## Project scope

The code is split into three packages behind facades: `file`, `document`, and `chat`.

### PDF upload

`POST /api/ai/upload` accepts a multipart field named `file`.

- Content type must be `application/pdf`. Anything else is skipped and the endpoint still returns `204`.
- `PagePdfDocumentReader` reads one page per document, with a top margin of `0` and no bottom lines removed.
- `TokenTextSplitter` chunks the extracted text.
- Chunks are added to the PGvector store. The schema is created on startup (`initialize-schema=true`).
- Request size is capped at 5 MB.

### Similarity search

`DocumentService` searches the vector store with the user question and `topK` of `4`.

### Chat

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/api/ai/chat/messages` | Returns `{ "messages": [ ... ] }` |
| `POST` | `/api/ai/chat/messages` | Accepts `{ "content": "..." }`, answers, then returns the full transcript |

Each turn:

1. Retrieves similar chunks for the question.
2. Builds a system prompt that tells the model to answer briefly from a `<dokumentacja>` block and to skip the question when that block has no relevant data. The prompt text is in Polish.
3. Adds the last six stored messages inside a `<messages>` block.
4. Calls `OpenAiChatModel` (`gpt-4.1-nano`).
5. Appends the user message and the model reply (`MessageType.SYSTEM`) with a timestamp.

The transcript is an in-memory list on the chat service. It is not written to the database and is lost on restart.

## Project status

`0.0.1-SNAPSHOT` — an example application, not a production service.

- Automated coverage is a single `@SpringBootTest` that checks the application context loads.
- There is no CI workflow in this repository.
- Chat history is process-local and not shared across instances.
- Non-PDF uploads succeed with `204` and do not report that the file was ignored.
- The POM leaves `url`, `licenses`, `developers`, and `scm` empty. Those empty license and developer blocks are intentional overrides so Maven does not inherit them from the Spring Boot parent (see [HELP.md](HELP.md)).

## License

No license is declared. There is no `LICENSE` file, and `pom.xml` contains an empty `<license>` element. All rights remain with the copyright holder until a license is added.
