package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.util.Random;

import entities.EnemyHandler;
import entities.Player;
import levels.LevelHandler;
import main.Game;
import objects.ObjectHandler;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utils.LoadSave;

import static utils.Constants.Environment.*;

public class Playing extends State implements StateMethods {

    private Player player;
    private LevelHandler levelHandler;
    private EnemyHandler enemyHandler;
    private ObjectHandler objectHandler;
    private GameOverOverlay gameOverOverlay;
    private LevelCompletedOverlay levelCompletedOverlay;
    private PauseOverlay pauseOverlay;
    private boolean paused = Boolean.FALSE;

    private int xLvlOffset;
    private int leftBorder = (int) (0.2 * Game.GAME_WIDTH);
    private int rightBorder = (int) (0.8 * Game.GAME_WIDTH);
    private int maxLvlOffsetX;

    private BufferedImage backgroundImg;
    private BufferedImage bigCloud;
    private BufferedImage smallCloud;
    private int[] smallCloudsPos;
    private Random rnd = new Random();

    private Boolean gameOver = Boolean.FALSE;
    private Boolean lvlCompleted = Boolean.FALSE;
    private Boolean playerDying = Boolean.FALSE;

    public Playing(Game game) {
        super(game);
        initClasses();

        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.PLAYING_BACKGROUND_IMG);
        bigCloud = LoadSave.getSpriteAtlas(LoadSave.BIG_CLOUDS);
        smallCloud = LoadSave.getSpriteAtlas(LoadSave.SMALL_CLOUDS);
        smallCloudsPos = new int[8];
        for (int i = 0; i < smallCloudsPos.length; i++)
            smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

        calculateLvlOffset();
        loadStartLevel();
    }

    public void loadNextLevel() {
        resetAll();
        levelHandler.loadNextLevel();
        player.setSpawn(levelHandler.getCurrentLevel().getPlayerLvlSpawn());
    }

    private void loadStartLevel() {
        enemyHandler.loadEnemies(levelHandler.getCurrentLevel());
        objectHandler.loadObjects(levelHandler.getCurrentLevel());
    }

    private void calculateLvlOffset() {
        maxLvlOffsetX = levelHandler.getCurrentLevel().getLvlOffset();
    }

    private void initClasses() {
        levelHandler = new LevelHandler(game);
        enemyHandler = new EnemyHandler(this);
        objectHandler = new ObjectHandler(this);
        player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
        player.loadLevelData(levelHandler.getCurrentLevel().getLevelData());
        player.setSpawn(levelHandler.getCurrentLevel().getPlayerLvlSpawn());
        pauseOverlay = new PauseOverlay(this);
        gameOverOverlay = new GameOverOverlay(this);
        levelCompletedOverlay = new LevelCompletedOverlay(this);// 13:15
    }

    @Override
    public void update() {
        if (paused) {
            pauseOverlay.update();
        } else if (lvlCompleted) {
            levelCompletedOverlay.update();
        } else if (gameOver) {
            gameOverOverlay.update();
        } else if (playerDying){
            player.update();
        } else {
            levelHandler.update();
            objectHandler.update(levelHandler.getCurrentLevel().getLevelData(), player);
            player.update();
            enemyHandler.update(levelHandler.getCurrentLevel().getLevelData(), player);
            checkCloseToBorder();
        }
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
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        drawClouds(g);

        levelHandler.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        enemyHandler.draw(g, xLvlOffset);
        objectHandler.draw(g, xLvlOffset);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlay.draw(g);
        } else if (gameOver) {
            gameOverOverlay.draw(g);
        } else if (lvlCompleted)
            levelCompletedOverlay.draw(g);
    }

    private void drawClouds(Graphics g) {

        for (int i = 0; i < 3; i++)
            g.drawImage(bigCloud, 0 + i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

        for (int i = 0; i < smallCloudsPos.length; i++)
            g.drawImage(smallCloud, i * 4 * SMALL_CLOUD_WIDTH - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
    }

    public void resetAll() {
        gameOver = Boolean.FALSE;
        paused = Boolean.FALSE;
        lvlCompleted = Boolean.FALSE;
        playerDying = Boolean.FALSE;
        player.resetAll();
        enemyHandler.resetAllEnemies();
        objectHandler.resetAllObjects();
    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void checkObjectHit(Rectangle2D.Float attackBox) {
        objectHandler.checkObjectHit(attackBox);
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        enemyHandler.checkEnemyHit(attackBox);
    }

    public void checkPotionTouched(Rectangle2D.Float hitbox) {
        objectHandler.checkObjectTouched(hitbox);
    }

    public void checkSpikesTouched(Player p) {
        objectHandler.checkSpikesTouched(p);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!gameOver)
            if (e.getButton() == MouseEvent.BUTTON1) {
                player.setAttack(Boolean.TRUE);
            }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!gameOver) {
            if (paused)
                pauseOverlay.mousePressed(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mousePressed(e);
        } else
            gameOverOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!gameOver) {
            if (paused)
                pauseOverlay.mouseReleased(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mouseReleased(e);
        } else
            gameOverOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!gameOver) {
            if (paused)
                pauseOverlay.mouseMoved(e);
            else if (lvlCompleted)
                levelCompletedOverlay.mouseMoved(e);
        } else
            gameOverOverlay.mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        if (!gameOver)
            if(paused)
                pauseOverlay.mouseDragged(e);

    }

    public void setLvlCompleted(Boolean lvlCompleted) {
        this.lvlCompleted = lvlCompleted;
    }

    public void setMaxLvlOffset(int lvlOffset) {
        this.maxLvlOffsetX = lvlOffset;
    }

    public void unpauseGame() {
        paused = Boolean.FALSE;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver)
            gameOverOverlay.keyPressed(e);
        else
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
        if (!gameOver)
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

    public EnemyHandler getEnemyHandler() {
        return enemyHandler;
    }

    public ObjectHandler getObjectHandler() {
        return objectHandler;
    }

    public LevelHandler getLevelHandler() {
        return levelHandler;
    }

    public void setPlayerDying(Boolean playerDying) {
        this.playerDying = playerDying;

    }
}
