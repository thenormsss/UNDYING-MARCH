# UNDYING-MARCH

#WHAT TO EXPECT IN THIS GAME?#
*The player can travel anywhere across the map.
*The player can move and attack using melee & ranged attack in 8-direction.
*Enemy AI can do the following:
1. They can patrol their assigned zone
2. If they see the player, base on the position of the player in the enemy ranges they will do the following:
-Chase the player
-Search the player in their last seen position (using the breadcrumbs)
-After searching, if the player is not visible, go back to the patrol zone
-Ranged attack (if available)
-Melee attack (if available)
-Maintaining distance to the player (if melee attack is not available)
3. When the player hit the enemy or in vice versa, the screen will slightly shake
4. When the player died, it will respawn after 5 seconds or the user can press the "R" hotkey
5. When the enemy died, it will respawn after 10 seconds back on its default position

#CONTROLS#
*"W" = UP
*"S" = DOWN
*"D" = RIGHT
*"A" = LEFT
*"U" = MELEE ATTACK
*"I" = RANGED ATTACK
*"R" = RESPAWN (WHEN DIED)
*"SHIFT" = TO SPRINT

#MAP COLLSION#
*LOCATED AT THE SOUTHERN PART OF THE MC (HOUSE AND THREE)
*COLLISION VISUAL = LIGHT WHITE
 
#ENEMIES#
*PRACTICE DUMMY
-Located near the spawn point of the player
-It doesn't move
-It doesn't attack
-It can only received damage and died
-It will respawn after 5 seconds

*BANDIT
-Located at the southern part of the player
-It can move
-It can attack only melee which it will keep the distance near to the player so it can perform melee attack
-It can damaged the player as well as received damaged and died
-It will respawn after 10 seconds

*HIGH MAGE
-Located at the northern part of the player
-It can move
-It can attack only ranged which it will maintain its distance to the player so it can perform ranged attack
-It can damaged the player as well as received damaged and died
-It will respawn after 10 seconds

*MUSKETEER
-Located at the eastern part of the player
-It can move
-It can attack both melee & ranged. It depends on the position of the player in the enemy range what it will perform. Also, if the musketeer is low on mana, it will shift to melee attack
-It can damaged the player as well as received damaged and died
-It will respawn after 10 seconds

#PLAYER INDICATORS#
*HEALTH = UPPERRED 
*MANA = BLUE
*TEXT = WHITE
*BREADCRUMBS = BROWN
*BODY_HITBOX = RED
*SENSOR_HITBOX = YELLOW
*PROJECTILE = MAGENTA

#ENEMY INDICATORS#
*HEALTH = RED
*MANA = BLUE
*TEXT = WHITE
*BODY_HITBOX = RED
*PROJECTILE = RED
*PATROL AREA = GREEN
*RANGES = BLACK 
*RANGES PURPOSE = Starting from the outer circle (it may depend on the enemy type):
-VISIBLE SIGHT
-RANGED
-MELEE