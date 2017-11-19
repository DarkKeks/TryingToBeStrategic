public class Point {
    public static final double EPS = 1E-6;

    public double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
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

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public int intX() {
        return (int) Math.round(x);
    }

    public int intY() {
        return (int) Math.round(y);
    }

    public int compareTo(double x, double y) {
        if(compareDouble(this.x, x) == 0) return compareDouble(this.y, y);
        return compareDouble(this.x, x);
    }

    private int compareDouble(double a, double b) {
        return (Math.abs(a - b) < EPS ? 0 : (a - b < 0 ? 1 : -1));
    }
}
