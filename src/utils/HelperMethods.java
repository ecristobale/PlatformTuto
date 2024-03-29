package utils;


import java.awt.geom.Rectangle2D;

import main.Game;
import objects.Projectile;

public class HelperMethods {

    public static boolean canMoveHere(float x, float y, float width, float height, int[][] levelData) {

        if (!isSolid(x, y, levelData))
            if (!isSolid(x+width, y+height, levelData))
                if (!isSolid(x+width, y, levelData))
                    if (!isSolid(x, y+height, levelData))
                        return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public static boolean isEntityOnFloor(Rectangle2D.Float hitbox, int[][] levelData) {
        // Check pixel below bottomleft and bottomright
        if (!isSolid(hitbox.x, hitbox.y + hitbox.height + 1, levelData))
            if (!isSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, levelData))
                return Boolean.FALSE;

        return Boolean.TRUE;
    }

    private static boolean isSolid(float x, float y, int[][] levelData) {
        int maxWidth = levelData[0].length * Game.TILES_SIZE;
        if (x < 0 || x >= maxWidth) {
            return Boolean.TRUE;
        }
        if (y < 0 || y >= Game.GAME_HEIGHT) {
            return Boolean.TRUE;
        }

        float xIndex = x / Game.TILES_SIZE;
        float yIndex = y / Game.TILES_SIZE;

        return isTileSolid((int)xIndex, (int)yIndex, levelData);
    }

    public static boolean isProjectileHittingLevel(Projectile p, int [][] levelData) {
        return isSolid(p.getHibox().x + p.getHibox().width / 2, p.getHibox().y + p.getHibox().height / 2, levelData);
    }

    public static boolean isEntityInWater(Rectangle2D.Float hitbox, int[][] lvlData) {
        // Entity touch top water?
        if (getTileValue(hitbox.x, hitbox.y + hitbox.height, lvlData) != 48)
            if (getTileValue(hitbox.x + hitbox.width, hitbox.y + hitbox.height, lvlData) != 48)
                return false;
        return true;
    }

    private static int getTileValue(float xPos, float yPos, int[][] lvlData) {
        int xCord = (int) (xPos / Game.TILES_SIZE);
        int yCord = (int) (yPos / Game.TILES_SIZE);
        return lvlData[yCord][xCord];
    }

    private static boolean isTileSolid(int xTile, int yTile, int[][] levelData) {
        int value = levelData[yTile][xTile];
        // Sprite level atlas: 48, 49, 12º sprite is blank
        switch (value) {
        case 11, 48, 49:
            return false;
        default:
            return true;
        }
    }

    public static float getEntityXPositionNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
        int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
        if (xSpeed > 0) {// to right
            // Pixel value for the current tile
            int tileXPositionPixel = currentTile * Game.TILES_SIZE;
            int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
            return tileXPositionPixel + xOffset - 1;
        } else {// to left
            return currentTile * Game.TILES_SIZE;
        }
    }

    public static float getEntityYPositionUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
        int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
        if (airSpeed > 0) {
            // Falling - touching floor
            int tileYPositionPixel = currentTile * Game.TILES_SIZE;
            int yOffset = (int) (Game.TILES_SIZE - hitbox.height);
            return tileYPositionPixel + yOffset - 1;
        } else {
            // Jumping
            return currentTile * Game.TILES_SIZE;
        }
    }

    public static boolean isFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] lvlData) {
        if (xSpeed > 0)
            return isSolid(hitbox.x + hitbox.width + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
        else
            return isSolid(hitbox.x + xSpeed, hitbox.y + hitbox.height + 1, lvlData);
    }

    public static boolean isFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
        if (!isSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
            if (!isSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
                return false;
        return true;
    }

    public static boolean canCannonSeePlayer(int[][] levelData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int yTile) {
        int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
        int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return isAllTilesClear(secondXTile, firstXTile, yTile, levelData);
        else
            return isAllTilesClear(firstXTile, secondXTile, yTile, levelData);
    }

    public static boolean isAllTilesClear(int xStart, int xEnd, int y, int[][] levelData) {
        for (int i = 0; i < xEnd - xStart; i++)
            if (isTileSolid(xStart + i, y, levelData))
                return Boolean.FALSE;

        return Boolean.TRUE;
    }

    public static boolean isAllTilesWalkable(int xStart, int xEnd, int y, int[][] levelData) {
        if (isAllTilesClear(xStart, xEnd, y, levelData))
            for (int i = 0; i < xEnd - xStart; i++) {
                if (!isTileSolid(xStart + i, y + 1, levelData))
                    return Boolean.FALSE;
            }
        return Boolean.TRUE;
    }

    public static boolean isSightClear(int[][] lvlData, Rectangle2D.Float enemyBox, Rectangle2D.Float playerBox, int yTile) {
        int firstXTile = (int) (enemyBox.x / Game.TILES_SIZE);

        int secondXTile;
        if (isSolid(playerBox.x, playerBox.y + playerBox.height + 1, lvlData))
            secondXTile = (int) (playerBox.x / Game.TILES_SIZE);
        else
            secondXTile = (int) ((playerBox.x + playerBox.width) / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return isAllTilesWalkable(secondXTile, firstXTile, yTile, lvlData);
        else
            return isAllTilesWalkable(firstXTile, secondXTile, yTile, lvlData);
    }

    public static boolean isSightClearOld(int[][] levelData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int tileY) {
        int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
        int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return isAllTilesWalkable(secondXTile, firstXTile, tileY, levelData);
        else
            return isAllTilesWalkable(firstXTile, secondXTile, tileY, levelData);

    }

//    public static int[][] getLvlData(BufferedImage img) {
//        int[][] levelData = new int[img.getHeight()][img.getWidth()];
//
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getRed();
//                if (value >= 48) {
//                    value = 0;
//                }
//                levelData[j][i] = value;
//            }
//        }
//        return levelData;
//    }
//
//    public static ArrayList<Crabby> getCrabs(BufferedImage img) {
//        ArrayList<Crabby> crabbyList = new ArrayList<>();
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getGreen();
//                if (value == CRABBY)
//                    crabbyList.add(new Crabby(i * Game.TILES_SIZE, j * Game.TILES_SIZE));
//            }
//        }
//        return crabbyList;
//    }
//
//    public static Point getPlayerSpawn(BufferedImage img) {
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getGreen();
//                if (value == 100)
//                    return new Point(i * Game.TILES_SIZE, j * Game.TILES_SIZE);
//            }
//        }
//        return new Point(1 * Game.TILES_SIZE, 1 * Game.TILES_SIZE);
//    }
//
//    public static ArrayList<Potion> getPotions(BufferedImage img) {
//        ArrayList<Potion> potionList = new ArrayList<>();
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getBlue();
//                if (value == RED_POTION || value == BLUE_POTION)
//                    potionList.add(new Potion(i * Game.TILES_SIZE, j * Game.TILES_SIZE, value));
//            }
//        }
//        return potionList;
//    }
//
//    public static ArrayList<GameContainer> getContainers(BufferedImage img) {
//        ArrayList<GameContainer> containerList = new ArrayList<>();
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getBlue();
//                if (value == BOX || value == BARREL)
//                    containerList.add(new GameContainer(i * Game.TILES_SIZE, j * Game.TILES_SIZE, value));
//            }
//        }
//        return containerList;
//    }
//
//    public static ArrayList<Spike> getSpikes(BufferedImage img) {
//        ArrayList<Spike> spikeList = new ArrayList<>();
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getBlue();
//                if (value == SPIKE)
//                    spikeList.add(new Spike(i * Game.TILES_SIZE, j * Game.TILES_SIZE, SPIKE));
//            }
//        }
//        return spikeList;
//    }
//
//    public static ArrayList<Cannon> getCannons(BufferedImage img) {
//        ArrayList<Cannon> cannonList = new ArrayList<>();
//        for (int j = 0; j < img.getHeight(); j++) {
//            for (int i = 0; i < img.getWidth(); i++) {
//                Color color = new Color(img.getRGB(i, j));
//                int value = color.getBlue();
//                if (value == CANNON_LEFT || value == CANNON_RIGHT)
//                    cannonList.add(new Cannon(i * Game.TILES_SIZE, j * Game.TILES_SIZE, value));
//            }
//        }
//        return cannonList;
//    }
}
