package org.example.logger;

import org.example.moves.CalculatedMove;

public class EnabledGameLogger extends AbstractGameLogger 
{
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    public static final String RED_BOLD = "\033[1;31m";
    public static final String PURPLE_BOLD = "\033[1;35m";
    public static final String WHITE_BOLD = "\033[1;37m";  

    @Override
    public void logMove(int x, int y, int c, int p) 
    {
        System.out.println();
        System.out.printf(GREEN + "Player %d sent move (%d, %d, %d) \n" + RESET, p, x, y, c);
    }

    @Override
    public void error(String message) 
    {
        System.out.println();
        System.out.println(RED_BOLD + "ERROR: " + RESET + RED + message + RESET);
    }

    @Override
    public void gameInfo(String message)
    {
        System.out.println();
        System.out.println(message);
    }

    @Override
    public void clientSettings(boolean abp, boolean sorting) 
    {
        System.out.println();
        System.out.println("Client settings:");
        System.out.println(" - alpha/beta pruning: " + PURPLE_BOLD + abp + RESET);
        System.out.println(" - move sorting: " + PURPLE_BOLD + sorting + RESET);
    }

    @Override
    public void gameEnd(long totalSearchTime, long totalMoveCount, char[][] finalMap, int interruptedSearches)
    {
        System.out.println();
        System.out.println(YELLOW + "==================== game end ====================" + RESET);
        System.out.println();
        System.out.println(WHITE_BOLD + "final map:" + RESET);
        logMap(finalMap);
        System.out.println();
        System.out.println(WHITE_BOLD + "statistics:" + RESET);
        if (totalMoveCount > 0)
            System.out.println("average search time per move: " + PURPLE_BOLD + totalSearchTime / totalMoveCount + RESET + " milliseconds");
        System.out.println("interrupted searches: " + PURPLE_BOLD + interruptedSearches + RESET);
    }

    @Override
    public void logMap(char[][] map)
    {
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length;  x++) 
                System.out.print(map[x][y] + " ");
            System.out.println();
        }
    }

    @Override
    public void runtimeInfo(String message)
    {
        System.out.println(BLUE + message + RESET);
    }

    @Override
    public void logMoveRequest(int timelimit, int[] move, long duration, int depthReach)
    {
        System.out.println(); 
        System.out.println(YELLOW + "==================== move request ====================");
        System.out.println("| time left: " + timelimit);
        System.out.println("| reached depth " + depthReach);
        System.out.printf("| found move (%d, %d, %d) in %d milliseconds\n", move[0], move[1], move[2], duration);
        System.out.println("======================================================" + RESET);
    }

    @Override
    public void logHelpText() 
    {
        System.out.println();
        System.out.println("Client08 supports the following options:");
        System.out.println("\toptional:");
        System.out.println(CYAN + "\t\t-q" + RESET + "\tdisable console output");
        System.out.println(CYAN + "\t\t-a" + RESET + "\tdisable alpha/beta pruning");
        System.out.println(CYAN + "\t\t-i" + RESET + "\tset IP address of server");
        System.out.println(CYAN + "\t\t-p" + RESET + "\tset port");
        System.out.println(CYAN + "\t\t-n" + RESET + "\tdisable move sorting");
        System.out.println(CYAN + "\t\t-h" + RESET + "\tdisplay this help Dialog (application still runs normal)");
        System.out.println();
    }

    @Override
    public void startMove()
    {
        System.out.println();
        System.out.println("==================== move request ====================");
    }

    @Override
    public void logCalculatedMove(CalculatedMove calcMove)
    {
        System.out.print("|| \n");
        System.out.printf("|| result of searching until depth %s%d%s:\n", YELLOW,  calcMove.depthReach, RESET);
        System.out.printf("|| \tmove:\t\t\t\t%s(%d, %d, %d)%s\n", YELLOW, calcMove.move.x, calcMove.move.y, calcMove.move.c, RESET);
        System.out.printf("|| \tduration:\t\t\t%s%d%s ms\n", YELLOW, calcMove.duration, RESET);
        System.out.printf("|| \tevaluation value:\t\t%s%d%s\n", YELLOW, calcMove.evaluationValue, RESET);
        System.out.printf("|| \tinner nodes:\t\t\t%s%d%s\n", YELLOW, calcMove.innerCount, RESET);
        System.out.printf("|| \tleafnodes visited:\t\t%s%d%s\n", YELLOW, calcMove.leafCount, RESET);
        System.out.printf("|| \ttotal states:\t\t\t%s%d%s\n", YELLOW, calcMove.innerCount + calcMove.leafCount, RESET);
        System.out.printf("|| \taverage valid moves:\t\t%s%d%s\n", YELLOW, calcMove.validMovesCount / calcMove.innerCount, RESET);
        System.out.printf("|| \taverage time per state:\t\t%s%f%s\n", YELLOW, calcMove.timePerState, RESET);
        System.out.printf("|| \tnext estimated duration:\t%s%f%s\n", YELLOW, calcMove.estimatedTimeNextMove, RESET);
        // System.out.printf("|| \tpruning percentage:\t%s%f%s\n", YELLOW, 1 - ((float) (calcMove.leafCount + calcMove.stateCount) / (float) calcMove.expectedStates), RESET);
    }

    @Override
    public void endMove(long duration, int timelimit)
    {
        System.out.println("|| ");
        System.out.println("|| time consumed: " + duration);
        System.out.println("|| time left: " + (timelimit - duration));
        System.out.println("|| ");
        System.out.println("======================================================");
    }

    @Override
    public void logIntoMove(String message)
    {
        System.out.println("|| ");
        System.out.println("|| " + message);
    }
}
