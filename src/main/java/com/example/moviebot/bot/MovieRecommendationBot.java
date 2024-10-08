package com.example.moviebot.bot;

import com.example.moviebot.model.Movie;
import com.example.moviebot.model.Preference;
import com.example.moviebot.model.User;
import com.example.moviebot.model.WatchlistItem;
import com.example.moviebot.service.MovieService;
import com.example.moviebot.service.UserService;
import com.example.moviebot.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieRecommendationBot extends TelegramLongPollingBot {

    @Autowired
    private UserService userService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private WatchlistService watchlistService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    public MovieRecommendationBot() {
        setCommands();
    }

    private void setCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Start using the bot"));
        commands.add(new BotCommand("/help", "Get help information"));
        commands.add(new BotCommand("/about", "About the bot"));
        commands.add(new BotCommand("/addpreference", "Add a movie preference"));
        commands.add(new BotCommand("/showpreferences", "Show your preferences"));
        commands.add(new BotCommand("/deletepreference", "Delete a preference"));
        commands.add(new BotCommand("/findmovie", "Find a movie"));
        commands.add(new BotCommand("/randommovie", "Get a random movie"));
        commands.add(new BotCommand("/addtowatchlist", "Add a movie to watchlist"));
        commands.add(new BotCommand("/showwatchlist", "Show your watchlist"));
        commands.add(new BotCommand("/markwatched", "Mark a movie as watched"));
        commands.add(new BotCommand("/removefromwatchlist", "Remove from watchlist"));

        try {
            this.execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            if (messageText.startsWith("/")) {
                processCommand(chatId, username, messageText);
            } else {
                processNonCommandMessage(chatId, username, messageText);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String username = update.getCallbackQuery().getFrom().getUserName();

            processCallbackQuery(chatId, username, callbackData);
        }
    }


    private void processCommand(long chatId, String username, String command) {
        String[] parts = command.split("\\s+", 2);
        String action = parts[0].toLowerCase();

        switch (action) {
            case "/start":
                handleStart(chatId, username);
                break;
            case "/help":
                handleHelp(chatId);
                break;
            case "/about":
                handleAbout(chatId);
                break;
            case "/addpreference":
                handleAddPreference(chatId, parts.length > 1 ? parts[1] : "");
                break;
            case "/showpreferences":
                handleShowPreferences(chatId);
                break;
            case "/deletepreference":
                handleDeletePreference(chatId, parts.length > 1 ? parts[1] : "");
                break;
            case "/findmovie":
                handleFindMovie(chatId, parts.length > 1 ? parts[1] : "");
                break;
            case "/randommovie":
                handleRandomMovie(chatId);
                break;
            case "/addtowatchlist":
                handleAddToWatchlist(chatId, parts.length > 1 ? parts[1] : "");
                break;
            case "/showwatchlist":
                handleShowWatchlist(chatId);
                break;
            case "/markwatched":
                handleMarkWatched(chatId, parts.length > 1 ? parts[1] : "");
                break;
            case "/removefromwatchlist":
                handleRemoveFromWatchlist(chatId, parts.length > 1 ? parts[1] : "");
                break;
            default:
                sendMessage(chatId, "Unknown command. Use /help to see available commands.", getMainMenuKeyboard());
        }
    }

    private void processCallbackQuery(long chatId, String username, String callbackData) {
        String[] parts = callbackData.split(":");
        String action = parts[0];

        switch (action) {
            case "help":
                handleHelp(chatId);
                break;
            case "addpreference":
                promptForPreference(chatId);
                break;
            case "showpreferences":
                handleShowPreferences(chatId);
                break;
            case "findmovie":
                promptForMovieSearch(chatId);
                break;
            case "randommovie":
                handleRandomMovie(chatId);
                break;
            case "showwatchlist":
                handleShowWatchlist(chatId);
                break;
        }
    }


    private void promptForPreference(long chatId) {
        String message = "Please enter your movie preference in the following format:\n\n" +
                "genre,actor,releaseYear\n\n" +
                "For example: Action,Tom Cruise,2000";
        userService.setUserState(chatId, "ADDING_PREFERENCE")
                .then(Mono.fromRunnable(() -> sendMessage(chatId, message, null)))
                .subscribe();
    }

    private void promptForMovieSearch(long chatId) {
        String message = "Please enter your search criteria in the following format:\n\n" +
                "genre,actor,releaseYear,keyword\n\n" +
                "For example: Action,Brad Pitt,2010,war\n\n" +
                "You can use 'skip' for any criteria you want to ignore.\n" +
                "For example: Action,skip,2010,war";
        userService.setUserState(chatId, "SEARCHING_MOVIE")
                .then(Mono.fromRunnable(() -> sendMessage(chatId, message, null)))
                .subscribe();
    }

    private void processNonCommandMessage(long chatId, String username, String message) {
        userService.getUserState(chatId)
                .flatMap(state -> {
                    switch (state) {
                        case "ADDING_PREFERENCE":
                            return handleAddPreference(chatId, message)
                                    .then(userService.setUserState(chatId, "NORMAL"));
                        case "SEARCHING_MOVIE":
                            return handleFindMovie(chatId, message)
                                    .then(userService.setUserState(chatId, "NORMAL"));
                        default:
                            return Mono.fromRunnable(() ->
                                    sendMessage(chatId, "I'm not sure what you mean. Please use a command or one of the buttons below.", getMainMenuKeyboard())
                            );
                    }
                })
                .onErrorResume(error -> {
                    error.printStackTrace();
                    return Mono.fromRunnable(() ->
                            sendMessage(chatId, "An error occurred. Please try again.", getMainMenuKeyboard())
                    );
                })
                .subscribe();
    }



    private void handleStart(long chatId, String username) {
        userService.findOrCreateUser(chatId, username)
                .subscribe(user -> {
                    String welcomeMessage = "Welcome to Movie Recommendation Bot, " + username + "! 🎬\n\n" +
                            "Here you can discover new movies, manage your preferences, and keep track of your watchlist.\n\n" +
                            "Use /help to see available commands or use the buttons below for quick access.";
                    sendMessage(chatId, welcomeMessage, getMainMenuKeyboard());
                });
    }

    private void handleHelp(long chatId) {
        String helpText = "*Available commands:*\n\n" +
                "🚀 /start - Start using the bot\n" +
                "❓ /help - Get help information\n" +
                "ℹ️ /about - About the bot\n" +
                "➕ /addpreference - Add a movie preference\n" +
                "  Example: `/addpreference Action,Tom Cruise,2000`\n" +
                "👁️ /showpreferences - Show your preferences\n" +
                "🗑️ /deletepreference - Delete a preference\n" +
                "  Example: `/deletepreference 1`\n" +
                "🔍 /findmovie - Find a movie\n" +
                "  Example: `/findmovie Action,Brad Pitt,2010,war`\n" +
                "  You can use 'skip' for any criteria to ignore it.\n" +
                "  Example: `/findmovie Action,skip,2010,war`\n" +
                "  If criteria are not set, your preferences will be used.\n" +
                "🎲 /randommovie - Get a random movie\n" +
                "📝 /addtowatchlist - Add a movie to watchlist\n" +
                "  Example: `/addtowatchlist movieId`\n" +
                "📋 /showwatchlist - Show your watchlist\n" +
                "✅ /markwatched - Mark a movie as watched\n" +
                "  Example: `/markwatched watchlistItemId`\n" +
                "❌ /removefromwatchlist - Remove from watchlist\n" +
                "  Example: `/removefromwatchlist watchlistItemId`\n\n" +
                "Use the buttons below for quick access to common actions.";

        sendMessage(chatId, helpText, getMainMenuKeyboard());
    }

    private void handleAbout(long chatId) {
        String aboutText = "*Movie Recommendation Bot* 🎥\n\n" +
                "This bot helps you discover movies, manage your preferences, and keep track of your watchlist. " +
                "It uses advanced algorithms to recommend movies based on your preferences and viewing history.\n\n" +
                "*Features:*\n" +
                "✅ Personalized movie recommendations\n" +
                "✅ Manage your movie preferences\n" +
                "✅ Search for movies by various criteria\n" +
                "✅ Maintain a watchlist\n" +
                "✅ Track watched movies\n\n" +
                "*Version:* 1.0";

        sendMessage(chatId, aboutText, null);
    }

    private Mono<Void> handleAddPreference(long chatId, String preferenceData) {
        String[] parts = preferenceData.split(",");
        if (parts.length != 3) {
            return Mono.fromRunnable(() ->
                    sendMessage(chatId, "Invalid format. Use: genre,actor,releaseYear", null)
            );
        }

        Preference preference = new Preference();
        preference.setGenre(parts[0].trim());
        preference.setActor(parts[1].trim());
        try {
            preference.setReleaseYear(Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException e) {
            return Mono.fromRunnable(() ->
                    sendMessage(chatId, "Invalid release year. Please provide a valid number.", null)
            );
        }

        return userService.addPreference(chatId, preference)
                .then(Mono.fromRunnable(() ->
                        sendMessage(chatId, "Preference added successfully!", getMainMenuKeyboard())
                ));
    }


    private void handleShowPreferences(long chatId) {
        userService.findOrCreateUser(chatId, null)
                .flatMap(user -> {
                    if (user.getPreferences().isEmpty()) {
                        return Mono.fromRunnable(() ->
                                sendMessage(chatId, "You haven't set any preferences yet. Use /addpreference to add some.", null)
                        );
                    } else {
                        StringBuilder sb = new StringBuilder("*Your preferences:*\n\n");
                        for (int i = 0; i < user.getPreferences().size(); i++) {
                            Preference pref = user.getPreferences().get(i);
                            sb.append(i + 1).append(". ")
                                    .append("Genre: ").append(pref.getGenre())
                                    .append(", Actor: ").append(pref.getActor())
                                    .append(", Year: ").append(pref.getReleaseYear())
                                    .append("\n");
                        }
                        return Mono.fromRunnable(() -> sendMessage(chatId, sb.toString(), null));
                    }
                })
                .subscribe();
    }

    private void handleDeletePreference(long chatId, String indexStr) {
        try {
            int index = Integer.parseInt(indexStr) - 1;
            userService.deletePreference(chatId, index)
                    .subscribe(user -> sendMessage(chatId, "Preference deleted successfully.", null));
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Invalid index. Please provide a number.", null);
        }
    }


    private Mono<Void> handleFindMovie(long chatId, String query) {
        String[] parts = query.split(",");
        String genre = parts.length > 0 ? parts[0].trim() : null;
        String actor = parts.length > 1 ? parts[1].trim() : null;
        Integer year = null;
        if (parts.length > 2 && !parts[2].trim().equalsIgnoreCase("skip")) {
            try {
                year = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException e) {
                // Ignore invalid year
            }
        }
        String keyword = parts.length > 3 ? parts[3].trim() : null;

        return movieService.findMovies(chatId, genre, actor, year, keyword)
                .take(5)
                .map(this::formatMovieInfo)
                .collectList()
                .flatMap(movies -> {
                    if (movies.isEmpty()) {
                        return Mono.fromRunnable(() ->
                                sendMessage(chatId, "No movies found matching your criteria or preferences.", null)
                        );
                    } else {
                        return Mono.fromRunnable(() ->
                                sendMessage(chatId, "*Found movies:*\n\n" + String.join("\n\n", movies), null)
                        );
                    }
                })
                .then();
    }

    private void handleRandomMovie(long chatId) {
        movieService.getRandomMovie()
                .subscribe(movie -> sendMessage(chatId, "*Random movie recommendation:*\n\n" + formatMovieInfo(movie), null));
    }

    private void handleAddToWatchlist(long chatId, String movieId) {
        if (movieId.isEmpty()) {
            sendMessage(chatId, "Please provide a movie ID. Example: /addtowatchlist movieId", null);
            return;
        }

        userService.findOrCreateUser(chatId, null)
                .flatMap(user -> watchlistService.addToWatchlist(user.getId(), movieId))
                .subscribe(item -> sendMessage(chatId, "Movie added to your watchlist.", null));
    }

    private void handleShowWatchlist(long chatId) {
        userService.findOrCreateUser(chatId, null)
                .flatMap(user -> watchlistService.getWatchlist(user.getId())
                        .flatMap(item -> movieService.getMovieById(item.getMovieId())
                                .map(movie -> formatWatchlistItem(item, movie)))
                        .collectList())
                .subscribe(watchlist -> {
                    if (watchlist.isEmpty()) {
                        sendMessage(chatId, "Your watchlist is empty. Use /addtowatchlist to add movies.", null);
                    } else {
                        sendMessage(chatId, "*Your watchlist:*\n\n" + String.join("\n\n", watchlist), null);
                    }
                });
    }

    private void handleMarkWatched(long chatId, String itemId) {
        if (itemId.isEmpty()) {
            sendMessage(chatId, "Please provide a watchlist item ID. Example: /markwatched itemId", null);
            return;
        }

        watchlistService.markAsWatched(itemId)
                .subscribe(item -> sendMessage(chatId, "Movie marked as watched.", null));
    }

    private void handleRemoveFromWatchlist(long chatId, String itemId) {
        if (itemId.isEmpty()) {
            sendMessage(chatId, "Please provide a watchlist item ID. Example: /removefromwatchlist itemId", null);
            return;
        }

        watchlistService.removeFromWatchlist(itemId)
                .subscribe(v -> sendMessage(chatId, "Movie removed from your watchlist.", null));
    }

    private String formatMovieInfo(Movie movie) {
        return String.format("🎬 *%s*\n" +
                        "🎭 Genre: %s\n" +
                        "📅 Year: %d\n" +
                        "🌟 Actors: %s\n" +
                        "📝 Description: _%s_\n" +
                        "🆔 ID: `%s`",
                movie.getTitle(), movie.getGenre(), movie.getReleaseYear(),
                String.join(", ", movie.getActors()), movie.getDescription(),
                movie.getId());
    }

    private String formatWatchlistItem(WatchlistItem item, Movie movie) {
        return String.format("%s\n" +
                        "👀 Status: %s\n" +
                        "🆔 Watchlist ID: `%s`",
                formatMovieInfo(movie),
                item.isWatched() ? "Watched ✅" : "Not watched ⏳",
                item.getId());
    }

    private void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("Markdown");
        if (replyMarkup != null) {
            message.setReplyMarkup(replyMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getMainMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();

        InlineKeyboardButton addPreferenceButton = new InlineKeyboardButton();
        addPreferenceButton.setText("Add Preference");
        addPreferenceButton.setCallbackData("addpreference");
        rowInline1.add(addPreferenceButton);

        InlineKeyboardButton showPreferencesButton = new InlineKeyboardButton();
        showPreferencesButton.setText("Show Preferences");
        showPreferencesButton.setCallbackData("showpreferences");
        rowInline1.add(showPreferencesButton);

        InlineKeyboardButton findMovieButton = new InlineKeyboardButton();
        findMovieButton.setText("Find Movie");
        findMovieButton.setCallbackData("findmovie");
        rowInline2.add(findMovieButton);

        InlineKeyboardButton randomMovieButton = new InlineKeyboardButton();
        randomMovieButton.setText("Random Movie");
        randomMovieButton.setCallbackData("randommovie");
        rowInline2.add(randomMovieButton);

        InlineKeyboardButton showWatchlistButton = new InlineKeyboardButton();
        showWatchlistButton.setText("Show Watchlist");
        showWatchlistButton.setCallbackData("showwatchlist");
        rowInline3.add(showWatchlistButton);

        InlineKeyboardButton helpButton = new InlineKeyboardButton();
        helpButton.setText("Help");
        helpButton.setCallbackData("help");
        rowInline3.add(helpButton);

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}