package org.example;

import org.example.Evaluation.EvaluationDynamic;
import org.example.Evaluation.EvaluationStatic;
import org.example.moves.CalculatedMove;
import org.example.moves.Move;
import org.example.moves.MoveTree;
import org.example.network.GameMessage;
import org.example.network.ServerConnection;
import org.example.timer.DisabledMovetimer;
import org.example.timer.EnabledMoveTimer;
import org.example.timer.MoveTimer;
import org.example.timer.TimerExpiredException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Client
{
    // network communication
    private final ServerConnection connection;
    private GameMessage packet;
    private DataInputStream reader;
    private DataOutputStream writer;

    private GameInfo gameInfo;
    private Settings settings;
    private MoveTimer timer;
    private EvaluationStatic evStatic;
    private EvaluationDynamic evDynamic;
    private GameState state;
    // private MoveTree tree;

    // game phase
    private boolean bombPhase = false, gameRunning = true;

    // statistics
    private long totalSearchTime = 0, totalMoveCount = 0;
    private int interruptedSearches = 0;

    public Client(ServerConnection sc, Settings settings)
    {
        this.connection = sc;
        this.settings = settings;

        Main.logger.clientSettings(settings.abp, settings.sorting); // TODO: change method to take Settings as parameter
    }

    public void run() throws IOException
    {
        sendGroupNumber();
        receiveMap();
        receivePlayerNumber();

        while (gameRunning) {
            this.packet = connection.receive();
            this.reader = new DataInputStream(new ByteArrayInputStream(packet.data()));

            switch (packet.type()) {
                case 4 -> {
                    int timelimit = reader.readInt();
                    int depthlimit = reader.readByte();
                    if (bombPhase)
                        sendBomb(state.getBestBombField(state.getBombPhaseTarget()));
                    else
                        sendMove(timelimit, depthlimit);
                }
                case 6 -> {
                    if (bombPhase) {
                        state.execBomb(reader.readShort(), reader.readShort(), gameInfo.bombRadius);
                        reader.skipBytes(2);
                    } else {
                        int x = reader.readShort();
                        int y = reader.readShort();
                        int c = reader.readByte();
                        int p = reader.readByte();
                        Main.logger.logMove(x, y, c, p);
                        state.makeMove(x, y, c, p);
                    }
                }
                case 7 -> reader.skipBytes(1);
                case 8 -> this.bombPhase = true;
                case 9 -> {
                    gameRunning = false;
                    Main.logger.gameEnd(totalSearchTime, totalMoveCount, state.map, interruptedSearches);
                }
                default -> throw new IllegalStateException("Unexpected packet: " + packet);
            }
        }
    }

    private void sendGroupNumber()
    {
        this.packet = new GameMessage(1, new byte[]{Settings.GROUP_NUMBER});
        connection.send(packet);
    }

    private void receiveMap()
    {
        this.packet = connection.receive();
        Main.logger.runtimeInfo("initializing game ...");
        initGame(packet.data());
    }

    private void receivePlayerNumber()
    {
        this.packet = connection.receive();
        gameInfo.playerNumber = packet.data()[0];
        Main.logger.gameInfo("received playernumber " + gameInfo.playerNumber);
    }

    private void runEvaluationStatic(MoveTimer timer)
    {
        try {
            evStatic.runEvaluation(timer);
            evStatic.printEvaluationMap();
        } catch (TimerExpiredException e) {
            Main.logger.logIntoMove("NO EVALUATION: static evaluation did not finish in time ...");
        }
    }

    private Move searchMoveDepthlimit(int depthlimit)
    {
        Move move = state.getFirstMove(gameInfo.playerNumber);
        timer = new DisabledMovetimer();

        if (!evStatic.isEvaluated())
            runEvaluationStatic(timer);

        try {
            // move = tree.getBestMoveParanoid(new GameState(state, gameInfo), gameInfo.playerNumber, depthlimit, timer).move;
            move = new MoveTree(gameInfo, evDynamic, settings, gameInfo.playerNumber).calculateBestMove(state, depthlimit, timer, settings.brsp).move;
        } catch (TimerExpiredException e) {
            return move;
        }

        timer.cancelTimer();

        return move;
    }

    private Move iterativeDeepening(int timelimit)
    {
        int currentDepth = 1;
        CalculatedMove possibleMove, acceptedMove;

        Move move = state.getFirstMove(gameInfo.playerNumber);

        timer = new EnabledMoveTimer(timelimit - Settings.TIMER_BUFFER);

        if (!evStatic.isEvaluated())
            runEvaluationStatic(timer);

        while (!timer.isExpired()) {
            try {
                // possibleMove = tree.getBestMoveParanoid(new GameState(state, gameInfo), gameInfo.playerNumber, currentDepth, timer);
                possibleMove = new MoveTree(gameInfo, evDynamic, settings, gameInfo.playerNumber).calculateBestMove(state, currentDepth, timer, settings.brsp);
            } catch (TimerExpiredException e) {
                Main.logger.logIntoMove("TIMELIMIT REACHED: did not finish the last iteration ...");
                interruptedSearches++;
                break;
            }

            acceptedMove = possibleMove;
            move = possibleMove.move;
            Main.logger.logCalculatedMove(acceptedMove);

            if (acceptedMove.evaluationValue == Integer.MAX_VALUE || acceptedMove.evaluationValue == Integer.MIN_VALUE) {
                Main.logger.logIntoMove("QUIT ITERATION: reached final state ...");
                break;
            }

            if (acceptedMove.estimatedTimeNextMove > timer.getTimeLeft() && !timer.isExpired()) {
                Main.logger.logIntoMove("QUIT ITERATION: next estimated time is too long ...");
                break;
            }

            currentDepth++;
        }

        timer.cancelTimer();

        return move;
    }

    private void sendMove(int timelimit, int depthlimit) throws IOException
    {
        Main.logger.startMove();

        long startTime = System.currentTimeMillis();

        Move move;

        if (timelimit > 0)
            move = iterativeDeepening(timelimit);
        else
            move = searchMoveDepthlimit(depthlimit);

        var data = new ByteArrayOutputStream(5);
        this.writer = new DataOutputStream(data);

        writer.writeShort(move.x);
        writer.writeShort(move.y);
        writer.writeByte(move.c);
        writer.flush();

        this.packet = new GameMessage(5, data.toByteArray());
        connection.send(packet);

        long finishTime = System.currentTimeMillis();

        totalMoveCount++;
        totalSearchTime = totalSearchTime + (finishTime - startTime);

        Main.logger.endMove(finishTime - startTime, timelimit);
    }

    private void sendBomb(int[] field) throws IOException
    {
        if (field.length == 0)
            System.out.println("No move available!");
        else {
            var data = new ByteArrayOutputStream(5);
            this.writer = new DataOutputStream(data);

            writer.writeShort(field[0]);
            writer.writeShort(field[1]);
            writer.writeByte(0);
            writer.flush();

            this.packet = new GameMessage(5, data.toByteArray());
            connection.send(packet);
        }
    }

    public void initGame(byte[] data)
    {
        gameInfo  = new GameInfo(data);
        state     = new GameState(gameInfo);
        evStatic  = new EvaluationStatic(gameInfo);
        evDynamic = new EvaluationDynamic(gameInfo, evStatic);
        // tree = new MoveTree(gameInfo, evDynamic, settings, gameInfo.playerNumber);

        Main.logger.gameInfo("Initial map:");
        Main.logger.logMap(state.map);
    }
}
