# Billiards Scraper

A Spring Boot application for scraping and managing billiards/pool statistics.

## Features

- Player statistics tracking
- Tournament management
- Team and club information
- Match history and results
- RESTful API endpoints
- Swagger/OpenAPI documentation

## Prerequisites

- Java 8 or higher
- Maven 3.6+
- PostgreSQL database

## Database Setup

Create a PostgreSQL database named `poolstats`:

```bash
createdb poolstats
```

Configure your database connection in `src/main/resources/application.properties`.

## Running the Application

### Using Maven

```bash
./mvnw spring-boot:run
```

### Using IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Wait for Maven to download dependencies
3. Run the `BilijarScraperApplication` class

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the Swagger UI at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v2/api-docs

### Importing to Postman

1. Open Postman
2. Click **Import**
3. Select **Link** tab
4. Enter: `http://localhost:8080/v2/api-docs`
5. Click **Continue** and **Import**

## Endpoints

### Sync Controller
- `GET /sync/players` - Sync player data
- `GET /sync/tournaments` - Sync tournament data
- `GET /sync/teams` - Sync team data
- `GET /sync/clubs` - Sync club data
- `GET /sync/matches` - Sync match data

## Tech Stack

- **Spring Boot 2.4.0**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL**
- **Hibernate**
- **Springfox (Swagger)**
- **HtmlUnit** (for web scraping)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/poolstats/billiardsscraper/
│   │       ├── common/
│   │       │   ├── controller/    # REST Controllers
│   │       │   ├── entity/        # JPA Entities
│   │       │   ├── repo/          # JPA Repositories
│   │       │   └── service/       # Business Logic
│   │       └── config/            # Configuration Classes
│   └── resources/
│       └── application.properties # Configuration
└── test/
    └── java/
        └── com/poolstats/billiardsscraper/
```

## License

[Add your license here]
