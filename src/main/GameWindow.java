package main;

import javax.swing.JFrame;

public class GameWindow {

    private JFrame jFrame;

    public GameWindow(GamePanel gamePanel) {
        jFrame = new JFrame();
        jFrame.setSize(400, 400);
        jFrame.add(gamePanel);
        jFrame.setVisible(Boolean.TRUE);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
