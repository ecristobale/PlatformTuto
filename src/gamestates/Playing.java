package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import entities.Player;
import levels.LevelHandler;
import main.Game;
import ui.PauseOverlay;
import utils.LoadSave;

public class Playing extends State implements StateMethods {

    private Player player;
    private LevelHandler levelHandler;
    private PauseOverlay pauseOverlay;
    private boolean paused = Boolean.FALSE;

    private int xLvlOffset;
    private int leftBorder = (int) (0.2 * Game.GAME_WIDTH);
    private int rightBorder = (int) (0.8 * Game.GAME_WIDTH);
    private int lvlTilesWide = LoadSave.getLevelData()[0].length;
    private int maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
    private int maxLvlOffsetX = maxTilesOffset * Game.TILES_SIZE;

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
            checkCloseToBorder();
        } else
            pauseOverlay.update();
    }

    private void checkCloseToBorder() {
        int playerX = (int) player.getHitbox().x;
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder)
            xLvlOffset += diff - rightBorder;
        else if (diff < leftBorder)
            xLvlOffset += diff - leftBorder;

        if (xLvlOffset > maxLvlOffsetX)
            xLvlOffset = maxLvlOffsetX;
        else if (xLvlOffset < 0)
            xLvlOffset = 0;

    }

    @Override
    public void draw(Graphics g) {
        levelHandler.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlay.draw(g);
        }
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
