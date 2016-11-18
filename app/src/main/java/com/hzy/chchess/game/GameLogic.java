package com.hzy.chchess.game;

import android.graphics.Canvas;

import com.hzy.chchess.widget.IGameView;
import com.hzy.chchess.xqwlight.Position;
import com.hzy.chchess.xqwlight.Search;

import java.util.ArrayDeque;
import java.util.Deque;


public class GameLogic {

    private static final int MAX_HISTORY_SIZE = 16;
    private Position mPosition;
    private Search mSearch;
    private String mCurrentFen;
    private Deque<String> mHistoryList;
    private int sqSelected, mvLast;
    private volatile boolean mIsThinking = false;
    private boolean mIsFlipped = false;
    private int mLevel = 0;
    private int mStartFen = 0;
    private IGameView mChessView;

    public GameLogic(IGameView gameView) {
        init(gameView, 16, 0);
    }

    private void init(IGameView gameView, int level, int startFen) {
        mChessView = gameView;
        mPosition = new Position();
        mSearch = new Search(mPosition, level);
        mStartFen = startFen;
        mCurrentFen = Position.STARTUP_FEN[startFen];
        mHistoryList = new ArrayDeque<>();
    }

    public void drawPieces(Canvas canvas) {
        for (int x = Position.FILE_LEFT; x <= Position.FILE_RIGHT; x++) {
            for (int y = Position.RANK_TOP; y <= Position.RANK_BOTTOM; y++) {
                int sq = Position.COORD_XY(x, y);
                sq = (mIsFlipped ? Position.SQUARE_FLIP(sq) : sq);
                int xx = x - Position.FILE_LEFT;
                int yy = y - Position.RANK_TOP;
                int pc = mPosition.squares[sq];
                if (pc > 0) {
                    mChessView.drawPiece(canvas, pc, xx, yy);
                }
                if (sq == sqSelected || sq == Position.SRC(mvLast) ||
                        sq == Position.DST(mvLast)) {
                    mChessView.drawSelected(canvas, xx, yy);
                }
            }
        }
    }

    public void restart() {
        if (!mIsThinking) {
            mIsFlipped = false;
            mCurrentFen = Position.STARTUP_FEN[mStartFen];
            mHistoryList.clear();
            startPlay();
        }
    }

    public void retract() {
        if (!mIsThinking) {
            String fen = popHistory();
            if (fen != null) {
                mCurrentFen = fen;
                startPlay();
            }
        }
    }

    public void onTouch(int posX, int posY) {
        if (mIsThinking) {
            return;
        }
        int square = Position.COORD_XY(posX + Position.FILE_LEFT, posY + Position.RANK_TOP);
        if (mIsFlipped) {
            square = Position.SQUARE_FLIP(square);
        }
        int pc = mPosition.squares[square];
        if ((pc & Position.SIDE_TAG(mPosition.sdPlayer)) != 0) {
            if (mvLast > 0) {
                mvLast = 0;
            }
            sqSelected = square;
            mChessView.repaint();
        } else if (sqSelected > 0) {
            int mv = Position.MOVE(sqSelected, square);
            if (!mPosition.legalMove(mv)) {
                return;
            }
            if (!mPosition.makeMove(mv)) {
                return;
            }
            if (mPosition.captured()) {
                mPosition.setIrrev();
            }
            mvLast = mv;
            sqSelected = 0;
            if (!getResult(-1)) {
                think();
            }
        }
    }

    private void startPlay() {
        mPosition.fromFen(mCurrentFen);
        sqSelected = mvLast = 0;
        if (mIsFlipped && mPosition.sdPlayer == 0) {
            think();
        } else {
            mChessView.repaint();
        }
    }

    private void think() {
        mIsThinking = true;
        new Thread() {
            @Override
            public void run() {
                mvLast = mSearch.searchMain(100 << (mLevel << 1));
                mPosition.makeMove(mvLast);
                if (mPosition.captured()) {
                    mPosition.setIrrev();
                }
                getResult(1);
                mIsThinking = false;
                mChessView.postRepaint();
            }
        }.start();
    }

    private boolean getResult(int response) {
        if (mPosition.isMate()) {
            return true;
        }
        int vlRep = mPosition.repStatus(3);
        if (vlRep > 0) {
            return true;
        }
        if (response >= 0) {
            pushHistory(mCurrentFen);
            mCurrentFen = mPosition.toFen();
        }
        return false;
    }

    private void pushHistory(String fen) {
        if (mHistoryList.size() >= MAX_HISTORY_SIZE) {
            mHistoryList.poll();
        }
        mHistoryList.offer(fen);
    }

    private String popHistory() {
        return mHistoryList.pollLast();
    }
}
