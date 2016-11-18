package com.hzy.chchess.widget;


import android.graphics.Canvas;

public interface IGameView {

    void repaint();

    void postRepaint();

    void drawPiece(Canvas canvas, int pc, int xx, int yy);

    void drawSelected(Canvas canvas, int xx, int yy);
}
