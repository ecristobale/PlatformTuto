package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import effects.DialogueEffect;
import effects.Rain;
import entities.EnemyHandler;
import entities.Player;
import levels.LevelHandler;
import main.Game;
import objects.ObjectHandler;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utils.LoadSave;

import static utils.Constants.Environment.BIG_CLOUD_HEIGHT;
import static utils.Constants.Environment.BIG_CLOUD_WIDTH;
import static utils.Constants.Environment.SMALL_CLOUD_HEIGHT;
import static utils.Constants.Environment.SMALL_CLOUD_WIDTH;
import static utils.Constants.Dialogue.*;

public class Playing extends State implements StateMethods {

    private Player player;
    private LevelHandler levelHandler;
    private EnemyHandler enemyHandler;
    private ObjectHandler objectHandler;
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;
    private LevelCompletedOverlay levelCompletedOverlay;
    private GameCompletedOverlay gameCompletedOverlay;
    private Rain rain;

    private boolean paused = Boolean.FALSE;

    private int xLvlOffset = 0;
    private int leftBorder = (int) (0.25 * Game.GAME_WIDTH);
    private int rightBorder = (int) (0.75 * Game.GAME_WIDTH);
    private int maxLvlOffsetX = 0;

    private BufferedImage backgroundImg;
    private BufferedImage bigCloud;
    private BufferedImage smallCloud;
    private BufferedImage[] shipImgs;
    private BufferedImage[] questionImgs;
    private BufferedImage[] exclamationImgs;
    private ArrayList<DialogueEffect> dialogueEffects = new ArrayList<>();

    private int[] smallCloudsPos;
    private Random rnd = new Random();

    private Boolean gameOver = Boolean.FALSE;
    private Boolean lvlCompleted = Boolean.FALSE;
    private Boolean gameCompleted = Boolean.FALSE;
    private Boolean playerDying = Boolean.FALSE;
    private Boolean drawRain = Boolean.TRUE;

    private Boolean drawShipFirstLvl = Boolean.TRUE;
    private int shipAni = 0;
    private int shipTick = 0;
    private int shipDir = 1;
    private float shipHeightDelta = 0f;
    private float shipHeightChange = 0.05f * Game.SCALE;

    public Playing(Game game) {
        super(game);
        initClasses();

        backgroundImg = LoadSave.getSpriteAtlas(LoadSave.PLAYING_BACKGROUND_IMG);
        bigCloud = LoadSave.getSpriteAtlas(LoadSave.BIG_CLOUDS);
        smallCloud = LoadSave.getSpriteAtlas(LoadSave.SMALL_CLOUDS);
        smallCloudsPos = new int[8];
        for (int i = 0; i < smallCloudsPos.length; i++)
            smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

        shipImgs = new BufferedImage[4];
        BufferedImage temp = LoadSave.getSpriteAtlas(LoadSave.SHIP);
        for (int i = 0; i < shipImgs.length; i++)
            shipImgs[i] = temp.getSubimage(i * 78, 0, 78, 72);

        loadDialogue();
        calculateLvlOffset();
        loadStartLevel();
        setDrawRainBoolean();
    }

    private void loadDialogue() {
        loadDialogueImgs();

        // Load dialogue array with premade objects for avoiding ConcurrentModificationException error.
        // (Adding to a list that is being looped through.

        for (int i = 0; i < 10; i++)
            dialogueEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
        for (int i = 0; i < 10; i++)
            dialogueEffects.add(new DialogueEffect(0, 0, QUESTION));

        for (DialogueEffect de : dialogueEffects)
            de.deactive();
    }

    private void loadDialogueImgs() {
        BufferedImage qtemp = LoadSave.getSpriteAtlas(LoadSave.QUESTION_ATLAS);
        questionImgs = new BufferedImage[5];
        for (int i = 0; i < questionImgs.length; i++)
            questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

        BufferedImage etemp = LoadSave.getSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
        exclamationImgs = new BufferedImage[5];
        for (int i = 0; i < exclamationImgs.length; i++)
            exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
    }

    public void loadNextLevel() {
        levelHandler.setLvlIndex(levelHandler.getLvlIndex() + 1);
        levelHandler.loadNextLevel();
        player.setSpawn(levelHandler.getCurrentLevel().getPlayerLvlSpawn());
        resetAll();
        drawRain = Boolean.FALSE;
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
        levelCompletedOverlay = new LevelCompletedOverlay(this);
        gameCompletedOverlay = new GameCompletedOverlay(this);

        rain = new Rain();
    }

    @Override
    public void update() {
        if (paused) {
            pauseOverlay.update();
        } else if (lvlCompleted) {
            levelCompletedOverlay.update();
        } else if (gameCompleted) {
            gameCompletedOverlay.update();
        } else if (gameOver) {
            gameOverOverlay.update();
        } else if (playerDying){
            player.update();
        } else {
            updateDialogue();
            if (drawRain)
                rain.update(xLvlOffset);
            levelHandler.update();
            objectHandler.update(levelHandler.getCurrentLevel().getLevelData(), player);
            player.update();
            enemyHandler.update(levelHandler.getCurrentLevel().getLevelData(), player);
            checkCloseToBorder();
            if (drawShipFirstLvl)
                updateShipAni();
        }
    }

    private void updateShipAni() {
        shipTick++;
        if (shipTick >= 35) {
            shipTick = 0;
            shipAni++;
            if (shipAni >= 4)
                shipAni = 0;
        }

        shipHeightDelta += shipHeightChange * shipDir;
        shipHeightDelta = Math.max(Math.min(10 * Game.SCALE, shipHeightDelta), 0);

        if (shipHeightDelta == 0)
            shipDir = 1;
        else if (shipHeightDelta == 10 * Game.SCALE)
            shipDir = -1;
    }

    private void updateDialogue() {
        for (DialogueEffect de : dialogueEffects)
            if (de.isActive())
                de.update();
    }

    private void drawDialogue(Graphics g, int xLvlOffset) {
        for (DialogueEffect de : dialogueEffects)
            if (de.isActive()) {
                if (de.getType() == QUESTION)
                    g.drawImage(questionImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
                else
                    g.drawImage(exclamationImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
            }
    }

    public void addDialogue(int x, int y, int type) {
        dialogueEffects.add(new DialogueEffect(x, y - (int) (15 * Game.SCALE), type));
        for (DialogueEffect de : dialogueEffects)
            if (!de.isActive())
                if (de.getType() == type) {
                    de.reset(x, -(int) (15 * Game.SCALE));
                    return;
                }
    }

    private void checkCloseToBorder() {
        int playerX = (int) player.getHitbox().x;
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder)
            xLvlOffset += diff - rightBorder;
        else if (diff < leftBorder)
            xLvlOffset += diff - leftBorder;

        xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        drawClouds(g);
        if (drawRain)
            rain.draw(g, xLvlOffset);

        if (drawShipFirstLvl)
            g.drawImage(shipImgs[shipAni], (int) (100 * Game.SCALE) - xLvlOffset, (int) ((288 * Game.SCALE) + shipHeightDelta), (int) (78 * Game.SCALE), (int) (72 * Game.SCALE), null);


        levelHandler.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        enemyHandler.draw(g, xLvlOffset);
        objectHandler.draw(g, xLvlOffset);
        objectHandler.drawBackgroundTrees(g, xLvlOffset);
        drawDialogue(g, xLvlOffset);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlay.draw(g);
        } else if (gameOver)
            gameOverOverlay.draw(g);
        else if (lvlCompleted)
            levelCompletedOverlay.draw(g);
        else if (gameCompleted)
            gameCompletedOverlay.draw(g);
    }

    private void drawClouds(Graphics g) {
        for (int i = 0; i < 3; i++)
            g.drawImage(bigCloud, 0 + i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

        for (int i = 0; i < smallCloudsPos.length; i++)
            g.drawImage(smallCloud, i * 4 * SMALL_CLOUD_WIDTH - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
    }

    public void setGameCompleted() {
        gameCompleted = Boolean.TRUE;
    }

    public void resetGameCompleted() {
        gameCompleted = Boolean.FALSE;
    }

    public void resetAll() {
        gameOver = Boolean.FALSE;
        paused = Boolean.FALSE;
        lvlCompleted = Boolean.FALSE;
        playerDying = Boolean.FALSE;
        drawRain = Boolean.FALSE;

        setDrawRainBoolean();

        player.resetAll();
        enemyHandler.resetAllEnemies();
        objectHandler.resetAllObjects();
        dialogueEffects.clear();
    }

    private void setDrawRainBoolean() {
        // rain 20% times
        if (rnd.nextFloat() >= 0.8f)
            drawRain = true;
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
        if (!gameOver) {
            if (e.getButton() == MouseEvent.BUTTON1)
                player.setAttack(Boolean.TRUE);
            else if (e.getButton() == MouseEvent.BUTTON3)
                player.powerAttack();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mousePressed(e);
        } else if (paused)
            pauseOverlay.mousePressed(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mousePressed(e);
        else if (gameCompleted)
            gameCompletedOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mouseReleased(e);
        } else if (paused)
            pauseOverlay.mouseReleased(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mouseReleased(e);
        else if (gameCompleted)
            gameCompletedOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameOver) {
            gameOverOverlay.mouseMoved(e);
        } else if (paused)
            pauseOverlay.mouseMoved(e);
        else if (lvlCompleted)
            levelCompletedOverlay.mouseMoved(e);
        else if (gameCompleted)
            gameCompletedOverlay.mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e) {
        if (!gameOver && !gameCompleted && !lvlCompleted)
            if(paused)
                pauseOverlay.mouseDragged(e);

    }

    public void setLvlCompleted(Boolean lvlCompleted) {
        game.getAudioPlayer().lvlCompleted();
        if (levelHandler.getLvlIndex() + 1 >= levelHandler.getAmountOfLevels()) {
            gameCompleted = true;
            levelHandler.setLvlIndex(0);
            levelHandler.loadNextLevel();
            resetAll();
            return;
        }
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
        if (!gameOver && !gameCompleted && !lvlCompleted)
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
