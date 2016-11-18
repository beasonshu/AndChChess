package com.hzy.chchess.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hzy.chchess.R;
import com.hzy.chchess.game.GameLogic;


public class GameView extends View implements IGameView {

    private static final String TAG = GameView.class.getSimpleName();
    private static final int WIDTH_CELL_COUNT = 9;
    private static final int HEIGHT_CELL_COUNT = 10;

    private float mCellWidth;
    private Paint mPaint;
    private GameLogic mGameLogic;
    private Resources mResources;
    private Bitmap[] mPiecesBitmap;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mResources = getResources();
        initResources();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mGameLogic = new GameLogic(this);
        restart();
    }

    public void restart() {
        mGameLogic.restart();
    }

    public void retract() {
        mGameLogic.retract();
    }

    private void initResources() {
        TypedArray piecesId = mResources.obtainTypedArray(R.array.pieces_id_array);
        mPiecesBitmap = new Bitmap[piecesId.length()];
        for (int i = 0; i < piecesId.length(); i++) {
            mPiecesBitmap[i] = BitmapFactory.decodeResource(mResources, piecesId.getResourceId(i, 0));
        }
        piecesId.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float widthCell = widthSize * 1.0f / WIDTH_CELL_COUNT;
        float heightCell = heightSize * 1.0f / HEIGHT_CELL_COUNT;
        float cellWidth;
        if (widthCell < 0.1f || heightCell < 0.1f) {
            cellWidth = Math.max(widthCell, heightCell);
        } else {
            cellWidth = Math.min(widthCell, heightCell);
        }
        setMeasuredDimension((int) (cellWidth * WIDTH_CELL_COUNT),
                (int) (cellWidth * HEIGHT_CELL_COUNT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCellWidth = w * 1.0f / WIDTH_CELL_COUNT;
        for (int i = 0; i < mPiecesBitmap.length; i++) {
            mPiecesBitmap[i] = Bitmap.createScaledBitmap(mPiecesBitmap[i],
                    (int) (mCellWidth), (int) (mCellWidth), false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mGameLogic.drawPieces(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int xx = (int) (event.getX() / mCellWidth);
            int yy = (int) (event.getY() / mCellWidth);
            mGameLogic.onTouch(xx, yy);
            return true;
        }
        return true;
    }

    @Override
    public void repaint() {
        invalidate();
    }

    @Override
    public void postRepaint() {
        postInvalidate();
    }

    @Override
    public void drawPiece(Canvas canvas, int pc, int xx, int yy) {
        float x = xx * mCellWidth;
        float y = yy * mCellWidth;
        pc -= 8;
        if (pc > 6) {
            pc--;
        }
        canvas.drawBitmap(mPiecesBitmap[pc], x, y, mPaint);
    }

    @Override
    public void drawSelected(Canvas canvas, int xx, int yy) {
        float x = xx * mCellWidth;
        float y = yy * mCellWidth;
        canvas.drawBitmap(mPiecesBitmap[14], x, y, mPaint);
    }
}
