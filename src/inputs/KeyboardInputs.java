package inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import main.GamePanel;

import static utils.Constants.Directions.*;

public class KeyboardInputs implements KeyListener {

    private GamePanel gamePanel;
    public KeyboardInputs(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch(e.getKeyCode()) {
        case KeyEvent.VK_W:
            gamePanel.getGame().getPlayer().setUp(Boolean.TRUE);
            break;
        case KeyEvent.VK_A:
            gamePanel.getGame().getPlayer().setLeft(Boolean.TRUE);
            break;
        case KeyEvent.VK_S:
            gamePanel.getGame().getPlayer().setDown(Boolean.TRUE);
            break;
        case KeyEvent.VK_D:
            gamePanel.getGame().getPlayer().setRight(Boolean.TRUE);
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        switch(e.getKeyCode()) {
        case KeyEvent.VK_W:
            gamePanel.getGame().getPlayer().setUp(Boolean.FALSE);
            break;
        case KeyEvent.VK_A:
            gamePanel.getGame().getPlayer().setLeft(Boolean.FALSE);
            break;
        case KeyEvent.VK_S:
            gamePanel.getGame().getPlayer().setDown(Boolean.FALSE);
            break;
        case KeyEvent.VK_D:
            gamePanel.getGame().getPlayer().setRight(Boolean.FALSE);
            break;
        }
    }

}
