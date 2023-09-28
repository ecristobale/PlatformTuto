package levels;

import static utils.HelperMethods.getCrabs;
import static utils.HelperMethods.getLvlData;
import static utils.HelperMethods.getPlayerSpawn;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Crabby;
import main.Game;
import objects.Cannon;
import objects.GameContainer;
import objects.Potion;
import objects.Spike;
import utils.HelperMethods;

public class Level {

    private BufferedImage img;
    private int[][] lvlData;
    private ArrayList<Crabby> crabbies;
    private ArrayList<Potion> potionList;
    private ArrayList<GameContainer> containerList;
    private ArrayList<Cannon> cannonList;
    private ArrayList<Spike> spikeList;
    private int lvlTilesWide;
    private int maxTilesOffset;
    private int maxLvlOffsetX;
    private Point playerLvlSpawn;

    public Level(BufferedImage img) {
        this.img = img;
        createLevelData();
        createEnemies();
        createPotions();
        createContainers();
        createSpikes();
        createCannons();
        calcLvlOffsets();
        calcPlayerSpawn();
    }

    private void createCannons() {
        cannonList = HelperMethods.getCannons(img);
    }

    private void createSpikes() {
        spikeList = HelperMethods.getSpikes(img);
    }

    private void createContainers() {
        containerList = HelperMethods.getContainers(img);
    }

    private void createPotions() {
        potionList = HelperMethods.getPotions(img);
    }

    private void calcPlayerSpawn() {
        playerLvlSpawn = getPlayerSpawn(img);
    }

    private void calcLvlOffsets() {
        lvlTilesWide = img.getWidth();
        maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
        maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;
    }

    private void createEnemies() {
        crabbies = getCrabs(img);
    }

    private void createLevelData() {
        lvlData = getLvlData(img);
    }

    public int getSpriteIndex(int x, int y) {
        return lvlData[y][x];
    }

    public int[][] getLevelData() {
        return lvlData;
    }

    public int getLvlOffset() {
        return maxLvlOffsetX;
    }

    public ArrayList<Crabby> getCrabbies() {
        return crabbies;
    }

    public Point getPlayerLvlSpawn() {
        return playerLvlSpawn;
    }

    public ArrayList<Potion> getPotionList() {
        return potionList;
    }

    public ArrayList<GameContainer> getContainerList() {
        return containerList;
    }

    public ArrayList<Spike> getSpikeList() {
        return spikeList;
    }

    public ArrayList<Cannon> getCannonList() {
        return cannonList;
    }
}