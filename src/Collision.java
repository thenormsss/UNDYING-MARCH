import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Collision {

    // List of all obstacles (rectangles) in map
    private final List<Rectangle> obstacles = new ArrayList<>();
     
    // Size of each tile in pixels. Used for converting map coordinates to pixel positions
    private int tileSize = 64; // size of each tile in pixels

    // Add obstacle manually (direct to the code. see it in class GamePanel -> initCollisions())
    public void addObstacle(int x, int y, int width, int height) {
        obstacles.add(new Rectangle(x, y, width, height));
    }

    // OPTIONAL: Add obstacles dynamically (it will come from a Tiled or txt file)
    public void loadFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int row = 0;

            // Read file line by line (each line = one tile row)
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int col = 0; col < values.length; col++) {
                    // If the tile is marked as solid ("1"), create an obstacle rectangle
                    if (values[col].trim().equals("1")) {
                        int x = col * tileSize;
                        int y = row * tileSize;
                        obstacles.add(new Rectangle(x, y, tileSize, tileSize));
                    }
                }
                row++;
            }
        } catch (IOException e) {
            System.out.println("Failed to load collision file: " + e.getMessage());
        }
    }

    // Check collision if MC collides with obstacles
    public boolean checkCollision(Rectangle rect) {
        for (Rectangle obs : obstacles) {
            if (rect.intersects(obs)) return true;
        }
        return false;
    }

    // To get what sensor detect
    public Rectangle getCollision(Rectangle rect) {
        for (Rectangle obs : obstacles) {
            if (rect.intersects(obs)) return obs;
        }
        return null;
    }

    // To visualize all of the obstacles
    public void draw(Graphics2D g, int cameraX, int cameraY) {
        g.setColor(new Color(255, 255, 255, 120)); // Semi-transparent white
        for (Rectangle obs : obstacles) {
            g.fillRect(obs.x - cameraX, obs.y - cameraY, obs.width, obs.height);
        }
    }

    // Return the list of all obstacles
    public List<Rectangle> getObstacles() {
        return obstacles;
    }
}