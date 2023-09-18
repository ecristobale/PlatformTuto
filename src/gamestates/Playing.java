package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import entities.Player;
import levels.LevelHandler;
import main.Game;
import ui.PauseOverlay;

public class Playing extends State implements StateMethods {

    private Player player;
    private LevelHandler levelHandler;
    private PauseOverlay pauseOverlay;
    private boolean paused = Boolean.FALSE;

    public Playing(Game game) {
        super(game);
        initClasses();
    }

    private void initClasses() {
        levelHandler = new LevelHandler(game);
        player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE));
        player.loadLevelData(levelHandler.getCurrentLevel().getLevelData());
        pauseOverlay = new PauseOverlay(this);
    }

    @Override
    public void update() {
        if (!paused) {
            levelHandler.update();
            player.update();
        } else
            pauseOverlay.update();
    }

    @Override
    public void draw(Graphics g) {
        levelHandler.draw(g);
        player.render(g);

        if (paused)
            pauseOverlay.draw(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            player.setAttack(Boolean.TRUE);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (paused)
            pauseOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (paused)
            pauseOverlay.mouseReleased(e);

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused)
            pauseOverlay.mouseMoved(e);

    }

    public void mouseDragged(MouseEvent e) {
        if(paused)
            pauseOverlay.mouseDragged(e);

    }

    public void unpauseGame() {
        paused = Boolean.FALSE;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_A:
            player.setLeft(Boolean.TRUE);
            break;
        case KeyEvent.VK_D:
            player.setRight(Boolean.TRUE);
            break;
        case KeyEvent.VK_SPACE:
            player.setJump(Boolean.TRUE);
            break;
        case KeyEvent.VK_ESCAPE:
            paused = !paused;
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_A:
            player.setLeft(Boolean.FALSE);
            break;
        case KeyEvent.VK_D:
            player.setRight(Boolean.FALSE);
            break;
        case KeyEvent.VK_SPACE:
            player.setJump(Boolean.FALSE);
            break;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void windowLostFocus() {
        player.resetDirBooleans();

    }
}
