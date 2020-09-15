package com.codegym.task.task35.task3513;

import javax.swing.*;

public class Main {
    private static Model model = new Model();
    private static Controller controller = new Controller(model);
    private static JFrame game = new JFrame();

    public static void main(String[] args) {
        game.setTitle("2048");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize(450, 500);
        game.setResizable(false);

        game.add(controller.getView());

        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }
}

