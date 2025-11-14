import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class Enemy {

    // ========================================
    // ENEMY CORE PROPERTIES
    // ========================================

    private String enemyType = "Enemy";
    private int x, y;
    private int spawnX, spawnY;
    
    /** Enemy sprite dimensions (default if they are 64x64 canvas size) */
    private final int width = 192;
    private final int height = 192;

    /** Custom rendering (adjustable per enemy type) */
    private boolean useCustomEnemy = false;
    private int customWidth = 0;
    private int customHeight = 0;
    private boolean hideHealthBars = false;
    
    /** Main collision hitbox */
    private Rectangle bodyHitbox;

    /** Body hitbox size (adjustable per enemy type) */
    private int bodyHitboxWidth = 30;
    private int bodyHitboxHeight = 21;
    private int bodyHitboxOffsetY = 0;
    
    // ========================================
    // STATS (Configurable per enemy type)
    // ========================================
    
    private int maxHealth = 0;
    private int currentHealth = 0;
    private int maxMana = 0;
    private int currentMana = 0;
    private float baseSpeed = 0f;
    private boolean alive = true;

    // Auto-respawn system (for training dummies)
    private boolean autoRespawn = false;
    private long respawnDelay = 0;
    private long deathTime = 0;

    // ========================================
    // COMBAT RANGES (Configurable per enemy type)
    // ========================================
    
    private int sightRange = 0;
    private int meleeRangeEnter = 0;
    private int meleeRangeExit = 0;
    private int rangeAttackRangeEnter = 0;
    private int rangeAttackRangeExit = 0;

    // ========================================
    // ATTACK DAMAGE VALUES (Configurable per enemy type)
    // ========================================
    
    private int meleeDamage = 0;
    private int rangeDamage = 0;
    
    // ========================================
    // AI STATE MACHINE
    // ========================================
    
    public enum State { PATROL, CHASE, MELEE_ATTACK, RANGE_ATTACK, IDLE, SEARCH }
    private State state = State.PATROL;

    // ========================================
    // PLAYER TRACKING
    // ========================================
    
    private Rectangle playerHitbox;
    private Point lastSeenPosition = null;
    private boolean hasSeenPlayer = false;
    private boolean wasSeeingPlayer = false;
    private boolean canSeePlayer = false;

    // ========================================
    // BREADCRUMB TRAIL SYSTEM
    // ========================================
    
    private List<Point> breadcrumbTrail;
    private Point currentBreadcrumbTarget = null;
    private long breadcrumbTimestamp = 0;
    private final long breadcrumbExpireTime = 5000;
    private final int breadcrumbAcceptRadius = 800;
    private int breadcrumbsFollowed = 0;
    private final int maxBreadcrumbsToFollow = 3;

    // ========================================
    // SEARCH BEHAVIOR
    // ========================================
    
    private boolean isSearching = false;
    private Point searchTarget = null;
    private int searchAttempts = 0;
    private final int maxSearchAttempts = 3;
    private long searchStartTime = 0;
    private final long searchDuration = 10000;
    private boolean searchCompleted = false;
    
    // Search pausing behavior
    private boolean isSearchPausing = false;
    private long searchPauseUntil = 0;
    private final long searchPauseMin = 1000;
    private final long searchPauseMax = 1500;
    private long searchMoveStartTime = 0;
    private long searchMoveDuration = 0;
    private final long searchMoveMin = 100;
    private final long searchMoveMax = 1000;

    // ========================================
    // PATROL BEHAVIOR
    // ========================================
    
    private Rectangle patrolArea;
    private Point randomTarget;
    private long waitAtWaypointUntil = 0;

    // ========================================
    // MOVEMENT SYSTEM
    // ========================================
    
    private float vx = 0, vy = 0;
    
    // Stuck detection
    private int stuckCounter = 0;
    private int lastX, lastY;
    private long lastMoveTime = System.currentTimeMillis();
    
    // Force step back system
    private int consecutiveStuckFrames = 0;
    private final int stuckThreshold = 30;
    private boolean isForcingStepBack = false;
    private int stepBackFramesRemaining = 0;
    private float stepBackVx = 0, stepBackVy = 0;
    private int lastStuckX = 0, lastStuckY = 0;

    // ========================================
    // MELEE ATTACK SYSTEM
    // ========================================
    
    private boolean isMeleeAttacking = false;
    private int meleeAttackFrameIndex = 0;
    private long meleeLastFrameTime = 0;
    private final int meleeSpawnFrame = 4;
    private final long meleeFrameInterval = 100;
    private boolean meleeSpawnedThisAttack = false;
    private boolean meleeDamageApplied = false;
    
    private boolean meleeOnCooldown = false;
    private long meleeLastTime = 0;
    private final long meleeCooldown = 1500;
    
    private Rectangle meleeHitbox = null;
    private int meleeHitboxWidth = 30;
    private int meleeHitboxHeight = 30;
    private int meleeHitboxOffset = 20;

    // ========================================
    // RANGE ATTACK SYSTEM
    // ========================================
    
    private boolean isRangeAttacking = false;
    private int rangeAttackFrameIndex = 0;
    private long rangeLastFrameTime = 0;
    private final long rangeFrameInterval = 200;
    private int projectileSpawnFrame = 4;
    private boolean projectileSpawnedThisAttack = false;
    
    private boolean rangeOnCooldown = false;
    private long rangeLastTime = 0;
    private final long rangeCooldown = 2000;
    private final int rangeManaCost = 5;
    
    public final List<EnemyProjectile> projectiles = new ArrayList<>();
    private final int projectileSpeed = 4;
    private final int projectileRange = 200;

    // ========================================
    // ANIMATION SYSTEM
    // ========================================
    
    private String currentDirection = "down";
    public BufferedImage[] upMove, downMove, leftMove, rightMove;
    public BufferedImage[] upLeftMove, upRightMove, downLeftMove, downRightMove;
    public BufferedImage[] upMelee, downMelee, leftMelee, rightMelee;
    public BufferedImage[] upLeftMelee, upRightMelee, downLeftMelee, downRightMelee;
    public BufferedImage[] upRange, downRange, leftRange, rightRange;
    public BufferedImage[] upLeftRange, upRightRange, downLeftRange, downRightRange;
    public BufferedImage enemyCurrentFrame;
    private BufferedImage[] currentAnimation;
    private int frameIndex = 0;
    @SuppressWarnings("unused")
    private int frameCounter = 0;
    private long lastFrameTime = System.currentTimeMillis();
    private final long frameInterval = 100;

    // ========================================
    // IDLE BEHAVIOR
    // ========================================
    
    private long nextLookTime = 0;
    private final long lookIntervalMin = 500;
    private final long lookIntervalMax = 1000;

    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.spawnX = startX;
    this.spawnY = startY;
        this.state = State.PATROL;
        initHitbox();
    }
    
    private void initHitbox() {

        if (useCustomEnemy) {
            bodyHitbox = new Rectangle(
            x + (width - bodyHitboxWidth) / 2,
            y + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth,
                bodyHitboxHeight
            );
            return;
        }
        bodyHitbox = new Rectangle(
            x + (width - bodyHitboxWidth) / 2,
            y + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth,
            bodyHitboxHeight
        );
    }

    // ========================================
    // CONFIGURATION METHODS (For creating different enemy types)
    // ========================================
    
    public Enemy setStats(int health, int mana, float speed) {
        this.maxHealth = health;
        this.currentHealth = health;
        this.maxMana = mana;
        this.currentMana = mana;
        this.baseSpeed = speed;
        return this;
    }
    
    public Enemy setCombatRanges(int sight, int meleeEnter, int meleeExit, 
                                  int rangeEnter, int rangeExit) {
        this.sightRange = sight;
        this.meleeRangeEnter = meleeEnter;
        this.meleeRangeExit = meleeExit;
        this.rangeAttackRangeEnter = rangeEnter;
        this.rangeAttackRangeExit = rangeExit;
        return this;
    }
    
    public Enemy setDamage(int melee, int range) {
        this.meleeDamage = melee;
        this.rangeDamage = range;
        return this;
    }
    
    public Enemy setEnemy(String type) {
        this.enemyType = type;
        return this;
    }
    
    public void setPatrolArea(int x, int y, int width, int height) {
        this.patrolArea = new Rectangle(x, y, width, height);
    }

    public Enemy setAutoRespawn(boolean enabled, long delay) {
        this.autoRespawn = enabled;
        this.respawnDelay = delay;
        return this;
    }

    public Enemy setCustomSize(int width, int height) {
        this.useCustomEnemy = true;
        this.customWidth = width;
        this.customHeight = height;
        return this;
    }

    public Enemy setCustomHitbox(int width, int height, int offsetY) {
        this.useCustomEnemy = true;
        this.bodyHitboxWidth = width;
        this.bodyHitboxHeight = height;
        this.bodyHitboxOffsetY = offsetY;
        // Reinitialize hitbox with new dimensions
        initHitbox();
        return this;
    }

    public Enemy setHideHealthBars(boolean hide) {
        this.hideHealthBars = hide;
        return this;
    }

    public Enemy setProjectileSpawnFrame(int frame) {
        this.projectileSpawnFrame = frame;
        return this;
    }

    // ========================================
    // MAIN UPDATE LOOP
    // ========================================
    
    public void update(GamePanel gp, Collision collision, Rectangle playerHitbox, Player player, ScreenShake screenShake) {
        this.playerHitbox = playerHitbox;
        // Handle respawn if enabled
        if (!alive) {
            if (autoRespawn) {
                long timeSinceDeath = System.currentTimeMillis() - deathTime;
                if (timeSinceDeath >= respawnDelay) {
                    respawn();
                }
            }
            return;
        }
        
        // Store position before update (for stuck detection)
        int posBeforeX = x;
        int posBeforeY = y;
        
        // Handle forced step back (highest priority)
        if (isForcingStepBack) {
            executeStepBack(collision);
            updateHitbox();
            return;
        }
        
        // Calculate distance to player
        double dx = (playerHitbox.x + playerHitbox.width/2) - (x + width/2);
        double dy = (playerHitbox.y + playerHitbox.height/2) - (y + height/2);
        double distToPlayer = Math.sqrt(dx*dx + dy*dy);
        
        // Update vision and tracking
        updatePlayerTracking(collision, distToPlayer);
        
        // Determine AI state
        determineState(distToPlayer);
        
        // Update melee attack animation if active
        if (isMeleeAttacking) {
            updateMeleeAttackAnimation(playerHitbox, gp, player, screenShake);
        }
        
        // Update range attack animation if active
        if (isRangeAttacking) {
            updateRangeAttackAnimation();
        }
        
        // Execute current state behavior
        executeStateBehavior(collision);
        
        // Handle idle looking around
        handleIdleLook();
        
        // Update cooldowns
        updateCooldowns();
        
        // Regenerate mana
        regenerateMana();
        
        // Apply movement
        moveWithCollision(collision);
        
        // Check if stuck
        checkStuckStatus(posBeforeX, posBeforeY);
        
        // Update projectiles
        updateProjectiles(collision);
        
        // Update hitbox position
        updateHitbox();
    }

    // ========================================
    // PLAYER TRACKING
    // ========================================
    
    private void updatePlayerTracking(Collision collision, double distToPlayer) {
        // Check if player is visible
        canSeePlayer = (distToPlayer <= sightRange) && hasLineOfSight(
            collision,
            x + width/2, y + height/2,
            playerHitbox.x + playerHitbox.width/2,
            playerHitbox.y + playerHitbox.height/2
        );
        
        // Update tracking state
        if (canSeePlayer) {
            hasSeenPlayer = true;
            lastSeenPosition = new Point(
                playerHitbox.x + playerHitbox.width/2,
                playerHitbox.y + playerHitbox.height/2
            );
            
            // Clear breadcrumb trail when player is visible
            if (breadcrumbTrail != null) {
                breadcrumbTrail.clear();
                breadcrumbTrail = null;
            }
            currentBreadcrumbTarget = null;
            isSearching = false;
            searchTarget = null;
            breadcrumbsFollowed = 0;
            searchCompleted = false;
        } else {
            // Player just went out of sight
            if (wasSeeingPlayer) {
                breadcrumbsFollowed = 0;
                searchCompleted = false;
                currentBreadcrumbTarget = null;
            }
        }
        
        wasSeeingPlayer = canSeePlayer;
        
        // Expire old breadcrumbs
        if (breadcrumbTrail != null && !breadcrumbTrail.isEmpty()) {
            if (System.currentTimeMillis() - breadcrumbTimestamp > breadcrumbExpireTime) {
                breadcrumbTrail.clear();
                breadcrumbTrail = null;
                currentBreadcrumbTarget = null;
            }
        }
    }
    
    public void setBreadcrumbTrail(List<Point> trail) {
        if (canSeePlayer || trail == null || trail.isEmpty()) return;
        
        List<Point> copy = new ArrayList<>(trail);
        
        // Trim trail to start from closest breadcrumb to last seen position
        if (lastSeenPosition != null) {
            int bestIdx = 0;
            double bestDist = Double.MAX_VALUE;
            
            for (int i = 0; i < copy.size(); i++) {
                Point p = copy.get(i);
                double d = Math.hypot(p.x - lastSeenPosition.x, p.y - lastSeenPosition.y);
                if (d < bestDist) {
                    bestDist = d;
                    bestIdx = i;
                }
            }
            
            if (bestIdx > 0 && bestIdx < copy.size()) {
                copy = new ArrayList<>(copy.subList(bestIdx, copy.size()));
            }
        }
        
        // Only accept trail if it starts nearby (if never saw player)
        if (!hasSeenPlayer) {
            if (!copy.isEmpty()) {
                Point start = copy.get(0);
                double dx = start.x - (x + width/2);
                double dy = start.y - (y + height/2);
                double d = Math.hypot(dx, dy);
                if (d > breadcrumbAcceptRadius) {
                    return;
                }
            }
        }
        
        this.breadcrumbTrail = copy;
        this.breadcrumbTimestamp = System.currentTimeMillis();
    }

    // ========================================
    // STATE DETERMINATION
    // ========================================
    
    private void determineState(double distToPlayer) {
        long now = System.currentTimeMillis();
        
        // Animation locks override state
        if (isMeleeAttacking) {
            state = State.MELEE_ATTACK;
            return;
        }
        
        if (isRangeAttacking) {
            state = State.RANGE_ATTACK;
            return;
        }
        
        // Player visible - choose attack or chase
        if (canSeePlayer) {
            // Check if this enemy can melee (meleeRangeEnter > 1)
            boolean canMelee = meleeRangeEnter > 1;
            
            // Check if enemy can ranged attack (rangeAttackRangeEnter > 0)
            boolean canRange = rangeAttackRangeEnter > 0;
            
            // NEW: Check if enemy has enough mana for ranged attack
            boolean hasMana = currentMana >= rangeManaCost;

            // CRITICAL: Check if ranged-only enemy is too close and needs to flee
            if (!canMelee && canRange && distToPlayer < rangeAttackRangeExit) {
                // Ranged-only enemy too close - needs to kite away
                state = State.RANGE_ATTACK;  // Use RANGE_ATTACK state to handle kiting
                return;
            }

            // Prioritize melee if low on mana (for hybrid enemies)
            if (canMelee && canRange && !hasMana) {
                // Low mana, switch to melee mode
                if (distToPlayer <= meleeRangeEnter) {
                    state = State.MELEE_ATTACK;
                } else {
                    // Need to get closer for melee
                    state = State.CHASE;
                }
                return;
            }
                
            // Normal attack logic
            if (canMelee && distToPlayer <= meleeRangeEnter) {
                state = State.MELEE_ATTACK;
            } else if (distToPlayer >= rangeAttackRangeExit && 
                       distToPlayer <= rangeAttackRangeEnter &&
                       (now - meleeLastTime >= 1200) &&
                       rangeAttackRangeEnter > 0) { // Check if range attack is enabled
                state = State.RANGE_ATTACK;
            } else {
                state = State.CHASE;
            }
            return;
        }
        
        // Player not visible - follow breadcrumbs, search, or patrol
        if (searchCompleted) {
            state = State.PATROL;
        } else if (isSearching) {
            state = State.SEARCH;
        } else if (breadcrumbTrail != null && !breadcrumbTrail.isEmpty() && 
                   breadcrumbsFollowed < maxBreadcrumbsToFollow) {
            state = State.CHASE;
        } else if (patrolArea != null) {
            state = State.PATROL;
        } else {
            state = State.IDLE;
        }
    }

    // ========================================
    // STATE EXECUTION
    // ========================================
    
    private void executeStateBehavior(Collision collision) {
        switch (state) {
            case PATROL -> doPatrol(collision);
            case CHASE -> doChaseWithBreadcrumbs(collision);
            case SEARCH -> doSearch(collision);
            case MELEE_ATTACK -> doMelee(playerHitbox);
            case RANGE_ATTACK -> doRange(playerHitbox);
            case IDLE -> { vx = vy = 0; setIdle(); }
        }
        
        // Generate new patrol target if needed
        if (state == State.PATROL && randomTarget == null && 
            System.currentTimeMillis() >= waitAtWaypointUntil) {
            generatePatrolTarget();
        }
    }

    // ========================================
    // PATROL BEHAVIOR
    // ========================================
    
    private void doPatrol(Collision collision) {
        if (patrolArea == null) {
            vx = vy = 0;
            return;
        }
        
        long now = System.currentTimeMillis();
        
        // Wait at waypoint
        if (now < waitAtWaypointUntil) {
            vx = vy = 0;
            setIdle();
            return;
        }
        
        // Check if reached target
        if (randomTarget == null || reachedTarget(randomTarget, 8)) {
            generatePatrolTarget();
            waitAtWaypointUntil = now + 1000 + (long)(Math.random() * 2000);
            vx = vy = 0;
            setIdle();
            return;
        }
        
        // Move toward target
        moveToward(randomTarget, baseSpeed);
    }
    
    private void generatePatrolTarget() {
        if (patrolArea == null) return;
        randomTarget = new Point(
            patrolArea.x + (int)(Math.random() * patrolArea.width),
            patrolArea.y + (int)(Math.random() * patrolArea.height)
        );
    }

    // ========================================
    // CHASE BEHAVIOR
    // ========================================

    private void doChaseWithBreadcrumbs(Collision collision) {
        // Direct chase if player visible
        if (canSeePlayer) {
            breadcrumbTrail = null;
            currentBreadcrumbTarget = null;
            isSearching = false;
            searchTarget = null;
            breadcrumbsFollowed = 0;
            searchCompleted = false;
            
            Point playerCenter = new Point(
                playerHitbox.x + playerHitbox.width/2,
                playerHitbox.y + playerHitbox.height/2
            );

            // Always chase towards player in CHASE state
        moveToward(playerCenter, baseSpeed);
        return;
    }
        
        // Return to patrol if search complete
        if (searchCompleted) {
            state = State.PATROL;
            vx = vy = 0;
            doPatrol(collision);
            return;
        }
        
        // Continue searching if in search mode
        if (isSearching) {
            doSearch(collision);
            return;
        }
        
        // Follow breadcrumbs
        if (breadcrumbTrail != null && !breadcrumbTrail.isEmpty() && 
            breadcrumbsFollowed < maxBreadcrumbsToFollow) {
            followBreadcrumbs(collision);
        } else {
            // No breadcrumbs - enter search or patrol
            breadcrumbTrail = null;
            currentBreadcrumbTarget = null;
            
            if (hasSeenPlayer) {
                enterSearchMode(collision);
            } else {
                state = State.PATROL;
                vx = vy = 0;
            }
        }
    }
    
    private void followBreadcrumbs(Collision collision) {
        // Initialize target
        if (currentBreadcrumbTarget == null && !breadcrumbTrail.isEmpty()) {
            currentBreadcrumbTarget = breadcrumbTrail.get(0);
        }
        
        if (currentBreadcrumbTarget == null) return;
        
        double dist = distanceTo(currentBreadcrumbTarget);
        
        // Skip if stuck near breadcrumb
        if (dist < 30 && stuckCounter > 1) {
            advanceBreadcrumb(collision);
            return;
        }
        
        // Move toward breadcrumb
        if (dist > 5) {
            moveTowardWithObstacleAvoidance(currentBreadcrumbTarget, baseSpeed, collision);
            handleStuckAvoidance(collision);
        } else {
            // Reached breadcrumb
            advanceBreadcrumb(collision);
        }
    }
    
    private void advanceBreadcrumb(Collision collision) {
        if (!breadcrumbTrail.isEmpty()) {
            breadcrumbTrail.remove(0);
        }
        breadcrumbsFollowed++;
        currentBreadcrumbTarget = !breadcrumbTrail.isEmpty() ? breadcrumbTrail.get(0) : null;
        stuckCounter = 0;
        
        if (breadcrumbsFollowed >= maxBreadcrumbsToFollow || currentBreadcrumbTarget == null) {
            breadcrumbTrail = null;
            currentBreadcrumbTarget = null;
            enterSearchMode(collision);
        }
    }

    // ========================================
    // SEARCH BEHAVIOR
    // ========================================
    
    private void enterSearchMode(Collision collision) {
        isSearching = true;
        searchAttempts = 0;
        searchStartTime = System.currentTimeMillis();
        searchTarget = null;
        stuckCounter = 0;
        searchCompleted = false;
        
        // Initialize pause behavior
        isSearchPausing = false;
        searchMoveStartTime = System.currentTimeMillis();
        searchMoveDuration = searchMoveMin + 
            (long)(Math.random() * (searchMoveMax - searchMoveMin));
        
        // Clear breadcrumbs
        breadcrumbTrail = null;
        currentBreadcrumbTarget = null;
        
        // Set search origin
        lastSeenPosition = new Point(x + width/2, y + height/2);
        
        // Generate first search target
        searchTarget = generateSearchTarget(collision);
    }
    
    private void doSearch(Collision collision) {
        long now = System.currentTimeMillis();
        
        // Check timeout
        if (now - searchStartTime > searchDuration) {
            completeSearch();
            return;
        }
        
        // Handle pausing
        if (isSearchPausing) {
            if (now < searchPauseUntil) {
                vx = vy = 0;
                setIdle();
                return;
            } else {
                // Resume from pause - generate new target
                isSearchPausing = false;
                searchMoveStartTime = now;
                searchMoveDuration = searchMoveMin + 
                    (long)(Math.random() * (searchMoveMax - searchMoveMin));
                searchTarget = generateSearchTarget(collision); // NEW TARGET after pause
            }
        }
        
        // Check if time to pause (only if actively moving)
        if (!isSearchPausing && (now - searchMoveStartTime >= searchMoveDuration)) {
            startSearchPause(now);
            return;
        }
        
        // Generate new target if needed
        if (searchTarget == null || reachedTarget(searchTarget, 15) ||
            isTargetBlocked(searchTarget, collision)) {
            searchAttempts++;
            
            if (searchAttempts > maxSearchAttempts) {
                completeSearch();
                return;
            }
            
            searchTarget = generateSearchTarget(collision);
            searchMoveStartTime = now;
            searchMoveDuration = searchMoveMin + 
                (long)(Math.random() * (searchMoveMax - searchMoveMin));
        }
        
        // Move toward search target
        if (searchTarget != null && !isSearchPausing) {
            moveTowardWithObstacleAvoidance(searchTarget, baseSpeed, collision);
            handleStuckAvoidance(collision); // helps detect stuck during search
        }
    }
    
    private void startSearchPause(long now) {
        isSearchPausing = true;
        long pauseDuration = searchPauseMin + 
            (long)(Math.random() * (searchPauseMax - searchPauseMin));
        searchPauseUntil = now + pauseDuration;
        vx = vy = 0;
        setIdle();
    }
    
    private void completeSearch() {
        isSearching = false;
        searchTarget = null;
        searchCompleted = true;
        isSearchPausing = false;
        state = State.PATROL;
        randomTarget = null;
        vx = vy = 0;
    }
    
    private Point generateSearchTarget(Collision collision) {
        // Increase search radius with each attempt
        int baseRadius = 100 + (searchAttempts * 80);
        int maxRadius = Math.min(baseRadius, 400);
        
        for (int tries = 0; tries < 20; tries++) {
            // Fully random angle - no bias
            double angle = Math.random() * Math.PI * 2;
            
            // Vary distance more (between 50% and 100% of max radius)
            double distancePercent = 0.5 + (Math.random() * 0.5);
            double distance = maxRadius * distancePercent;
            
            int offsetX = (int)(Math.cos(angle) * distance);
            int offsetY = (int)(Math.sin(angle) * distance);
            
            // Use current position as origin (not last seen position)
            Point candidate = new Point(x + width/2 + offsetX, y + height/2 + offsetY);
            
            if (!isTargetBlocked(candidate, collision)) {
                return candidate;
            }
        }
        
        // Fallback - completely random nearby point
        return new Point(
            x + width/2 + (int)(Math.random() * 300 - 150),
            y + height/2 + (int)(Math.random() * 300 - 150)
        );
    }

    // ========================================
    // MELEE ATTACK
    // ========================================
    
    private void doMelee(Rectangle targetHitbox) {
        long now = System.currentTimeMillis();
        
        double dx = (targetHitbox.x + targetHitbox.width / 2) - (x + width / 2);
        double dy = (targetHitbox.y + targetHitbox.height / 2) - (y + height / 2);
        double dist = Math.sqrt(dx*dx + dy*dy);
        
        // Exit if player out of range
        if (!isMeleeAttacking && dist > meleeRangeExit) {
            state = State.CHASE;
            setIdle();
            return;
        }
        
        // Start new attack
        if (!isMeleeAttacking && !meleeOnCooldown && dist <= meleeRangeEnter) {
            startMeleeAttack(now);
        }
        
        // Stop movement during attack
        vx = vy = 0;
        
        // Face player
        if (!isMeleeAttacking) {
            faceTarget(dx, dy);
            meleeHitbox = null;
            setIdle();
        }
    }
    
    private void startMeleeAttack(long now) {
        isMeleeAttacking = true;
        meleeAttackFrameIndex = 0;
        meleeLastFrameTime = now;
        meleeSpawnedThisAttack = false;
        meleeDamageApplied = false;
        meleeLastTime = now;
        meleeOnCooldown = true;
    }
    
    private void updateMeleeAttackAnimation(Rectangle targetHitbox, GamePanel gp, Player player, ScreenShake screenShake) {
        if (!isMeleeAttacking) return;
        
        BufferedImage[] attackFrames = getMeleeAnimationFrames();
        if (attackFrames == null) {
            isMeleeAttacking = false;
            return;
        }
        
        long now = System.currentTimeMillis();

        // Check if enough time has passed to advance frame
        if (now - meleeLastFrameTime >= meleeFrameInterval) {
            meleeLastFrameTime = now;
            meleeAttackFrameIndex++;
            
            // Spawn hitbox at specific frame
            if (!meleeSpawnedThisAttack && meleeAttackFrameIndex == meleeSpawnFrame) {
                createMeleeHitbox(targetHitbox, gp, player, screenShake);
                meleeSpawnedThisAttack = true;
            }
            
            // Animation finished
            if (meleeAttackFrameIndex >= attackFrames.length) {
                meleeAttackFrameIndex = 0;
                isMeleeAttacking = false;
                meleeSpawnedThisAttack = false;
                meleeDamageApplied = false;
                setIdle();
                return;
            }
            
            enemyCurrentFrame = attackFrames[meleeAttackFrameIndex];
        }
    }
    
    private BufferedImage[] getMeleeAnimationFrames() {
        return switch (currentDirection) {
            case "up" -> upMelee;
            case "down" -> downMelee;
            case "left" -> leftMelee;
            case "right" -> rightMelee;
            case "up-left" -> upLeftMelee;
            case "up-right" -> upRightMelee;
            case "down-left" -> downLeftMelee;
            case "down-right" -> downRightMelee;
            default -> null;
        };
    }
    
    private void createMeleeHitbox(Rectangle targetHitbox, GamePanel gp, Player player, ScreenShake screenShake) {
        if (!isMeleeAttacking) {
            meleeHitbox = null;
            meleeDamageApplied = false;
            return;
        }
        
        int hitboxX = x + width / 2 - meleeHitboxWidth / 2;
        int hitboxY = y + height / 2 - meleeHitboxHeight / 2;
        
        // Offset based on direction
        switch (currentDirection) {
            case "up" -> hitboxY -= meleeHitboxOffset;
            case "down" -> hitboxY += meleeHitboxOffset;
            case "left" -> hitboxX -= meleeHitboxOffset;
            case "right" -> hitboxX += meleeHitboxOffset;
            case "up-left" -> { hitboxX -= meleeHitboxOffset; hitboxY -= meleeHitboxOffset; }
            case "up-right" -> { hitboxX += meleeHitboxOffset; hitboxY -= meleeHitboxOffset; }
            case "down-left" -> { hitboxX -= meleeHitboxOffset; hitboxY += meleeHitboxOffset; }
            case "down-right" -> { hitboxX += meleeHitboxOffset; hitboxY += meleeHitboxOffset; }
        }
        
        meleeHitbox = new Rectangle(hitboxX, hitboxY, meleeHitboxWidth, meleeHitboxHeight);
        
        // Apply damage once per swing
        if (!meleeDamageApplied && meleeHitbox.intersects(targetHitbox)) {
            player.cranetakeDamage(meleeDamage, screenShake);
            meleeDamageApplied = true;
        }
    }

    // ========================================
    // RANGE ATTACK
    // ========================================
    
    private void doRange(Rectangle targetHitbox) {
        float dx = (targetHitbox.x + targetHitbox.width / 2f) - (x + width / 2f);
        float dy = (targetHitbox.y + targetHitbox.height / 2f) - (y + height / 2f);
        double distToPlayer = Math.sqrt(dx*dx + dy*dy);
        
        // Check if enemy should move away
        boolean canMelee = meleeRangeEnter > 1;
        if (!canMelee && distToPlayer < rangeAttackRangeExit) {
            // TOO CLOSE! Move away from player
            moveAwayFromTarget(targetHitbox, (rangeAttackRangeEnter + rangeAttackRangeExit) / 2);
            
            // Still try to attack while moving if possible
            long now = System.currentTimeMillis();
            if (!isRangeAttacking && !rangeOnCooldown && 
                now - rangeLastTime >= rangeCooldown && currentMana >= rangeManaCost) {
                faceTarget(dx, dy);
                startRangeAttack(now);
            }
            
            if (isRangeAttacking) {
                updateRangeAttackAnimation();
            }
            return;
        }

        vx = vy = 0;
        
        // Don't restart if already attacking
        if (isRangeAttacking) {
            updateRangeAttackAnimation();
            return;
        }
        
        // Face player even during cooldown
        if (!isRangeAttacking) {
            faceTarget(dx, dy);
        }
        
        // Start new attack
        long now = System.currentTimeMillis();
        if (!rangeOnCooldown && now - rangeLastTime >= rangeCooldown && currentMana >= rangeManaCost) {
            startRangeAttack(now);
        }
        
        updateRangeAttackAnimation();
    }
    
    private void startRangeAttack(long now) {
        isRangeAttacking = true;
        rangeAttackFrameIndex = 0;
        rangeLastFrameTime = now;
        projectileSpawnedThisAttack = false;
        rangeLastTime = now;
        rangeOnCooldown = true;
        useMana(rangeManaCost);
    }
    
    private void updateRangeAttackAnimation() {
        if (!isRangeAttacking) return;
        
        // Use range animations if available, otherwise use movement animations
        BufferedImage[] attackFrames = getRangeAnimationFrames();
        if (attackFrames == null) {
            attackFrames = getMovementAnimationFrames();
        }
        
        if (attackFrames == null) {
            isRangeAttacking = false;
            return;
        }
        
        long now = System.currentTimeMillis();
    
        // Check if enough time has passed to advance frame
        if (now - rangeLastFrameTime >= rangeFrameInterval) {
            rangeLastFrameTime = now;
            rangeAttackFrameIndex++;
            
            // Auto-calculate spawn frame or use configured value
            int calculatedSpawnFrame = calculateOptimalSpawnFrame(attackFrames.length);
            
            // Spawn projectile at specific frame
            if (!projectileSpawnedThisAttack && rangeAttackFrameIndex == calculatedSpawnFrame) {
                spawnProjectile(playerHitbox);
                projectileSpawnedThisAttack = true;
            }
            
            // Animation finished
            if (rangeAttackFrameIndex >= attackFrames.length) {
                rangeAttackFrameIndex = 0;
                isRangeAttacking = false;
                projectileSpawnedThisAttack = false;
                setIdle();
                return;
            }
            
            enemyCurrentFrame = attackFrames[rangeAttackFrameIndex];
        }
    }
    
    private BufferedImage[] getRangeAnimationFrames() {
        return switch (currentDirection) {
            case "up" -> upRange;
            case "down" -> downRange;
            case "left" -> leftRange;
            case "right" -> rightRange;
            case "up-left" -> upLeftRange;
            case "up-right" -> upRightRange;
            case "down-left" -> downLeftRange;
            case "down-right" -> downRightRange;
            default -> null;
        };
    }

    private void spawnProjectile(Rectangle targetHitbox) {
        int sx = x + width / 2;
        int sy = y + height / 2;
        float tx = targetHitbox.x + targetHitbox.width / 2f;
        float ty = targetHitbox.y + targetHitbox.height / 2f;
        
        float dx = tx - sx;
        float dy = ty - sy;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len != 0) {
            dx /= len;
            dy /= len;
        }
        
        EnemyProjectile p = new EnemyProjectile(sx, sy, dx, dy, projectileSpeed, projectileRange);
        projectiles.add(p);
    }

    private void moveAwayFromTarget(Rectangle targetHitbox, int optimalRange) {
        float dx = (targetHitbox.x + targetHitbox.width / 2f) - (x + width / 2f);
        float dy = (targetHitbox.y + targetHitbox.height / 2f) - (y + height / 2f);
        double distToPlayer = Math.sqrt(dx*dx + dy*dy);

        if (distToPlayer < optimalRange) {
            // Too close - move directly away from player
            if (distToPlayer > 0) {
                // Normalize direction and move away
                float dirX = (float)(-dx / distToPlayer);
                float dirY = (float)(-dy / distToPlayer);
                
                vx = dirX * baseSpeed * 0.4f;
                vy = dirY * baseSpeed * 0.4f;
                
                setDirectionAnimation(vx, vy);
            }
        } else if (distToPlayer > rangeAttackRangeEnter) {
            // Too far - move closer
            if (distToPlayer > 0) {
                float dirX = (float)(dx / distToPlayer);
                float dirY = (float)(dy / distToPlayer);
                
                vx = dirX * baseSpeed;
                vy = dirY * baseSpeed;
                
                setDirectionAnimation(vx, vy);
            }
        } else {   
            // Within optimal range - strafe around player
            if (distToPlayer > 0) {
                // Move perpendicular to player
                vx = (float)(-dy / distToPlayer) * baseSpeed  * 0.4f;
                vy = (float)(dx / distToPlayer) * baseSpeed * 0.4f;
                setDirectionAnimation(vx, vy);
            }
        }
    }

    private int calculateOptimalSpawnFrame(int totalFrames) {
        // If spawn frame was manually set and is valid, use it
        if (projectileSpawnFrame >= 0 && projectileSpawnFrame < totalFrames) {
            return projectileSpawnFrame;
        }
   
        if (totalFrames == 4) {
            return 3;
        } else  {
            return 4;
        }
    }

    // ========================================
    // MOVEMENT UTILITIES
    // ========================================
    
    private void moveToward(Point target, float speed) {
        double dx = target.x - (x + width / 2);
        double dy = target.y - (y + height / 2);
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 1) {
            vx = vy = 0;
            return;
        }
        
        vx = (float)(dx / dist) * speed;
        vy = (float)(dy / dist) * speed;
        setDirectionAnimation(vx, vy);
    }
    
    private void moveTowardWithObstacleAvoidance(Point target, float speed, Collision collision) {
        double dx = target.x - (x + width / 2);
        double dy = target.y - (y + height / 2);
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 1) {
            vx = vy = 0;
            return;
        }
        
        float desiredVx = (float)(dx / dist * speed);
        float desiredVy = (float)(dy / dist * speed);
        
        // Check if direct path is blocked
        int nextX = Math.round(x + desiredVx);
        int nextY = Math.round(y + desiredVy);
        
        Rectangle nextPos = new Rectangle(
            nextX + (width - bodyHitboxWidth) / 2,
            nextY + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth, bodyHitboxHeight
        );
        
        if (collision.checkCollision(nextPos)) {
            // Try X-only or Y-only movement
            Rectangle testX = new Rectangle(
                nextX + (width - bodyHitboxWidth) / 2,
                y + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            Rectangle testY = new Rectangle(
                x + (width - bodyHitboxWidth) / 2,
                nextY + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            
            if (!collision.checkCollision(testX)) {
                vx = desiredVx * 1.5f;
                vy = 0;
            } else if (!collision.checkCollision(testY)) {
                vx = 0;
                vy = desiredVy * 1.5f;
            } else {
                vx = desiredVx;
                vy = desiredVy;
            }
        } else {
            vx = desiredVx;
            vy = desiredVy;
        }
        
        setDirectionAnimation(vx, vy);
    }
    
    private void moveWithCollision(Collision collision) {
        if (vx == 0 && vy == 0) return;
        
        int nextX = Math.round(x + vx);
        int nextY = Math.round(y + vy);
        
        Rectangle nextBodyX = new Rectangle(
            nextX + (width - bodyHitboxWidth) / 2,
            y + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth, bodyHitboxHeight
        );
        Rectangle nextBodyY = new Rectangle(
            x + (width - bodyHitboxWidth) / 2,
            nextY + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth, bodyHitboxHeight
        );
        Rectangle nextBodyBoth = new Rectangle(
            nextX + (width - bodyHitboxWidth) / 2,
            nextY + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth, bodyHitboxHeight
        );
        
        boolean blockedX = collision.checkCollision(nextBodyX);
        boolean blockedY = collision.checkCollision(nextBodyY);
        boolean blockedBoth = collision.checkCollision(nextBodyBoth);
        
        // No collision
        if (!blockedBoth) {
            x = nextX;
            y = nextY;
            return;
        }
        
        // Try sliding
        if (blockedX && !blockedY) {
            y = nextY;
            trySlideX(nextY, collision);
            return;
        }
        
        if (blockedY && !blockedX) {
            x = nextX;
            trySlideY(nextX, collision);
            return;
        }
        
        // Try forced movement in various directions
        if (tryForcedMovement(collision)) {
            return;
        }
        
        stuckCounter += 2;
    }
    
    private void trySlideX(int nextY, Collision collision) {
        for (int slideX = 3; slideX >= 1; slideX--) {
            int testX = x + (vx > 0 ? slideX : -slideX);
            Rectangle slideRect = new Rectangle(
                testX + (width - bodyHitboxWidth) / 2,
                nextY + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            if (!collision.checkCollision(slideRect)) {
                x = testX;
                return;
            }
        }
    }
    
    private void trySlideY(int nextX, Collision collision) {
        for (int slideY = 3; slideY >= 1; slideY--) {
            int testY = y + (vy > 0 ? slideY : -slideY);
            Rectangle slideRect = new Rectangle(
                nextX + (width - bodyHitboxWidth) / 2,
                testY + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            if (!collision.checkCollision(slideRect)) {
                y = testY;
                return;
            }
        }
    }
    
    private boolean tryForcedMovement(Collision collision) {
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
            {2, 1}, {-2, 1}, {2, -1}, {-2, -1},
            {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
        };
        
        for (int[] dir : directions) {
            for (int magnitude = 3; magnitude >= 1; magnitude--) {
                int testX = x + (dir[0] * magnitude);
                int testY = y + (dir[1] * magnitude);
                
                Rectangle testRect = new Rectangle(
                    testX + (width - bodyHitboxWidth) / 2,
                    testY + (height - bodyHitboxHeight) / 2,
                    bodyHitboxWidth, bodyHitboxHeight
                );
                
                if (!collision.checkCollision(testRect)) {
                    x = testX;
                    y = testY;
                    return true;
                }
            }
        }
        
        return false;
    }

    // ========================================
    // STUCK DETECTION & RECOVERY
    // ========================================
    
    private void checkStuckStatus(int posBeforeX, int posBeforeY) {
        int distanceMoved = Math.abs(x - posBeforeX) + Math.abs(y - posBeforeY);
        
        // Only check if trying to move (not idle or attacking)
        if ((vx != 0 || vy != 0) && state != State.MELEE_ATTACK && state != State.RANGE_ATTACK) {
            if (distanceMoved < 1) {
                consecutiveStuckFrames++;
                
                if (consecutiveStuckFrames >= stuckThreshold) {
                    forceStepBack();
                }
            } else {
                consecutiveStuckFrames = 0;
            }
        } else {
            consecutiveStuckFrames = 0;
        }
    }
    
    private void handleStuckAvoidance(Collision collision) {
        long now = System.currentTimeMillis();
        
        if (now - lastMoveTime > 400) {
            int dx = Math.abs(x - lastX);
            int dy = Math.abs(y - lastY);
            int distanceMoved = dx + dy;
            
            if (distanceMoved < 2) {
                stuckCounter++;
            } else {
                stuckCounter = 0;
            }
            
            lastX = x;
            lastY = y;
            lastMoveTime = now;
        }
        
        if (stuckCounter > 1) {
            if (breadcrumbTrail != null && !breadcrumbTrail.isEmpty()) {
                advanceBreadcrumb(collision);
            } else if (isSearching && searchTarget != null) {
                searchTarget = generateSearchTarget(collision);
                stuckCounter = 0;
            } else {
                stuckCounter = 0;
            }
        }
    }
    
    private void forceStepBack() {
        if (isForcingStepBack) return;
        
        isForcingStepBack = true;
        stepBackFramesRemaining = 20;
        lastStuckX = x;
        lastStuckY = y;
        
        // Find best direction to step back
        float[] bestDirection = findBestStepBackDirection();
        stepBackVx = bestDirection[0] * baseSpeed * 0.5f;
        stepBackVy = bestDirection[1] * baseSpeed * 0.5f;
        
        consecutiveStuckFrames = 0;
        stuckCounter = 0;
    }
    
    private float[] findBestStepBackDirection() {
        float[][] testDirections = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0},
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
        };
        
        float bestVx = 0, bestVy = 0;
        int maxClearance = -1;
        
        for (float[] dir : testDirections) {
            int clearance = testDirectionClearance(dir[0], dir[1]);
            if (clearance > maxClearance) {
                maxClearance = clearance;
                bestVx = dir[0];
                bestVy = dir[1];
            }
        }
        
        if (maxClearance < 0) {
            double angle = Math.random() * Math.PI * 2;
            bestVx = (float)Math.cos(angle);
            bestVy = (float)Math.sin(angle);
        }
        
        return new float[]{bestVx, bestVy};
    }
    
    private int testDirectionClearance(float dirX, float dirY) {
        float len = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        if (len == 0) return 0;
        dirX /= len;
        dirY /= len;
        
        return (int)(Math.random() * 60);
    }
    
    private void executeStepBack(Collision collision) {
        if (stepBackFramesRemaining <= 0) {
            isForcingStepBack = false;
            stepBackVx = 0;
            stepBackVy = 0;
            consecutiveStuckFrames = 0;
            
            int distanceFromStuck = Math.abs(x - lastStuckX) + Math.abs(y - lastStuckY);
            if (distanceFromStuck > 15) {}
            return;
        }
        
        int nextX = Math.round(x + stepBackVx);
        int nextY = Math.round(y + stepBackVy);
        
        Rectangle testRect = new Rectangle(
            nextX + (width - bodyHitboxWidth) / 2,
            nextY + (height - bodyHitboxHeight) / 2,
            bodyHitboxWidth, bodyHitboxHeight
        );
        
        if (!collision.checkCollision(testRect)) {
            x = nextX;
            y = nextY;
        } else {
            // Try X and Y separately
            Rectangle testX = new Rectangle(
                nextX + (width - bodyHitboxWidth) / 2,
                y + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            Rectangle testY = new Rectangle(
                x + (width - bodyHitboxWidth) / 2,
                nextY + (height - bodyHitboxHeight) / 2,
                bodyHitboxWidth, bodyHitboxHeight
            );
            
            if (!collision.checkCollision(testX)) x = nextX;
            if (!collision.checkCollision(testY)) y = nextY;
            
            if (collision.checkCollision(testX) && collision.checkCollision(testY)) {
                stepBackFramesRemaining = 0;
                return;
            }
        }
        
        stepBackFramesRemaining--;
        setDirectionAnimation(stepBackVx, stepBackVy);
    }

    // ========================================
    // ANIMATION SYSTEM
    // ========================================
    
    private void setDirectionAnimation(float vx, float vy) {
        if (isRangeAttacking) return;
        
        if (vy < 0 && vx == 0) setDirection("up", upMove);
        else if (vy > 0 && vx == 0) setDirection("down", downMove);
        else if (vx < 0 && vy == 0) setDirection("left", leftMove);
        else if (vx > 0 && vy == 0) setDirection("right", rightMove);
        else if (vy < 0 && vx < 0) setDirection("up-left", upLeftMove);
        else if (vy < 0 && vx > 0) setDirection("up-right", upRightMove);
        else if (vy > 0 && vx < 0) setDirection("down-left", downLeftMove);
        else if (vy > 0 && vx > 0) setDirection("down-right", downRightMove);
    }
    
    private void setDirection(String direction, BufferedImage[] frames) {
        currentDirection = direction;
        animate(frames);
    }

    private void animate(BufferedImage[] animation) {
        if (animation == null || animation.length == 0) return;
        
        if (currentAnimation != animation) {
            currentAnimation = animation;
            frameIndex = 0;
            frameCounter = 0;
            lastFrameTime = System.currentTimeMillis();
        }
        
        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= frameInterval) {
            lastFrameTime = now;
            frameIndex = (frameIndex + 1) % animation.length;
            enemyCurrentFrame = currentAnimation[frameIndex];
        }
    }
    
    public void setIdle() {
        if (isRangeAttacking) return;
        
        BufferedImage[] moveFrames = getMovementAnimationFrames();
        if (moveFrames != null && moveFrames.length > 3) {
            enemyCurrentFrame = moveFrames[3];
        }
    }
    
    private BufferedImage[] getMovementAnimationFrames() {
        return switch (currentDirection) {
            case "up" -> upMove;
            case "down" -> downMove;
            case "left" -> leftMove;
            case "right" -> rightMove;
            case "up-left" -> upLeftMove;
            case "up-right" -> upRightMove;
            case "down-left" -> downLeftMove;
            case "down-right" -> downRightMove;
            default -> downMove;
        };
    }
    
    private void faceTarget(double dx, double dy) {
        String direction;
        if (Math.abs(dx) < 8 && dy < 0) direction = "up";
        else if (Math.abs(dx) < 8 && dy > 0) direction = "down";
        else if (Math.abs(dy) < 8 && dx < 0) direction = "left";
        else if (Math.abs(dy) < 8 && dx > 0) direction = "right";
        else if (dx < 0 && dy < 0) direction = "up-left";
        else if (dx > 0 && dy < 0) direction = "up-right";
        else if (dx < 0 && dy > 0) direction = "down-left";
        else direction = "down-right";
        
        currentDirection = direction;
    }

    // ========================================
    // IDLE LOOKING BEHAVIOR
    // ========================================
    
    private void handleIdleLook() {
        // Don't look around during attacks
        if (isMeleeAttacking || isRangeAttacking) return;
        if (state == State.MELEE_ATTACK || state == State.RANGE_ATTACK) return;
        
        if (vx == 0 && vy == 0) {
            long now = System.currentTimeMillis();
            if (now >= nextLookTime) {
                pickRandomFacing();
                
                if (isSearchPausing) {
                    nextLookTime = now + 300 + (long)(Math.random() * 500);
                } else {
                    nextLookTime = now + lookIntervalMin +
                        (long)(Math.random() * (lookIntervalMax - lookIntervalMin));
                }
            }
        }
    }
    
    private void pickRandomFacing() {
        String[] directions = {
            "up", "down", "left", "right",
            "up-left", "up-right", "down-left", "down-right"
        };
        currentDirection = directions[(int)(Math.random() * directions.length)];
        
        BufferedImage[] moveFrames = getMovementAnimationFrames();
        if (moveFrames != null && moveFrames.length > 0) {
            enemyCurrentFrame = moveFrames[0];
        }
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    private double distanceTo(Point target) {
        double dx = target.x - (x + width / 2);
        double dy = target.y - (y + height / 2);
        return Math.hypot(dx, dy);
    }

    private boolean reachedTarget(Point target, double threshold) {
        return distanceTo(target) < threshold;
    }
    
    private boolean isTargetBlocked(Point target, Collision collision) {
        Rectangle test = new Rectangle(
            target.x - bodyHitboxWidth / 2,
            target.y - bodyHitboxHeight / 2,
            bodyHitboxWidth,
            bodyHitboxHeight
        );
        return collision.checkCollision(test);
    }

    private boolean hasLineOfSight(Collision collision, int x1, int y1, int x2, int y2) {
        int samples = Math.max(4, (int)(Math.hypot(x2-x1, y2-y1) / 16));
        for (int i = 1; i <= samples; i++) {
            double t = (double)i / (double)samples;
            int sx = (int)(x1 + (x2 - x1) * t);
            int sy = (int)(y1 + (y2 - y1) * t);
            Rectangle probe = new Rectangle(sx-4, sy-4, 8, 8);
            if (collision.checkCollision(probe)) return false;
        }
        return true;
    }
    
    private void updateHitbox() {
        if (useCustomEnemy) {
            bodyHitbox.x = x + (customWidth - bodyHitboxWidth) / 2;
            bodyHitbox.y = y + (customHeight - bodyHitboxHeight + bodyHitboxOffsetY) / 2;
            return;
        }
        bodyHitbox.x = x + (width - bodyHitboxWidth) / 2;
        bodyHitbox.y = y + (height - bodyHitboxHeight) / 2;
    }
    
    private void updateCooldowns() {
        long now = System.currentTimeMillis();
        
        if (meleeOnCooldown && now - meleeLastTime >= meleeCooldown) {
            meleeOnCooldown = false;
        }
        
        if (rangeOnCooldown && now - rangeLastTime >= rangeCooldown) {
            rangeOnCooldown = false;
        }
    }

    private void regenerateMana() {
        long currentTime = System.currentTimeMillis();
        if (currentTime % 1000 < 17) {
            if (currentMana < maxMana) {
                currentMana += 1;
            }
        }
    }
    
    private void updateProjectiles(Collision collision) {
        List<EnemyProjectile> deadProjectiles = new ArrayList<>();
        for (EnemyProjectile p : projectiles) {
            p.update(collision);
            if (!p.alive) {
                deadProjectiles.add(p);
            }
        }
        projectiles.removeAll(deadProjectiles);
    }

    public void clearPlayerMemory() {
        // Reset enemy state back to patrol
        this.state = State.PATROL;
        this.hasSeenPlayer = false;
    }
    
    // ========================================
    // HEALTH, MANA, & RESPAWN
    // ========================================
    
    public void takeDamage(int damage, ScreenShake screenShake) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
            if (autoRespawn) {
                deathTime = System.currentTimeMillis();
            }
        }
        
        // Trigger smaller screen shake for enemy hits
        if (screenShake != null && alive) {
            int shakeIntensity = Math.min(damage / 3 + 2, 8);
            long shakeDuration = 150 + (damage * 5L);
            screenShake.start(shakeDuration, shakeIntensity);
        }
    }

    public void useMana(int amount) { 
        currentMana -= amount;
        if (currentMana < 0) currentMana = 0;
    }

    private void respawn() {
        alive = true;
        currentHealth = maxHealth;
        currentMana = maxMana;
        isMeleeAttacking = false;
        isRangeAttacking = false;
        meleeOnCooldown = false;
        rangeOnCooldown = false;
        hasSeenPlayer = false;
        state = State.PATROL;
        projectiles.clear();
        x = spawnX;
        y = spawnY;
        updateHitbox();
        vx = 0;
        vy = 0;
    }

    // ========================================
    // RENDERING
    // ========================================
    
    public void draw(Graphics2D g, int camX, int camY) {
        if (!alive) return;
        
        int drawX = x - camX;
        int drawY = y - camY;
        
        // If the enemy size is being customize, otherwise use default
        int renderWidth = useCustomEnemy ? customWidth : width;
        int renderHeight = useCustomEnemy ? customHeight : height; 
        
        // Draw sprite
        if (enemyCurrentFrame != null) {
            g.drawImage(enemyCurrentFrame, drawX, drawY, renderWidth, renderHeight, null);
        }
        
        // Draw health
        if (hideHealthBars) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String healthText = enemyType + " HP: " + currentHealth;
            g.drawString(healthText, drawX - 32, drawY);
        } else {
            drawHealthAndManaBars(g, drawX, drawY);
        }
        
        // Draw debug visuals
        drawDebugVisuals(g, camX, camY);
        
        // Draw projectiles
        List<EnemyProjectile> projCopy = new ArrayList<>(projectiles);
        for (EnemyProjectile p : projCopy) {
            p.draw(g, camX, camY);
        }
    }
    
    private void drawHealthAndManaBars(Graphics2D g, int drawX, int drawY) {
        int barWidth = 50;
        int barHeight = 8;
        int barOffsetY = 50;
        
        // Health bar
        double healthPercent = (double) currentHealth / maxHealth;
        int healthFillWidth = (int) (barWidth * healthPercent);
        
        g.setColor(new Color(80, 0, 0)); // Dark red background
        g.fillRect(drawX + (width / 2 - barWidth / 2), drawY + barOffsetY, barWidth, barHeight);
        
        g.setColor(new Color(220, 20, 20)); // Bright red for health
        g.fillRect(drawX + (width / 2 - barWidth / 2), drawY + barOffsetY, healthFillWidth, barHeight);
        
        g.setColor(Color.BLACK);
        g.drawRect(drawX + (width / 2 - barWidth / 2), drawY + barOffsetY, barWidth, barHeight);
        
        // Mana bar
        double manaPercent = (double) currentMana / maxMana;
        int manaFillWidth = (int) (barWidth * manaPercent);
        int manaOffsetY = barOffsetY + barHeight + 4;
        
        g.setColor(new Color(0, 0, 80)); // Dark blue background
        g.fillRect(drawX + (width / 2 - barWidth / 2), drawY + manaOffsetY, barWidth, barHeight);
        
        g.setColor(new Color(20, 120, 220)); // Bright blue for mana
        g.fillRect(drawX + (width / 2 - barWidth / 2), drawY + manaOffsetY, manaFillWidth, barHeight);
        
        g.setColor(Color.BLACK);
        g.drawRect(drawX + (width / 2 - barWidth / 2), drawY + manaOffsetY, barWidth, barHeight);
    }
    
    private void drawDebugVisuals(Graphics2D g, int camX, int camY) {
        
        // Patrol area
        if (patrolArea != null) {
            g.setColor(new Color(95, 255, 98, 60)); // Semi-transparent green
            g.fillRect(
                patrolArea.x - camX, patrolArea.y - camY,
                patrolArea.width, patrolArea.height
            );
            g.setColor(new Color(95, 250, 98)); // Solid green border
            g.drawRect(
                patrolArea.x - camX, patrolArea.y - camY,
                patrolArea.width, patrolArea.height
            );
        }

        // Body hitbox
        g.setColor(Color.RED);
        g.drawRect(
            bodyHitbox.x - camX, bodyHitbox.y - camY,
            bodyHitbox.width, bodyHitbox.height
        );
          
        // Range indicators
        drawRangeIndicators(g, camX, camY);
    }
    
    private void drawRangeIndicators(Graphics2D g, int camX, int camY) {
        int centerX = x + width/2;
        int centerY = y + height/2;
        
        // Sight range
        g.setColor(Color.BLACK);
        g.drawOval(
            centerX - sightRange - camX,
            centerY - sightRange - camY,
            sightRange * 2, sightRange * 2
        );
        
        // Melee range enter
        g.setColor(Color.BLACK);
        g.drawOval(
            centerX - meleeRangeEnter - camX,
            centerY - meleeRangeEnter - camY,
            meleeRangeEnter * 2, meleeRangeEnter * 2
        );
        
        // Melee range exit
        g.setColor(Color.BLACK);
        g.drawOval(
            centerX - meleeRangeExit - camX,
            centerY - meleeRangeExit - camY,
            meleeRangeExit * 2, meleeRangeExit * 2
        );
        
        // Range attack enter
        g.setColor(Color.BLACK);
        g.drawOval(
            centerX - rangeAttackRangeEnter - camX,
            centerY - rangeAttackRangeEnter - camY,
            rangeAttackRangeEnter * 2, rangeAttackRangeEnter * 2
        );
        
        // Range attack exit
        g.setColor(Color.BLACK);
        g.drawOval(
            centerX - rangeAttackRangeExit - camX,
            centerY - rangeAttackRangeExit - camY,
            rangeAttackRangeExit * 2, rangeAttackRangeExit * 2
        );
    }

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public boolean isAlive() { return alive; }
    public Rectangle getBodyHitbox() { return bodyHitbox; }
    public Rectangle consumeMeleeHitbox() {
        Rectangle r = meleeHitbox;
        meleeHitbox = null;
        return r;
    }
    public boolean hasSeenPlayerEver() { return hasSeenPlayer; }
    public boolean canCurrentlySeePlayer() { return canSeePlayer; }
    public boolean isCurrentlySearching() { return isSearching; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getCurrentDirection() { return currentDirection; }
    public State getState() { return state; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentMana() { return currentMana; }
    public int getMaxMana() { return maxMana; }
    public int getMeleeDamage() { return meleeDamage; }
    public int getRangeDamage() { return rangeDamage; }
}