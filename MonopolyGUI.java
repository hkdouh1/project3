import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import java.util.*;
import java.io.*;
import javafx.scene.text.FontPosture;

public class MonopolyGUI extends Application {
    private Stage primaryStage;
    private Board board;
    private Random random;
    private int currentPlayerIndex;
    private int doublesCount;
    private HashMap<String, ArrayList<Integer>> colorGroups;
    private Font customFont;

    private Label currentPlayerLabel;
    private Label moneyLabel;
    private Label locationLabel;
    private Label diceResultLabel;
    private TextArea gameLogArea;
    private ArrayList<VBox> propertySpaces;
    private Button rollDiceBtn;
    private Button buyPropertyBtn;
    private Button buildHouseBtn;
    private Button buildHotelBtn;
    private Button endTurnBtn;
    private ListView<String> propertiesList;
    private Label cardLabel;

    
    private final ArrayList<String> chanceDeck = new ArrayList<>(Arrays.asList(
            "Advance to GO (Collect $200)", "Bank error in your favor, collect $200", "Doctor's fees, pay $50",
            "Get out of Jail Free", "Go to Jail, go directly to Jail", "Pay poor tax of $15",
            "Your building and loan matures, collect $150", "You have won a crossword contest, collect $100"
    ));
    private final ArrayList<String> communityChestDeck = new ArrayList<>(Arrays.asList(
            "Advance to Illinois Ave", "Advance to St. Charles Place", "Bank pays you dividend of $50",
            "Go Back 3 Spaces", "Go directly to Jail", "Pay each player $50",
            "You have been elected Chairman of the Board, pay each player $50", "Collect $25 consultancy fee"
    ));
    private Queue<String> shuffledChance;
    private Queue<String> shuffledChest;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.random = new Random();
        this.doublesCount = 0;
        this.currentPlayerIndex = 0;
        this.propertySpaces = new ArrayList<>();
        this.colorGroups = initColorGroups();

        
        this.customFont = Font.font("Arial", 18);

        shuffleDecks();

        primaryStage.setTitle("Monopoly Game");
        showMainMenu();
    }

    private HashMap<String, ArrayList<Integer>> initColorGroups() {
        HashMap<String, ArrayList<Integer>> groups = new HashMap<>();
        groups.put("Brown", new ArrayList<>(Arrays.asList(1,2)));
        groups.put("Light Blue", new ArrayList<>(Arrays.asList(4,5,6)));
        groups.put("Pink", new ArrayList<>(Arrays.asList(8,9,10)));
        groups.put("Orange", new ArrayList<>(Arrays.asList(12,13,14)));
        groups.put("Red", new ArrayList<>(Arrays.asList(15,16,17)));
        groups.put("Yellow", new ArrayList<>(Arrays.asList(19,20,22)));
        groups.put("Green", new ArrayList<>(Arrays.asList(23,24,25)));
        groups.put("Dark Blue", new ArrayList<>(Arrays.asList(27,28)));
        return groups;
    }

    private void showMainMenu() {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setStyle("-fx-background-color: #1a472a;");

        Label titleLabel = new Label("MONOPOLY");
        titleLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 56));
        titleLabel.setTextFill(Color.WHITE);

        Button newGameBtn = new Button("New Game");
        Button loadGameBtn = new Button("Load Game");
        Button exitBtn = new Button("Exit");

        String buttonStyle = "-fx-font-size: 18px; -fx-min-width: 200px;" +
                "-fx-min-height: 50px; -fx-background-color: #124820;" +
                "-fx-text-fill: white; -fx-font-weight: bold;";
        newGameBtn.setStyle(buttonStyle);
        loadGameBtn.setStyle(buttonStyle);
        exitBtn.setStyle(buttonStyle);

        newGameBtn.setOnAction(e -> startNewGame());
        loadGameBtn.setOnAction(e -> loadGame());
        exitBtn.setOnAction(e -> primaryStage.close());

        menuLayout.getChildren().addAll(titleLabel, newGameBtn, loadGameBtn, exitBtn);

        Scene menuScene = new Scene(menuLayout, 900, 700);
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void startNewGame() {
        TextInputDialog dialog = new TextInputDialog("2");
        dialog.setTitle("New Game");
        dialog.setHeaderText("Setup New Game");
        dialog.setContentText("Number of players (2-4):");

        dialog.showAndWait().ifPresent(numPlayersStr -> {
            try {
                int numPlayers = Integer.parseInt(numPlayersStr);

                if (numPlayers >= 2 && numPlayers <= 4) {
                    board = new Board();

                    for (int i = 0; i < numPlayers; i++) {
                        TextInputDialog nameDialog = new TextInputDialog("Player " + (i + 1));
                        nameDialog.setTitle("Player Name");
                        nameDialog.setHeaderText("Enter name for Player " + (i + 1));
                        nameDialog.setContentText("Name:");

                        String name = nameDialog.showAndWait().orElse("Player " + (i + 1));
                        player newPlayer = new player(name, 1500);
                        board.addPlayer(newPlayer);
                    }

                    currentPlayerIndex = 0;
                    doublesCount = 0;
                    shuffleDecks();
                    showGameBoard();

                } else {
                    showAlert("Invalid Input", "Please enter a number between 2 and 4");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number");
            }
        });
    }

    private void shuffleDecks() {
        ArrayList<String> tempChance = new ArrayList<>(chanceDeck);
        ArrayList<String> tempChest = new ArrayList<>(communityChestDeck);
        Collections.shuffle(tempChance);
        Collections.shuffle(tempChest);
        shuffledChance = new LinkedList<>(tempChance);
        shuffledChest = new LinkedList<>(tempChest);
    }

    private void showGameBoard() {
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: #c7e8ca;");

        GridPane boardGrid = createBoard();
        gameLayout.setCenter(boardGrid);

        VBox infoPanel = createInfoPanel();
        gameLayout.setRight(infoPanel);

        HBox actionPanel = createActionPanel();
        gameLayout.setBottom(actionPanel);

        updateDisplay();

        ScrollPane scrollPane = new ScrollPane(gameLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene gameScene = new Scene(scrollPane, 1400, 900);
        primaryStage.setScene(gameScene);
    }

    private GridPane createBoard() {
        GridPane boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setPadding(new Insets(20));
        boardGrid.setHgap(3);
        boardGrid.setVgap(3);

        
        for (int i = 0; i <= 10; i++) {
            VBox space = createPropertySpace(i);
            propertySpaces.add(space);
            boardGrid.add(space, 10 - i, 10);
        }

      
        for (int i = 11; i < 20; i++) {
            VBox space = createPropertySpace(i);
            propertySpaces.add(space);
            boardGrid.add(space, 0, 20 - i);
        }


        for (int i = 20; i <= 30; i++) {
            VBox space = createPropertySpace(i);
            propertySpaces.add(space);
            boardGrid.add(space, i - 20, 0);
        }

    
        for (int i = 31; i < 40; i++) {
            VBox space = createPropertySpace(i);
            propertySpaces.add(space);
            boardGrid.add(space, 10, i - 30);
        }

 
        VBox centerArea = new VBox(15);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.setStyle("-fx-background-color: #1a472a; -fx-padding: 30;");
        centerArea.setMinSize(400, 400);

        Label centerLabel = new Label("MONOPOLY");
        centerLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 36));
        centerLabel.setTextFill(Color.WHITE);

        diceResultLabel = new Label("");
        diceResultLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 24));
        diceResultLabel.setTextFill(Color.YELLOW);

        cardLabel = new Label("");
        cardLabel.setFont(Font.font(customFont.getFamily(), FontWeight.NORMAL, 14));
        cardLabel.setTextFill(Color.LIGHTBLUE);
        cardLabel.setWrapText(true);
        cardLabel.setMaxWidth(350);
        cardLabel.setAlignment(Pos.CENTER);

        centerArea.getChildren().addAll(centerLabel, diceResultLabel, cardLabel);
        GridPane.setConstraints(centerArea, 1, 1, 9, 9);
        boardGrid.getChildren().add(centerArea);

        return boardGrid;
    }

    private VBox createPropertySpace(int position) {
        VBox space = new VBox(3);
        space.setAlignment(Pos.TOP_CENTER);
        space.setPadding(new Insets(5));
        space.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: white;");
        space.setMinSize(70, 100);
        space.setMaxSize(70, 100);

        property prop = board.getPropertyAt(position);


        Rectangle colorBar = new Rectangle(60, 20);
        colorBar.setFill(getPropertyColor(position));


        Label nameLabel = new Label(prop.getName());
        nameLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 8));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(60);
        nameLabel.setAlignment(Pos.CENTER);


        Label priceLabel = new Label("");
        if (prop.getPurchasePrice() > 0) {
            priceLabel.setText("$" + prop.getPurchasePrice());
            priceLabel.setFont(Font.font(customFont.getFamily(), 8));
        }

 
        HBox buildingsArea = new HBox(2);
        buildingsArea.setAlignment(Pos.CENTER);
        buildingsArea.setMinHeight(15);


        HBox tokensArea = new HBox(2);
        tokensArea.setAlignment(Pos.CENTER);
        tokensArea.setMinHeight(20);

        space.getChildren().addAll(colorBar, nameLabel, priceLabel, buildingsArea, tokensArea);

        return space;
    }

    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(15);
        infoPanel.setPadding(new Insets(20));
        infoPanel.setStyle("-fx-background-color: #f0f0f0;");
        infoPanel.setMinWidth(300);

        Label infoTitle = new Label("Game Info");
        infoTitle.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 22));

        currentPlayerLabel = new Label("Current Player: ");
        currentPlayerLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 16));

        moneyLabel = new Label("Money: $0");
        moneyLabel.setFont(Font.font(customFont.getFamily(), 14));

        locationLabel = new Label("Location: GO");
        locationLabel.setFont(Font.font(customFont.getFamily(), 14));

        Separator separator1 = new Separator();

        Label logTitle = new Label("Game Log:");
        logTitle.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 14));

        gameLogArea = new TextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setPrefHeight(200);
        gameLogArea.setWrapText(true);
        gameLogArea.setFont(Font.font(customFont.getFamily(), 11));

        Separator separator2 = new Separator();

        Label propertiesTitle = new Label("Owned Properties:");
        propertiesTitle.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 14));

        propertiesList = new ListView<>();
        propertiesList.setPrefHeight(180);
        propertiesList.setStyle("-fx-font-family: '" + customFont.getFamily() + "'; -fx-font-size: 11px;");

        infoPanel.getChildren().addAll(
                infoTitle, currentPlayerLabel, moneyLabel, locationLabel,
                separator1, logTitle, gameLogArea, separator2, propertiesTitle, propertiesList
        );

        return infoPanel;
    }

    private HBox createActionPanel() {
        HBox actionPanel = new HBox(15);
        actionPanel.setPadding(new Insets(20));
        actionPanel.setAlignment(Pos.CENTER);
        actionPanel.setStyle("-fx-background-color: #d4d4d4;");

        rollDiceBtn = new Button("🎲 Roll Dice");
        buyPropertyBtn = new Button("💰 Buy Property");
        buildHouseBtn = new Button("🏠 Build House");
        buildHotelBtn = new Button("🏨 Build Hotel");
        endTurnBtn = new Button("➡️ End Turn");
        Button saveGameBtn = new Button("💾 Save Game");
        Button menuBtn = new Button("📋 Menu");

        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 130; -fx-min-height: 45; " +
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;";

        rollDiceBtn.setStyle(buttonStyle);
        buyPropertyBtn.setStyle(buttonStyle);
        buildHouseBtn.setStyle(buttonStyle);
        buildHotelBtn.setStyle(buttonStyle);
        endTurnBtn.setStyle(buttonStyle);
        saveGameBtn.setStyle(buttonStyle);
        menuBtn.setStyle(buttonStyle);

        buyPropertyBtn.setDisable(true);
        endTurnBtn.setDisable(true);

        rollDiceBtn.setOnAction(e -> rollDice());
        buyPropertyBtn.setOnAction(e -> buyProperty());
        buildHouseBtn.setOnAction(e -> buildHouse());
        buildHotelBtn.setOnAction(e -> buildHotel());
        endTurnBtn.setOnAction(e -> endTurn());
        saveGameBtn.setOnAction(e -> saveGame());
        menuBtn.setOnAction(e -> showMainMenu());

        actionPanel.getChildren().addAll(
                rollDiceBtn, buyPropertyBtn, buildHouseBtn,
                buildHotelBtn, endTurnBtn, saveGameBtn, menuBtn
        );

        return actionPanel;
    }

    private void rollDice() {
        player currentPlayer = board.getPlayers().get(currentPlayerIndex);

        int dice1 = random.nextInt(6) + 1;
        int dice2 = random.nextInt(6) + 1;
        int totalRoll = dice1 + dice2;

        diceResultLabel.setText("🎲 " + dice1 + " + " + dice2 + " = " + totalRoll);
        cardLabel.setText("");
        logMessage("You rolled: " + dice1 + " and " + dice2 + " = " + totalRoll);

        if (dice1 == dice2) {
            doublesCount++;
            logMessage("Doubles! You rolled " + doublesCount + " time(s)");

            if (doublesCount == 3) {
                logMessage("You rolled doubles 3 times! Go to jail!");
                currentPlayer.setLocation(10);
                currentPlayer.subtractMoney(50);
                logMessage("Pay $50 to get out of jail");
                doublesCount = 0;
                updateDisplay();
                rollDiceBtn.setDisable(true);
                endTurnBtn.setDisable(false);
                return;
            }
        } else {
            doublesCount = 0;
        }

        int oldLocation = currentPlayer.getLocation();
        int newLocation = (oldLocation + totalRoll) % board.getProperties().size();
        currentPlayer.setLocation(newLocation);

        if (newLocation < oldLocation) {
            currentPlayer.addMoney(200);
            logMessage("🎉 You passed GO! Collect $200");
        }

        property currentProperty = board.getPropertyAt(newLocation);
        logMessage("You landed on: " + currentProperty.getName());

     
        handleSpecialSpaces(newLocation, currentPlayer);

 
        if (currentProperty.getPurchasePrice() > 0) {
            if (currentProperty.getOwner().equals("unowned")) {
                logMessage("This property costs $" + currentProperty.getPurchasePrice());
                buyPropertyBtn.setDisable(false);
                rollDiceBtn.setDisable(true);
            } else if (!currentProperty.getOwner().equals(currentPlayer.getName())) {
                logMessage("This property is owned by " + currentProperty.getOwner());
                int rent = currentProperty.getRentPrice();
                logMessage("You pay $" + rent + " rent");

                currentPlayer.subtractMoney(rent);

                for (player p : board.getPlayers()) {
                    if (p.getName().equals(currentProperty.getOwner())) {
                        p.addMoney(rent);
                    }
                }
            }
        }

        if (currentPlayer.getMoney() < 0) {
            logMessage(currentPlayer.getName() + " ran out of money!");
            for (property prop : board.getProperties()) {
                if (prop.getOwner().equals(currentPlayer.getName())) {
                    prop.setOwner("unowned");
                    prop.setHouses(0);
                    prop.setHotel(false);
                }
            }
            currentPlayer.setMoney(0);
        }

        updateDisplay();

        if (dice1 == dice2 && doublesCount < 3) {
            logMessage("You rolled doubles! Roll again!");
            rollDiceBtn.setDisable(false);
        } else {
            rollDiceBtn.setDisable(true);
            endTurnBtn.setDisable(false);
        }

        checkGameOver();
    }

    private void handleSpecialSpaces(int position, player currentPlayer) {
        String spaceName = board.getPropertyAt(position).getName();

        if (spaceName.contains("Chance")) {
            if (shuffledChance.isEmpty()) {
                shuffleDecks();
            }
            String card = shuffledChance.poll();
            logMessage("🎴 CHANCE: " + card);
            cardLabel.setText("🎴 CHANCE\n" + card);
            processChanceCard(card, currentPlayer);
        } else if (spaceName.contains("Community Chest")) {
            if (shuffledChest.isEmpty()) {
                shuffleDecks();
            }
            String card = shuffledChest.poll();
            logMessage("🎴 COMMUNITY CHEST: " + card);
            cardLabel.setText("🎴 COMMUNITY CHEST\n" + card);
            processCommunityChestCard(card, currentPlayer);
        } else if (spaceName.equals("Go to Jail")) {
            logMessage("💀 Go to Jail! Do not pass GO, do not collect $200");
            currentPlayer.setLocation(10);
            currentPlayer.subtractMoney(50);
        } else if (spaceName.equals("Income Tax")) {
            logMessage("💵 Pay Income Tax: $200");
            currentPlayer.subtractMoney(200);
        } else if (spaceName.equals("Luxury Tax")) {
            logMessage("💎 Pay Luxury Tax: $100");
            currentPlayer.subtractMoney(100);
        }
    }

    private void processChanceCard(String card, player currentPlayer) {
        if (card.contains("Advance to GO")) {
            currentPlayer.setLocation(0);
            currentPlayer.addMoney(200);
        } else if (card.contains("Bank error")) {
            currentPlayer.addMoney(200);
        } else if (card.contains("Doctor's fees")) {
            currentPlayer.subtractMoney(50);
        } else if (card.contains("Go to Jail")) {
            currentPlayer.setLocation(10);
            currentPlayer.subtractMoney(50);
        } else if (card.contains("Pay poor tax")) {
            currentPlayer.subtractMoney(15);
        } else if (card.contains("building and loan")) {
            currentPlayer.addMoney(150);
        } else if (card.contains("crossword contest")) {
            currentPlayer.addMoney(100);
        }

        updatePlayerTokens();
    }

    private void processCommunityChestCard(String card, player currentPlayer) {
        if (card.contains("Illinois")) {
            currentPlayer.setLocation(15);
        } else if (card.contains("St. Charles")) {
            currentPlayer.setLocation(8);
        } else if (card.contains("dividend")) {
            currentPlayer.addMoney(50);
        } else if (card.contains("Go Back 3")) {
            int newPos = currentPlayer.getLocation() - 3;
            if (newPos < 0) newPos += board.getProperties().size();
            currentPlayer.setLocation(newPos);
        } else if (card.contains("Go directly to Jail")) {
            currentPlayer.setLocation(10);
            currentPlayer.subtractMoney(50);
        } else if (card.contains("Pay each player") || card.contains("Chairman of the Board")) {
            int numPlayers = board.getPlayers().size();
            currentPlayer.subtractMoney(50 * (numPlayers - 1));
            for (player p : board.getPlayers()) {
                if (!p.equals(currentPlayer)) {
                    p.addMoney(50);
                }
            }
        } else if (card.contains("consultancy fee")) {
            currentPlayer.addMoney(25);
        }

        updatePlayerTokens();
    }

    private void buyProperty() {
        player currentPlayer = board.getPlayers().get(currentPlayerIndex);
        property currentProperty = board.getPropertyAt(currentPlayer.getLocation());

        if (currentPlayer.getMoney() >= currentProperty.getPurchasePrice()) {
            currentPlayer.subtractMoney(currentProperty.getPurchasePrice());
            currentProperty.setOwner(currentPlayer.getName());
            logMessage("✅ You bought " + currentProperty.getName() + "!");
            updateDisplay();
        } else {
            showAlert("Insufficient Funds", "You don't have enough money!");
        }

        buyPropertyBtn.setDisable(true);
        endTurnBtn.setDisable(false);
    }

    private void buildHouse() {
        player currentPlayer = board.getPlayers().get(currentPlayerIndex);

   
        ArrayList<property> eligibleProps = new ArrayList<>();
        for (property prop : board.getProperties()) {
            if (prop.getOwner().equals(currentPlayer.getName()) &&
                    prop.getPurchasePrice() > 0 &&
                    !prop.hasHotel() &&
                    prop.getHouses() < 4 &&
                    ownsFullColorSet(currentPlayer, prop)) {
                eligibleProps.add(prop);
            }
        }

        if (eligibleProps.isEmpty()) {
            showAlert("Cannot Build", "You must own all properties of a color set to build houses!");
            return;
        }

    
        ArrayList<String> propNames = new ArrayList<>();
        for (property p : eligibleProps) {
            propNames.add(p.getName() + " (" + p.getHouses() + " houses)");
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(propNames.get(0), propNames);
        dialog.setTitle("Build House");
        dialog.setHeaderText("Build a house ($50 each)");
        dialog.setContentText("Choose property:");

        dialog.showAndWait().ifPresent(choice -> {
            String propName = choice.split(" \\(")[0];
            for (property prop : eligibleProps) {
                if (prop.getName().equals(propName)) {
                    if (currentPlayer.getMoney() >= 50) {
                        currentPlayer.subtractMoney(50);
                        prop.setHouses(prop.getHouses() + 1);
                        logMessage("🏠 Built house on " + prop.getName() + " (now has " + prop.getHouses() + " houses)");
                        updateDisplay();
                    } else {
                        showAlert("Insufficient Funds", "You need $50 to build a house!");
                    }
                    break;
                }
            }
        });
    }

    private boolean ownsFullColorSet(player plr, property prop) {
        for (Map.Entry<String, ArrayList<Integer>> entry : colorGroups.entrySet()) {
            ArrayList<Integer> positions = entry.getValue();

       
            int propPosition = -1;
            for (int i = 0; i < board.getProperties().size(); i++) {
                if (board.getPropertyAt(i).equals(prop)) {
                    propPosition = i;
                    break;
                }
            }

            if (positions.contains(propPosition)) {
         
                for (int pos : positions) {
                    property p = board.getPropertyAt(pos);
                    if (!p.getOwner().equals(plr.getName())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void buildHotel() {
        player currentPlayer = board.getPlayers().get(currentPlayerIndex);

        ArrayList<property> eligibleProps = new ArrayList<>();
        for (property prop : board.getProperties()) {
            if (prop.getOwner().equals(currentPlayer.getName()) &&
                    prop.getHouses() == 4 &&
                    !prop.hasHotel() &&
                    ownsFullColorSet(currentPlayer, prop)) {
                eligibleProps.add(prop);
            }
        }

        if (eligibleProps.isEmpty()) {
            showAlert("Cannot Build", "You need 4 houses on a property before building a hotel!");
            return;
        }

        ArrayList<String> propNames = new ArrayList<>();
        for (property p : eligibleProps) {
            propNames.add(p.getName());
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(propNames.get(0), propNames);
        dialog.setTitle("Build Hotel");
        dialog.setHeaderText("Build a hotel ($100)");
        dialog.setContentText("Choose property:");

        dialog.showAndWait().ifPresent(propName -> {
            for (property prop : eligibleProps) {
                if (prop.getName().equals(propName)) {
                    if (currentPlayer.getMoney() >= 100) {
                        currentPlayer.subtractMoney(100);
                        prop.setHouses(0);
                        prop.setHotel(true);
                        logMessage("🏨 Built hotel on " + prop.getName() + "!");
                        updateDisplay();
                    } else {
                        showAlert("Insufficient Funds", "You need $100 to build a hotel!");
                    }
                    break;
                }
            }
        });
    }

    private void endTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % board.getPlayers().size();
        doublesCount = 0;

        rollDiceBtn.setDisable(false);
        buyPropertyBtn.setDisable(true);
        endTurnBtn.setDisable(true);

        diceResultLabel.setText("");
        cardLabel.setText("");
        updateDisplay();

        logMessage("\n--- " + board.getPlayers().get(currentPlayerIndex).getName() + "'s turn ---\n");
    }

    private void updateDisplay() {
        player currentPlayer = board.getPlayers().get(currentPlayerIndex);

        currentPlayerLabel.setText("Current Player: " + currentPlayer.getName());
        moneyLabel.setText("Money: $" + currentPlayer.getMoney());

        property currentProp = board.getPropertyAt(currentPlayer.getLocation());
        locationLabel.setText("Location: " + currentProp.getName() + " (#" + currentPlayer.getLocation() + ")");

    
        propertiesList.getItems().clear();
        for (property prop : board.getProperties()) {
            if (prop.getOwner().equals(currentPlayer.getName())) {
                String info = prop.getName();
                if (prop.hasHotel()) {
                    info += " [🏨]";
                } else if (prop.getHouses() > 0) {
                    info += " [🏠x" + prop.getHouses() + "]";
                }
                propertiesList.getItems().add(info);
            }
        }

        updatePlayerTokens();
        updateBuildingsOnBoard();
    }

    private void updatePlayerTokens() {
        for (VBox space : propertySpaces) {
            HBox tokensArea = (HBox) space.getChildren().get(4);
            tokensArea.getChildren().clear();
        }

        Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};

        for (int i = 0; i < board.getPlayers().size(); i++) {
            player p = board.getPlayers().get(i);
            int location = p.getLocation();

            if (location < propertySpaces.size()) {
                VBox space = propertySpaces.get(location);
                HBox tokensArea = (HBox) space.getChildren().get(4);

                Circle token = new Circle(5);
                token.setFill(playerColors[i % playerColors.length]);
                token.setStroke(Color.BLACK);
                token.setStrokeWidth(1);

                tokensArea.getChildren().add(token);
            }
        }
    }

    private void updateBuildingsOnBoard() {
        for (int i = 0; i < propertySpaces.size(); i++) {
            VBox space = propertySpaces.get(i);
            HBox buildingsArea = (HBox) space.getChildren().get(3);
            buildingsArea.getChildren().clear();

            property prop = board.getPropertyAt(i);

            if (prop.hasHotel()) {
                Rectangle hotel = new Rectangle(15, 15);
                hotel.setFill(Color.RED);
                hotel.setStroke(Color.BLACK);
                buildingsArea.getChildren().add(hotel);
            } else if (prop.getHouses() > 0) {
                for (int h = 0; h < prop.getHouses(); h++) {
                    Rectangle house = new Rectangle(8, 8);
                    house.setFill(Color.GREEN);
                    house.setStroke(Color.BLACK);
                    buildingsArea.getChildren().add(house);
                }
            }
        }
    }

    private void saveGame() {
        try {
            PrintWriter writer = new PrintWriter("monopoly_save.txt");

            writer.println(board.getPlayers().size());

            for (player player : board.getPlayers()) {
                writer.println(player.getName());
                writer.println(player.getMoney());
                writer.println(player.getLocation());
            }

            for (property property : board.getProperties()) {
                writer.println(property.getOwner());
                writer.println(property.getHouses());
                writer.println(property.hasHotel());
            }

            writer.println(currentPlayerIndex);

            writer.close();
            showAlert("Game Saved", "Your game has been saved successfully!");
            logMessage("💾 Game saved!");
        } catch (Exception e) {
            showAlert("Error", "Could not save game: " + e.getMessage());
        }
    }

    private void loadGame() {
        try {
            File file = new File("monopoly_save.txt");
            if (!file.exists()) {
                showAlert("No Save File", "No saved game found!");
                return;
            }

            Scanner fileScanner = new Scanner(file);

            board = new Board();
            int numPlayers = fileScanner.nextInt();
            fileScanner.nextLine();

            for (int i = 0; i < numPlayers; i++) {
                String name = fileScanner.nextLine();
                int money = fileScanner.nextInt();
                int location = fileScanner.nextInt();
                fileScanner.nextLine();

                player player = new player(name, money);
                player.setLocation(location);
                board.addPlayer(player);
            }

            for (int i = 0; i < board.getProperties().size(); i++) {
                String owner = fileScanner.nextLine();
                int houses = fileScanner.nextInt();
                boolean hotel = fileScanner.nextBoolean();
                fileScanner.nextLine();

                board.getProperties().get(i).setOwner(owner);
                board.getProperties().get(i).setHouses(houses);
                board.getProperties().get(i).setHotel(hotel);
            }

            currentPlayerIndex = fileScanner.nextInt();
            doublesCount = 0;

            fileScanner.close();

            shuffleDecks();
            showGameBoard();
            showAlert("Game Loaded", "Your game has been loaded successfully!");

        } catch (Exception e) {
            showAlert("Error", "Could not load game: " + e.getMessage());
        }
    }

    private void logMessage(String message) {
        gameLogArea.appendText(message + "\n");
        gameLogArea.setScrollTop(Double.MAX_VALUE);
    }

    private boolean checkGameOver() {
        int playersWithMoney = 0;
        player winner = null;

        for (player p : board.getPlayers()) {
            if (p.getMoney() > 0) {
                playersWithMoney++;
                winner = p;
            }
        }

        if (playersWithMoney <= 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over!");
            alert.setHeaderText("🎉 We have a winner! 🎉");
            alert.setContentText(winner.getName() + " wins with $" + winner.getMoney() + "!");
            alert.showAndWait();

            showMainMenu();
            return true;
        }
        return false;
    }

    private Color getPropertyColor(int position) {
    
        if (position == 1 || position == 3) return Color.rgb(139, 69, 19);
    
        if (position == 6 || position == 8 || position == 9) return Color.LIGHTBLUE;
    
        if (position == 11 || position == 13 || position == 14) return Color.PINK;
    
        if (position == 16 || position == 18 || position == 19) return Color.ORANGE;
   
        if (position == 21 || position == 23 || position == 24) return Color.RED;

        if (position == 26 || position == 27 || position == 29) return Color.YELLOW;
  
        if (position == 11 || position == 13 || position == 14) return Color.GREEN;
   
        if (position == 1 || position == 3) return Color.DARKBLUE;

        return Color.LIGHTGRAY;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

