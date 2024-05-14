package org.example;

import org.example.Evaluation.EvaluationDynamic;
import org.example.moves.Move;
import org.example.moves.MoveList;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

import java.util.Arrays;

public class GameState
{
    public GameInfo gameInfo;

    public char[][] map;
    public int[] overwriteCountPlayers;
    public int[] bombCountPlayers;

    public GameState(GameState state, GameInfo gameInfo)
    {
        this.gameInfo = gameInfo;

        this.map = new char[gameInfo.mapWidth][gameInfo.mapHeight];
        this.overwriteCountPlayers = new int[gameInfo.playerCount];
        this.bombCountPlayers = new int[gameInfo.playerCount];

        for (int x = 0; x < gameInfo.mapWidth; x++)
            this.map[x] = state.map[x].clone();

        this.overwriteCountPlayers = state.overwriteCountPlayers.clone();
        this.bombCountPlayers = state.bombCountPlayers.clone();
    }

    public GameState(GameInfo gameInfo)
    {
        this.gameInfo = gameInfo;

        this.map = new char[gameInfo.mapWidth][gameInfo.mapHeight];
        this.overwriteCountPlayers = new int[gameInfo.playerCount];
        this.bombCountPlayers = new int[gameInfo.playerCount];

        for (int x = 0; x < gameInfo.mapWidth; x++)
            this.map[x] = gameInfo.initialMap[x].clone();

        for (int i = 0; i < gameInfo.playerCount; i++)
            this.overwriteCountPlayers[i] = gameInfo.overwriteCount;

        for (int i = 0; i < gameInfo.playerCount; i++)
            this.bombCountPlayers[i] = gameInfo.bombCount;
    }

    public void makeMove(int x, int y, int c, int player)
    {
        boolean validMove = false;
        char currentField = map[x][y];

        boolean[] validDirections = new boolean[8];

        // get valid Directions
        for (int i = 0; i < 8; i++)
            if (isValidDirection(i, x, y, player)) {
                validMove = true;
                validDirections[i] = true;
            }

        // color directions
        for (int i = 0; i < 8; i++)
            if (validDirections[i])
                colorDirection(i, x, y, player);
        
        // expansion rule: color only the current field if all directions were invalid
        if (!validMove && currentField == 'x') 
            map[x][y] = (char) (player + '0');

        // decrease overwrite count for the player if the field was already taken by a player or an expansion stone
        if (gameInfo.isPlayer(currentField) || currentField == 'x')
            overwriteCountPlayers[player - 1] --;

        // expansion rule: no need to color the rest of the field
        if (!validMove) 
            return; 

        map[x][y] = (char) (player + '0');

        // color all temporary colored fields in the players color
        for (int xk = 0; xk < gameInfo.mapWidth; xk++)
        for (int yk = 0; yk < gameInfo.mapHeight; yk++)
            if (map[xk][yk] == 'f')
                map[xk][yk] = (char) (player + '0');

        // choice stone: swap players
        if (currentField == 'c')
            swapPlayers(player, c);

        // inversion stone: rotate players
        if (currentField == 'i')
            rotatePlayers();

        // bonus stone: increase overwrite/bomb count of player
        if (currentField == 'b') {
            if (c == 20)
                bombCountPlayers[player - 1]++;
            if (c == 21)
                overwriteCountPlayers[player - 1]++;
        }
    }

    private void throwBomb(int x1, int y1, int strength, char[][] m)
    {
        if (isInMap(x1, y1)) {
            m[x1][y1] = '+';

            if (strength > 0) {
                for (int dir1 = 0; dir1 < Settings.DIRECTIONS.length; dir1++) {
                    if (gameInfo.transitions[x1][y1] != null && gameInfo.transitions[x1][y1][dir1] != null) {
                        int x2 = gameInfo.transitions[x1][y1][dir1][0];
                        int y2 = gameInfo.transitions[x1][y1][dir1][1];
                        if (map[x2][y2] != '-')
                            throwBomb(x2, y2, strength - 1, m);
                    } else {
                        throwBomb(x1 + Settings.DIRECTIONS[dir1][0], y1 + Settings.DIRECTIONS[dir1][1], strength - 1, m);
                    }
                }
            }
        }
    }


    public void execBomb(int x1, int y1, int strength)
    {
        throwBomb(x1, y1, strength, this.map);

        for (int i = 0; i < gameInfo.mapWidth;  i++)
        for (int j = 0; j < gameInfo.mapHeight; j++)
            if (map[i][j] == '+') {
                this.map[i][j] = '-';
            }
    }

    public boolean isValidMove(int player, int x, int y, boolean overwrite)
    {
        char currentField = this.map[x][y];

        if (!isInMap(x, y))
            return false;

        // expansion rule
        if (currentField == 'x')
            return overwrite;

        // invalid move if no overwrite but setting on other player
        if (!overwrite && Character.isDigit(currentField) && currentField != '0')
            return false;

        // check all directions and return if at least one direction is valid
        for (int k = 0; k < 8; k++)
            if (isValidDirection(k, x, y, player))
                return true;

        return false;
    }

    public int[] getValidMovesAndDirectionsCount(int player, boolean override, MoveTimer timer) throws TimerExpiredException
    {
        int moveSum = 0, directionSum = 0;
        boolean moveValid = false;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            if (!isInMap(x, y))
                break;

            char currentField = this.map[x][y];

            // expansion rule
            if (currentField == 'x') {
                if (override)
                    moveValid = true;
                else
                    break;
            }

            // invalid move if no overwrite but setting on other player
            if (!override && Character.isDigit(currentField) && currentField != '0')
                break;

            // check all directions and return if at least one direction is valid
            for (int k = 0; k < 8; k++)
                if (isValidDirection(k, x, y, player)) {
                    moveValid = true;
                    directionSum++;
                }

            if (moveValid)
                moveSum++;
            }

        return new int[]{moveSum, directionSum};
    }

    public boolean isValidDirection(int direction, int x, int y, int player)
    {
        char field = map[x][y];
        this.map[x][y] = 'n';
        boolean valid = checkDirection(direction, x, y, player, 0, false);
        this.map[x][y] = field;
        return valid;
    }

    private void colorDirection(int direction, int x, int y, int player)
    {
        checkDirection(direction, x, y, player, 0, true);
    }

    private boolean checkDirection(int direction, int x, int y, int player, int visitedCounter, boolean colorMap)
    {
        while (isInMap(x, y)) {
            char currentField = map[x][y];

            if (visitedCounter > 0 && currentField == 'n') // check if current field is the same where the player wants to set the stone (excluding the first iteration -> is the field itself)
                return false;
            if (visitedCounter == 1 && Character.getNumericValue(currentField) == player) // check if the field directly next to the chosen field is from the player himself
                return false;
            if (visitedCounter >= 2 && Character.getNumericValue(currentField) == player) // valid move if a player field is found that is not directly next to the chosen field 
                return true;
            if (visitedCounter > 0 && ((!Character.isDigit(currentField) && currentField != 'f' && currentField != 'x') || currentField == '0')) // invalid move if the field is not another player or an expansion field
                return false;

            // interim state for the currentField if colorMap is true
            if (colorMap && currentField != 'n')
                this.map[x][y] = 'f';

            if (gameInfo.transitions[x][y] != null && gameInfo.transitions[x][y][direction] != null) // check if a transition exists in the direction
                return checkDirection((gameInfo.transitions[x][y][direction][2] + 4) % 8, gameInfo.transitions[x][y][direction][0], gameInfo.transitions[x][y][direction][1], player, visitedCounter + 1, colorMap); // recursive call to checkDirection starting at transition endpoint

            visitedCounter++;

            // move coordinates to the next field depending on the direction
            x += Settings.DIRECTIONS[direction][0];
            y += Settings.DIRECTIONS[direction][1];
        }

        return false;
    }

    public MoveList getAllValidMoves(int player, EvaluationDynamic evDynamic, boolean override, MoveTimer timer, boolean sorting) throws TimerExpiredException
    {
        char cur;
        var moves = new MoveList(sorting);

        if (override)
            override = overwriteCountPlayers[player - 1] > 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (timer.isExpired())
                throw new TimerExpiredException();

            if (isValidMove(player, x, y, override)) {
                cur = map[x][y];

                if (cur == 'c')
                    moves.add(new Move(x, y, getCurrentBestPlayer(evDynamic, timer)));
                else if (cur == 'b')
                    moves.add(new Move(x, y, 21));
                else
                    moves.add(new Move(x, y, 0));
            }
        }

        return moves;
    }

    private int getCurrentBestPlayer(EvaluationDynamic evDynamic, MoveTimer timer) throws TimerExpiredException
    {
        int maxValue = Integer.MIN_VALUE, bestPlayer = 1, newValue;

        for (int i = 1; i <= gameInfo.playerCount; i++) {
            newValue = evDynamic.evaluateForPlayer((char) (i + '0'), this, timer);

            if (newValue > maxValue) {
                maxValue = newValue;
                bestPlayer = i;
            }
        }

        return bestPlayer;
    }

    public Move getFirstMove(int player)
    {
        Move move = getFirstMove(player, false);

        if (move == null)
            move = getFirstMove(player, true);

        return move;
    }

    public Move getFirstMove(int player, boolean override)
    {
        char cur;

        if (override)
            override = overwriteCountPlayers[player - 1] > 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (isValidMove(player, x, y, override)) {
                cur = map[x][y];

                if (cur == 'c')
                    return new Move(x, y, player);
                else if (cur == 'b')
                    return new Move(x, y, 21);
                else
                    return new Move(x, y, 0);
            }
        }

        return null;
    }

    private int getStoneCount(char player)
    {
        int sum = 0;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++)
            if (map[x][y] == player)
                sum++;

        return sum;
    }

    public int getGameRank()
    {
        int[][] playerStones = new int[gameInfo.playerCount][2];
        int rank = 0;

        for (int i = 0; i < gameInfo.playerCount; i++) {
            playerStones[i][0] = getStoneCount((char) (i + 1 + '0'));
            playerStones[i][1] = i + 1;
        }

        Arrays.sort(playerStones, (a, b) -> Integer.compare(b[0], a[0]));

        for (int i = 0; i < playerStones.length; i++) {
            if (playerStones[i][1] == gameInfo.playerNumber) {
                rank = i + 1;
                break;
            }
        }

        return rank;
    }

    public int getBombPhaseTarget()
    {
        int target, gameRank = getGameRank();

        if (gameRank == 1)
            target = 2;
        else
            target = gameRank - 1;

        return target;
    }

    public int[] getBestBombField(int opponent)
    {
        int[][] mapHits = new int[gameInfo.mapWidth][gameInfo.mapHeight];
        int[] bestField = new int[2];
        int max = Integer.MIN_VALUE;
        char opp = (char) (opponent + '0');

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++)
            if (map[x][y] != '-') {
                var mapTemp = new char[gameInfo.mapWidth][gameInfo.mapHeight];
                throwBomb(x, y, gameInfo.bombRadius, mapTemp);

                mapHits[x][y] = countHitStones(mapTemp, opp);
                if (mapHits[x][y] > max) {
                    max = mapHits[x][y];
                    bestField[0] = x;
                    bestField[1] = y;
                }
            }

        return bestField;
    }

    private int countHitStones(char[][] m, char opponent)
    {
        int counter = 0;
        var player = (char) (gameInfo.playerNumber + '0');

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (m[x][y] == '+') {
                if (map[x][y] == opponent)
                    counter++;
                if (map[x][y] == player)
                    counter--;
            }
        }
        return counter;
    }

    public boolean isInMap(int x, int y)
    {
        return x >= 0 && y >= 0 && x < gameInfo.mapWidth && y < gameInfo.mapHeight && map[x][y] != '-';
    }

    private void swapPlayers(int p1, int p2)
    {
        if (p1 == p2)
            return;

        char player1 = (char) (p1 + '0');
        char player2 = (char) (p2 + '0');

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (map[x][y] == player1)
                this.map[x][y] = player2;
            else if (map[x][y] == player2)
                this.map[x][y] = player1;
        }
    }

    private void rotatePlayers()
    {
        int currentField;

        for (int x = 0; x < gameInfo.mapWidth;  x++)
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            if (!Character.isDigit(map[x][y])) // no player
                continue;

            currentField = Character.getNumericValue(this.map[x][y]);

            if (!(currentField > 0) || currentField > gameInfo.playerCount) // invalid player or 0
                continue;

            this.map[x][y] = (char) ((currentField % gameInfo.playerCount) + 1 + '0');
        }
    }

    public void printMap()
    {
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            for (int x = 0; x < gameInfo.mapWidth; x++)
                System.out.print(map[x][y] + " ");
            System.out.println();
        }
    }

    public void printValidMovesForPlayer(int player)
    {
        for (int y = 0; y < gameInfo.mapHeight; y++) {
            for (int x = 0; x < gameInfo.mapWidth; x++) {
                if (isValidMove(player, x, y, overwriteCountPlayers[player - 1] > 0))
                    System.out.print(map[x][y] + "'");
                else
                    System.out.print(map[x][y] + " ");
            }
            System.out.println();
        }
    }
}
