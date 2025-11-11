package org.example;

import org.example.catalog.Catalog;
import org.example.commands.Command;
import org.example.parser.Parser;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        System.out.println("=== MiniDB Started ===");
        Catalog catalog = new Catalog("catalog.json");
        Parser parser = new Parser(catalog);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("db> ");
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) break;
            if (line.isEmpty()) continue;

            try {
                Command cmd = parser.parse(line);
                if (cmd != null) cmd.execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        catalog.save();
        System.out.println("Bye!");
    }
}
