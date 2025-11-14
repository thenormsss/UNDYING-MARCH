import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class EnemyFactory {

    // ========================================
    // FACTORY METHODS FOR DIFFERENT ENEMY TYPES
    // ========================================
    
    public static Enemy createDummy(int x, int y) throws IOException {
        Enemy dummy = new Enemy(x, y);
        
        // Configure as stationary target (no AI behavior)
        dummy.setEnemy("Practice Dummy")
            .setStats(100, 0, 0.0f)
            .setCombatRanges(0, 0, 0, 0, 0)
            .setDamage(0, 0)
            .setCustomSize(64, 64)
            .setCustomHitbox(40, 45, 15)
            .setHideHealthBars(true)
            .setAutoRespawn(true, 3000);
            
        // Load dummy animations
        loadDummyAnimations(dummy);
        
        return dummy;
    }

    public static Enemy createBandit(int x, int y) throws IOException {
        Enemy bandit = new Enemy(x, y);
        
        // Configure stats (primarily melee fighter, doesn't use ranged attacks)
        bandit.setEnemy("Bandit")
              .setStats(100,100, 1.5f)
              .setCombatRanges(300, 40, 60, 0, 0)
              .setDamage(5, 0)
              .setAutoRespawn(true, 10000);
        
        // Load animations
        loadBanditAnimations(bandit);

        return bandit;
    }
    
    public static Enemy createHighMage(int x, int y) throws IOException {
        Enemy high_mage = new Enemy(x, y);
        
        // Configure stats (range specialist, avoids close combat)
        high_mage.setEnemy("High Mage")
            .setStats(100,100, 1.5f)
            .setCombatRanges(300,0,0,200,150)
            .setDamage(0,5)
            .setProjectileSpawnFrame(5)
            .setAutoRespawn(true, 10000);
        
        // Load animations
        loadHighMageAnimations(high_mage);
        
        return high_mage;
    }

    public static Enemy createMusketeer(int x, int y) throws IOException {
        Enemy musketeer = new Enemy(x, y);
        
        // Configure stats (balanced for both combat styles)
        musketeer.setEnemy("Musketeer")
            .setStats(100,100,1.5f)
            .setCombatRanges(300,40,60,200,150)
            .setDamage(8,8)
            .setProjectileSpawnFrame(3)
            .setAutoRespawn(true, 10000);
        
        // Load animations
        loadMusketeerAnimations(musketeer);
        
        return musketeer;
    }

    // ========================================
    // ANIMATION LOADING HELPERS
    // ========================================
    
    private static void loadDummyAnimations(Enemy enemy) throws IOException {
        // Load dummy idle animation frames
        BufferedImage[] dummyFrames = loadAnimation("resources\\Enemies\\Dummy\\Dummy", 3);
        
        // Set initial frame
        enemy.enemyCurrentFrame = dummyFrames[0];
    }

    private static void loadBanditAnimations(Enemy enemy) throws IOException {
        // Movement animations
        enemy.upMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Back\\bandit_back", 8);
        enemy.downMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Front\\bandit_front", 8);
        enemy.leftMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Left\\bandit_left", 8);
        enemy.rightMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Right\\bandit_right", 8);
        enemy.upLeftMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Upper_Left\\bandit_U_left", 8);
        enemy.upRightMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Upper_Right\\bandit_U_right", 8);
        enemy.downLeftMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Lower_Left\\bandit_L_left", 8);
        enemy.downRightMove = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Movement\\Lower_Right\\bandit_L_right", 8);
        
        // Melee animations
        enemy.upMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\back\\b_back", 4);
        enemy.downMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\front\\b_front", 4);
        enemy.leftMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\left\\b_left", 4);
        enemy.rightMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\right\\b_right", 4);
        enemy.upLeftMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\upper_left\\b_Uleft", 4);
        enemy.upRightMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\upper_right\\b_Uright", 4);
        enemy.downLeftMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\lower_left\\b_LLeft", 4);
        enemy.downRightMelee = loadAnimation("resources\\Enemies\\Bandit\\Bandit_Attack\\lower_right\\b_Lright", 4);
        
        // Set initial frame
        enemy.enemyCurrentFrame = enemy.downMove != null ? enemy.downMove[3] : null;
    }
    
    private static void loadHighMageAnimations(Enemy enemy) throws IOException {
        // Movement animations
        enemy.upMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\back\\hm_back", 8);
        enemy.downMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\front\\hm_front", 8);
        enemy.leftMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\left\\hm_left", 8);
        enemy.rightMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\right\\hm_right", 8);
        enemy.upLeftMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\upper_left\\hm_ULeft", 8);
        enemy.upRightMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\upper_right\\hm_URight", 8);
        enemy.downLeftMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\lower_left\\hm_LLeft", 8);
        enemy.downRightMove = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Walking\\lower_right\\hm_LRight", 8);
        
        // Ranged attack animations
        enemy.upRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\back\\Hm_back", 5);
        enemy.downRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\front\\Hm_front", 5);
        enemy.leftRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\left\\Hm_Left", 5);
        enemy.rightRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\right\\Hm_Right", 5);
        enemy.upLeftRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\upper_left\\Hm_ULeft", 5);
        enemy.upRightRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\upper_right\\Hm_URight", 5);
        enemy.downLeftRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\lower_left\\Hm_LLeft", 5);
        enemy.downRightRange = loadAnimation("resources\\Enemies\\High_Mage\\High_Mage_Attack\\lower_right\\Hm_LRight", 5);
        
        // Set initial frame
        enemy.enemyCurrentFrame = enemy.downMove != null ? enemy.downMove[3] : null;
    }

    private static void loadMusketeerAnimations(Enemy enemy) throws IOException {
        // Movement animations
        enemy.upMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\back\\musk_back", 8);
        enemy.downMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\front\\musk_front", 8);
        enemy.leftMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\left\\musk_left", 8);
        enemy.rightMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\right\\musk_right", 8);
        enemy.upLeftMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\upper_left\\musk_Uleft", 8);
        enemy.upRightMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\upper_right\\musk_URight", 8);
        enemy.downLeftMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\lower_left\\musk_LLeft", 8);
        enemy.downRightMove = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Walking\\lower_right\\musk_LRight", 8);
        
        // Melee attack animations
        enemy.upMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\back\\M_back", 4);
        enemy.downMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\front\\M_front", 4);
        enemy.leftMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\left\\M_left", 4);
        enemy.rightMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\right\\M_right", 4);
        enemy.upLeftMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\upper_left\\M_ULeftt", 4);
        enemy.upRightMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\upper_right\\M_URight", 4);
        enemy.downLeftMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\lower_left\\M_LLeft", 4);
        enemy.downRightMelee = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Melee\\lower_right\\M_LRight", 4);
        
        // Range attack animations
        enemy.upRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\back\\M_back", 4);
        enemy.downRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\front\\M_front", 4);
        enemy.leftRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\left\\M_left", 4);
        enemy.rightRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\right\\M_Right", 4);
        enemy.upLeftRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\upper_left\\M_ULeft", 4);
        enemy.upRightRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\upper_right\\M_URight", 4);
        enemy.downLeftRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\lower_left\\M_LLeft", 4);
        enemy.downRightRange = loadAnimation("resources\\Enemies\\Musketeer\\Musketeer_Attack\\Ranged\\lower_right\\M_LRight", 4);
        
        // Set initial frame
        enemy.enemyCurrentFrame = enemy.downMove != null ? enemy.downMove[3] : null;
    }

    private static BufferedImage[] loadAnimation(String basePath, int frameCount) throws IOException {
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            String path = basePath + (i + 1) + ".png";
            frames[i] = ImageIO.read(new File(path));
        }
        return frames;
    }

    // ========================================
    // EXAMPLE: HOW TO ADD A NEW ENEMY TYPE
    // ========================================
    
    /**
     * TEMPLATE for creating a new enemy type
     * Copy this method and modify values to create new enemies!
     * Also add animation loading as needed
     * Call this method from GamePanel to spawn the enemy in-game
     */
    public static Enemy createCustomEnemy(int x, int y) throws IOException {
        Enemy custom = new Enemy(x, y);
        
        // Step 1: Configure basic stats
        custom.setEnemy("Custom Enemy Name")
              .setStats(
                  100,    // maxHealth
                  100,     // maxMana
                  1.0f    // speed (1.0 = normal, 2.0 = double speed, 0.5 = half speed)
              );
        
        // Step 2: Configure combat ranges
        custom.setCombatRanges(
            300,    // sightRange - how far enemy can see
            40,     // meleeRangeEnter - distance to start melee attack
            60,     // meleeRangeExit - distance to stop melee attack (must be > enter)
            200,    // rangeAttackEnter - distance to start ranged attack
            150     // rangeAttackExit - distance to stop ranged attack (must be < enter)
        );
        
        // Step 3: Configure damage values
        custom.setDamage(
            5,      // meleeDamage
            8       // rangeDamage
        );
        
        // Step 4: Load animations (create your own or reuse existing)
        loadBanditAnimations(custom);  // Reusing bandit sprites
        
        return custom;
    }
}