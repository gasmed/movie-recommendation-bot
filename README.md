# Movie Recommendation Bot

This is a Telegram bot that provides movie recommendations and manages user watchlists.

## Features

- User registration and sign-in
- Manage user preferences (genre, actors, release year)
- Find movies based on user preferences or custom criteria
- Get random movie recommendations
- Manage watchlist (add, remove, mark as watched)

## Prerequisites

- Java 21
- Maven
- Docker and Docker Compose
- MongoDB

## Building and Running

1. Clone the repository:

```bash
git clone https://github.com/NezdanchikG/movie-recommendation-bot.git
cd movie-recommendation-bot
```

2. Build the project:

```bash
mvn clean package
```

3. Start the application using Docker Compose:

```bash
docker-compose up --build
```

4. The bot should now be running and ready to accept commands on Telegram.

## DockerHub URL
1. Dowbload images:
```bash
docker pull nezdanchikg/moviebot-mongodb-1
docker pull nezdanchikg/moviebot-movie-bot-1
```

2. Run containers:
```bash
docker run -d --name moviebot-mongodb-1 nezdanchikg/moviebot-mongodb-1
docker run -d --name moviebot-movie-bot-1 nezdanchikg/moviebot-movie-bot-1
```

## Adding movies to the database

1. Open a running container with mongodb and go to the "exec" tab

2. Write these commands in order to get to the right place
```bash
mongosh
```
```bash
use moviebot
```

3. The last point is to paste the entire command from the file "insert_films.js" into the console
## Usage

Start a chat with the bot on Telegram and use the following commands:

- `/start`: Register or sign in
- `/addpreference genre,actor,releaseYear`: Add a preference
- `/showpreferences`: Show your preferences
- `/deletepreference index`: Delete a preference
- `/findmovie genre,actor,year,keyword`: Find movies matching criteria
- `/randommovie`: Get a random movie recommendation
- `/addtowatchlist movieId`: Add a movie to your watchlist
- `/showwatchlist`: Show your watchlist
- `/markwatched itemId`: Mark a movie as watched
- `/removefromwatchlist itemId`: Remove a movie from your watchlist

## Configuration

Update the `application.properties` file to change MongoDB connection settings if needed.

Make sure to set your Telegram Bot Token in the `MovieRecommendationBot` class.


