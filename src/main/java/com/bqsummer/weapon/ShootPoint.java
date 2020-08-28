package com.bqsummer.weapon;

import org.opencv.core.Point;

public class ShootPoint implements Comparable<ShootPoint>{
    private double x;
    private double y;

    public ShootPoint(Point point) {
        this.x = point.x;
        this.y = point.y;
    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public int compareTo(ShootPoint o) {
        return (this.y - o.y) >0 ? 1 : -1;
    }
}
