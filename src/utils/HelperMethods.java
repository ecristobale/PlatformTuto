package utils;

import java.awt.geom.Rectangle2D;

import main.Game;

public class HelperMethods {

    public static boolean canMoveHere(float x, float y, float width, float height, int[][] levelData) {

        if (!isSolid(x, y, levelData))
            if (!isSolid(x+width, y+height, levelData))
                if (!isSolid(x+width, y, levelData))
                    if (!isSolid(x, y+height, levelData))
                        return Boolean.TRUE;
        return Boolean.FALSE;
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

    private static boolean isTileSolid(int xTile, int yTile, int[][] levelData) {
        int value = levelData[yTile][xTile];
        // Sprite level atlas: 48, 12º sprite is blank
        if (value >= 48 || value < 0 || value != 11) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static boolean isFloor(Rectangle2D.Float hitbox, float xSpeed, int[][] levelData) {
        if (xSpeed > 0 )
            return isSolid(hitbox.x + hitbox.width + xSpeed, hitbox.y + hitbox.height + 1, levelData);
        else
            return isSolid(hitbox.x + xSpeed, hitbox.y + hitbox.height + 1, levelData);
    }

    public static boolean isAllTilesWalkable(int xStart, int xEnd, int y, int[][] levelData) {
        for (int i = 0; i < xEnd - xStart; i++) {
            if (isTileSolid(xStart + i, y, levelData))
                return Boolean.FALSE;
            if (!isTileSolid(xStart + i, y + 1, levelData))
                return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public static boolean isSightClear(int[][] levelData, Rectangle2D.Float firstHitbox, Rectangle2D.Float secondHitbox, int tileY) {
        int firstXTile = (int) (firstHitbox.x / Game.TILES_SIZE);
        int secondXTile = (int) (secondHitbox.x / Game.TILES_SIZE);

        if (firstXTile > secondXTile)
            return isAllTilesWalkable(secondXTile, firstXTile, tileY, levelData);
        else
            return isAllTilesWalkable(firstXTile, secondXTile, tileY, levelData);

    }
}
