import java.util.Scanner;
import java.util.Random;
import java.io.*;

// this file is for the main game logic and the controls
public class MonopolyGame
{
    private Board board;
    private final Scanner scanner;
    private final Random random;
    private int doublesCount;

    public MonopolyGame()
    {
        board = new Board();
        scanner = new Scanner(System.in);
        random = new Random();

        doublesCount = 0;
    }

    public void startGame()
    {
        System.out.println("Welcome to Monopoly!");
        System.out.println("How many players? (2-4)");

        int numPlayers = scanner.nextInt();

        scanner.nextLine();

        for (int i = 0; i < numPlayers; i++)
        {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();

            player player = new player(name, 1500);

            board.addPlayer(player);
        }

        System.out.println("\nGame started! Each player starts with $1500");
        playGame();
    }

    public void playGame()
    {
        boolean gameOver = false;

        while (!gameOver)
        {
            for (player player : board.getPlayers())
            {
                if (player.getMoney() <= 0)
                {
                    continue;
                }

                System.out.println("\n" + player.getName() + "'s turn");
                System.out.println("Money: $" + player.getMoney());
                System.out.println("Location: " + player.getLocation() + " - " +
                        board.getPropertyAt(player.getLocation()).getName());

                System.out.println("\nPress 1 to roll dice, 2 to save game, 3 to load game, 4 to quit");

                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1)
                {
                    takeTurn(player);
                } else if (choice == 2)
                {
                    saveGame();
                } else if (choice == 3)
                {
                    loadGame();
                } else if (choice == 4)
                {
                    System.out.println("Thanks for playing!");
                    return;
                }

                if (checkGameOver())
                {
                    gameOver = true;
                    break;
                }
            }
        }
    }

    public void takeTurn(player player)
    {
        int dice1 = random.nextInt(6) + 1;
        int dice2 = random.nextInt(6) + 1;
        int totalRoll = dice1 + dice2;

        System.out.println("You rolled: " + dice1 + " and " + dice2 + " = " + totalRoll);

        if (dice1 == dice2)
        {
            doublesCount++;
            System.out.println("Doubles! You rolled " + doublesCount + " time(s)");

            if (doublesCount == 3)
            {
                System.out.println("You rolled doubles 3 times! Go to jail!");

                player.setLocation(10);
                player.subtractMoney(50);

                System.out.println("Pay $50 to get out of jail");

                doublesCount = 0;
                return;
            }
        } else
        {
            doublesCount = 0;
        }

        int oldLocation = player.getLocation();
        int newLocation = (oldLocation + totalRoll) % board.getProperties().size();

        player.setLocation(newLocation);

        if (newLocation < oldLocation)
        {
            player.addMoney(200);

            System.out.println("You passed GO! Collect $200");
        }

        property currentProperty = board.getPropertyAt(newLocation);
        System.out.println("You landed on: " + currentProperty.getName());

        if (currentProperty.getPurchasePrice() > 0)
        {
            if (currentProperty.getOwner().equals("unowned"))
            {
                System.out.println("This property costs $" + currentProperty.getPurchasePrice());
                System.out.println("Do you want to buy it? (yes/no)");

                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("yes"))
                {
                    if (player.getMoney() >= currentProperty.getPurchasePrice())
                    {
                        player.subtractMoney(currentProperty.getPurchasePrice());
                        currentProperty.setOwner(player.getName());
                        System.out.println("You bought " + currentProperty.getName() + "!");
                    } else
                    {
                        System.out.println("You don't have enough money!");
                    }
                }
            } else if (!currentProperty.getOwner().equals(player.getName()))
            {
                System.out.println("This property is owned by " + currentProperty.getOwner());
                System.out.println("You pay $" + currentProperty.getRentPrice() + " rent");

                player.subtractMoney(currentProperty.getRentPrice());

                for (player p : board.getPlayers())
                {
                    if (p.getName().equals(currentProperty.getOwner()))
                    {
                        p.addMoney(currentProperty.getRentPrice());
                    }
                }
            }
        }

        if (player.getMoney() < 0)
        {
            System.out.println(player.getName() + " ran out of money!");
            for (property prop : board.getProperties())
            {
                if (prop.getOwner().equals(player.getName()))
                {
                    prop.setOwner("unowned");
                }
            }
            player.setMoney(0);
        }

        if (dice1 == dice2 && doublesCount < 3)
        {
            System.out.println("You rolled doubles! Roll again!");
            System.out.println("Press enter to continue...");

            scanner.nextLine();
            takeTurn(player);
        }
    }

    public boolean checkGameOver()
    {
        int playersWithMoney = 0;

        for (player player : board.getPlayers())
        {
            if (player.getMoney() > 0)
            {
                playersWithMoney++;
            }
        }

        if (playersWithMoney <= 1)
        {
            System.out.println("\nGame Over!");

            for (player player : board.getPlayers())
            {
                if (player.getMoney() > 0)
                {
                    System.out.println(player.getName() + " wins with $" + player.getMoney() + "!");
                }
            }
            return true;
        }
        return false;
    }

    public void saveGame()
    {
        try
        {
            PrintWriter writer = new PrintWriter("monopoly_save.txt");

            writer.println(board.getPlayers().size());

            for (player player : board.getPlayers())
            {
                writer.println(player.getName());
                writer.println(player.getMoney());
                writer.println(player.getLocation());
            }

            for (property property : board.getProperties())
            {
                writer.println(property.getOwner());
            }

            writer.close();
            System.out.println("Game saved!");
        } catch (Exception e)
        {
            System.out.println("Error saving game");
        }
    }

    public void loadGame()
    {
        try
        {
            File file = new File("monopoly_save.txt");
            Scanner fileScanner = new Scanner(file);

            board = new Board();
            int numPlayers = fileScanner.nextInt();

            fileScanner.nextLine();

            for (int i = 0; i < numPlayers; i++)
            {
                String name = fileScanner.nextLine();
                int money = fileScanner.nextInt();
                int location = fileScanner.nextInt();
                fileScanner.nextLine();

                player player = new player(name, money);
                player.setLocation(location);
                board.addPlayer(player);
            }

            for (int i = 0; i < board.getProperties().size(); i++)
            {
                String owner = fileScanner.nextLine();
                board.getProperties().get(i).setOwner(owner);
            }

            fileScanner.close();

            System.out.println("Game loaded!");

        } catch (Exception e)
        {
            System.out.println("Error loading game or no save file found");
        }
    }

    public static void main(String[] args)
    {
        MonopolyGame game = new MonopolyGame();
        game.startGame();
    }
}