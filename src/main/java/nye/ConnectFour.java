package nye;

import nye.adatbazis.Database;
import nye.jatekosok.AIPlayer;
import nye.jatekosok.Player;
import nye.tabla.Board;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class ConnectFour {
    public static void main(String[] args) {
        // Inicializáljuk az adatbázist
        Database.initializeDatabase();

        Scanner scanner = new Scanner(System.in);
        String filePath = "game_state.txt"; // A játékállást tartalmazó fájl elérési útja
        Board board;

        // Kérjük a felhasználót, hogy szeretné-e betölteni az előző játék állását
        System.out.println("Szeretnéd betölteni az előző játék állását? (y/n):");
        String loadResponse = scanner.nextLine().trim().toLowerCase();

        if (loadResponse.equals("y")) {
            try {
                board = Board.loadFromFile(filePath);
            } catch (IOException e) {
                System.out.println("Hiba a játék betöltésekor: " + e.getMessage());
                return; // Ha hiba történt a betöltéskor, kilépünk
            }
        } else {
            // Ha nem szeretnék betölteni, hozzunk létre egy új, üres táblát
            board = new Board(6, 7); // Az alapértelmezett tábla méret 6x7
            // Ha szükséges, töröljük a fájlt
            File file = new File(filePath);
            if (file.exists()) {
                file.delete(); // Töröljük a fájlt, ha létezik
            }
        }

        // Játékos neveinek bekérése
        System.out.println("Neve, Sárga Játékos:");
        String playerName = scanner.nextLine();
        Player human = new Player(playerName, 'Y');

        AIPlayer ai = new AIPlayer("AI", 'R');

        System.out.println("Üdvözöllek a Connec Four játékban, " + human.getName() + "!");
        board.display();

        boolean gameOver = false;
        while (!gameOver) {
            // Humán játékos lépése
            System.out.println("Te lépésed (válasz egy oszlopot: a, b, c, ...):");
            String move = scanner.nextLine();
            while (!board.makeMove(human, move)) {
                System.out.println("Érvénytelen lépés! Próbáld újra:");
                move = scanner.nextLine();
            }
            board.display();

            if (board.checkWin(human.getSymbol())) {
                System.out.println("Győztél!");
                // Játékos nyereményének frissítése az adatbázisban
                Database.updatePlayerWins(human.getName());
                gameOver = true;
                break;
            }

            if (board.isFull()) {
                System.out.println("Döntetlen!");
                break;
            }

            // Gépi játékos lépése
            System.out.println("A gép lép...");
            String aiMove = ai.chooseMove(board);
            board.makeMove(ai, aiMove);
            board.display();

            if (board.checkWin(ai.getSymbol())) {
                System.out.println("Gép győzőtt!");
                gameOver = true;
                break;
            }

            if (board.isFull()) {
                System.out.println("Döntetlen!");
                break;
            }
        }

        // A játék állásának mentése a fájlba
        try {
            board.saveToFile(filePath);
        } catch (IOException e) {
            System.out.println("Hiba játék mentésekor: " + e.getMessage());
        }

        // High score táblázat megjelenítése
        Database.displayHighScores();

        scanner.close();
    }
}


