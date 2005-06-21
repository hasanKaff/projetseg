package seg.jUCMNav.figures.router;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import ucm.map.PathNode;

/**
 * Created 2005-03-02
 * 
 * This class represent an interpolated B-Spline. Specify a point list representing the points on the curve and the class will be able to return a bspline going
 * through those points.
 * 
 * In the initial version of the connection router, used PathNodes for positions. Now uses a pointlist.
 * 
 * @author Etienne Tremblay
 */
public class BSpline {

    private int n, n1;

    private ArrayList nodes = new ArrayList(); // An array of pathnodes composing the spline.

    private double[] Px, Py, // The points (x,y) of the spline
            dx, dy, // The direction for the spline at each point
            Ax, Ay, Bi, B0, B1, B2, B3;

    private int precision = 26; // Number of iteration to do between two points

    // of the curve.

    /**
     * Build an empty spline. Points will be passed later.
     */
    public BSpline() {

    }

    /**
     * Build a spline from a list of points.
     * 
     * @param list
     */
    public BSpline(PointList list) {
        n = list.size();
        if (n <= 1)
            return; // We have to have two points or more to make a spline
        init(list);
    }

    /**
     * @deprecated Pass a point list, not an arraylist of pathnodes.
     * @param nodes
     */
    public BSpline(ArrayList nodes) {
        setPoints(nodes);
    }

    /**
     * Initialize all the arrays and the necessary data for the spline.
     *  
     */
    private void init(PointList points) {
        n1 = n + 1;
        Px = new double[n1];
        Py = new double[n1];
        dx = new double[n1];
        dy = new double[n1];

        Ax = new double[n1];
        Ay = new double[n1];
        Bi = new double[n1];

        B0 = new double[precision];
        B1 = new double[precision];
        B2 = new double[precision];
        B3 = new double[precision];

        double t = 0;
        double t1, t12, t2;
        for (int i = 0; i < precision; i++) {
            t1 = 1 - t;
            t12 = t1 * t1;
            t2 = t * t;

            B0[i] = t1 * t12;
            B1[i] = 3 * t * t12;
            B2[i] = 3 * t2 * t1;
            B3[i] = t * t2;
            t += .04;
        }

        int i;
        Point point;
        // Initialize the points array.
        for (i = 0; i < n; i++) {
            point = points.getPoint(i);
            Px[i] = point.x;
            Py[i] = point.y;
        }

        // dx, dy will determine the direction of the first and last connection
        // of the spline
        // If the nodes are in a straight line, we want the spline to be
        // straight too...
        double x, y;
        x = Px[1] - Px[0];
        y = Py[1] - Py[0];

        // Setting it between the first and second point seems to work.
        dx[0] = x / 2;
        dy[0] = y / 2;

        x = Px[n - 1] - Px[n - 2];
        y = Py[n - 1] - Py[n - 2];

        // Same thing for the last point.
        dx[n - 1] = x / 2;
        dy[n - 1] = y / 2;

    }

    /**
     * Add a point to the spline.
     * 
     * @param p
     *            The point to add.
     * @param index
     *            The index where to add it.
     */
    public void addPoint(Point p, int index) {
        index = getPoint(p.x, p.y) + 1;
        n1++;
        double[] px = new double[n1], py = new double[n1];
        for (int i = 0; i < index; i++) {
            px[i] = Px[i];
            py[i] = Py[i];
        }
        for (int i = index; i < n; i++) {
            px[i + 1] = Px[i];
            py[i + 1] = Py[i];
        }
        Px = px;
        Py = py;
        Px[index] = p.x;
        Py[index] = p.y;
        double[] tx = new double[n1];
        double[] ty = new double[n1];
        tx[0] = dx[0];
        ty[0] = dy[0];
        tx[n] = dx[n - 1];
        ty[n] = dy[n - 1];
        dx = tx;
        dy = ty;
        n++;
        Ax = new double[n1];
        Ay = new double[n1];
        Bi = new double[n1];
    }

    /**
     * Add a point at the end of the spline
     * 
     * @param p
     *            The point to add.
     */
    public void addPointLast(Point p) {
        addPoint(p, n1);
    }

    /**
     * Set the list of points.
     * 
     * @deprecated
     * @param nodes
     *            A list of PathNodes that this BSpline should follow.
     */
    public void setPoints(ArrayList nodes) {
        n = nodes.size();
        if (n <= 1)
            return; // We have to have two points or more to make a spline

        this.nodes = nodes;
        PointList points = new PointList();
        for (Iterator i = nodes.iterator(); i.hasNext();) {
            PathNode node = (PathNode) i.next();
            points.addPoint(node.getX(), node.getY());
        }
        init(points);
    }

    /**
     * @return The a path node point list contained in an ArrayList.
     */
    public ArrayList getPoints() {
        return nodes;
    }

    /**
     * Return the index of the nearest point on the curve from this x,y.
     * 
     * @param x
     * @param y
     * @return The index of the nearest point on the curve for this x,y.
     */
    private int getPoint(int x, int y) {
        int iMin = 0;
        double Rmin, r2, xi, yi;

        xi = (x - Px[0]);
        yi = (y - Py[0]);
        Rmin = xi * xi + yi * yi;

        // Loop trough all the points
        for (int i = 1; i < n; i++) {
            // Calculate the distance with this point
            xi = (x - Px[i]);
            yi = (y - Py[i]);
            r2 = xi * xi + yi * yi;
            // If the distance is smaller than the smallest so far, assign the
            // new nearest point...
            if (r2 < Rmin) {
                iMin = i;
                Rmin = r2;
            }
        }
        return iMin;
    }

    /**
     * @see getPoint(x,y)
     * @param point
     *            Return the index of the nearest point on the curve for this point.
     * @return The index of the nearest point on the curve for this point.
     */
    private Point getPoint(Point point) {
        int index = getPoint(point.x, point.y);
        return new Point(Px[index], Py[index]);
    }

    /**
     * Get the nearest index for the PathNode specified
     * 
     * @deprecated
     * @param node
     *            The node we want the index.
     * @return The index of the following node.
     */
    public int getPoint(PathNode node) {
        return nodes.indexOf(node);
    }

    /**
     * @deprecated
     * @return The start point of this BSpline.
     */
    public PathNode getStartPoint() {
        return (PathNode) nodes.get(0);
    }

    /**
     * @deprecated
     * @return The end point of this BSpline.
     */
    public PathNode getEndPoint() {
        return (PathNode) nodes.get(nodes.size() - 1);
    }

    /**
     * Return a point list of the points of the curve between two points on the curve. The function checks for which point is the closest to the two points
     * specified as parameter and return the corresponding index of the points on the curve for each point. So you have to give pretty precise points if you
     * don't want the function to find a point that is near the point you specified but is not the point you wanted.
     * 
     * @param p1
     *            The first point
     * @param p2
     *            The second point
     * @return The point list between the point p1 and p2.
     */
    public PointList getPointsBetween(Point p1, Point p2) {
        findCPoints(); // Find curve points
        int start = getPoint(p1.x, p1.y);
        int end = getPoint(p2.x, p2.y);

        if (start > end && end == 0 && Px[0] == Px[n - 1] && Py[0] == Py[n - 1])
            end = n - 1;
        PointList points = getPointBetween(start, end);

        return points;
    }

    /**
     * Return the point list between the PathNode p1 and p2.
     * 
     * @deprecated
     * @param p1
     *            The first point
     * @param p2
     *            The point following the first point
     * @return The point list representing the path between the two points.
     */
    public PointList getPointsBetween(PathNode p1, PathNode p2) {
        findCPoints(); // Find curve points
        int start = getPoint(p1);
        int end = getPoint(p2);

        PointList points = getPointBetween(start, end);

        return points;
    }

    /**
     * Return all the points for drawing the spline.
     * 
     * @return The point list for the spline.
     */
    public PointList drawSpline() {
        findCPoints();

        PointList points = getPointBetween(0, n - 1);

        return points;
    }

    /**
     * Return the point list between the index start and end.
     * 
     * @param start
     *            The first index
     * @param end
     *            The last index
     * @return The point list representing the curve between those two index.
     */
    private PointList getPointBetween(int start, int end) {
        int X, Y;
        int Xo = (int) Math.round(Px[0]);
        int Yo = (int) Math.round(Py[0]);
        int Xold = Xo;
        int Yold = Yo;

        PointList points = new PointList();

        for (int i = start; i < end; i++) {
            for (int k = 0; k < precision; k++) {
                X = (int) Math.round((Px[i] * B0[k] + (Px[i] + dx[i]) * B1[k] + (Px[i + 1] - dx[i + 1]) * B2[k] + Px[i + 1] * B3[k]));
                Y = (int) Math.round((Py[i] * B0[k] + (Py[i] + dy[i]) * B1[k] + (Py[i + 1] - dy[i + 1]) * B2[k] + Py[i + 1] * B3[k]));

                points.addPoint(X, Y);

                Xold = X;
                Yold = Y;
            }
        }
        return points;
    }

    /**
     * Find curve points. Update all the arrays so that we can find the curve points after.
     *  
     */
    public void findCPoints() {
        Bi[1] = -.25;
        Ax[1] = (Px[2] - Px[0] - dx[0]) / 4;
        Ay[1] = (Py[2] - Py[0] - dy[0]) / 4;
        for (int i = 2; i < n - 1; i++) {
            Bi[i] = -1 / (4 + Bi[i - 1]);
            Ax[i] = -(Px[i + 1] - Px[i - 1] - Ax[i - 1]) * Bi[i];
            Ay[i] = -(Py[i + 1] - Py[i - 1] - Ay[i - 1]) * Bi[i];
        }
        for (int i = n - 2; i > 0; i--) {
            dx[i] = Ax[i] + dx[i + 1] * Bi[i];
            dy[i] = Ay[i] + dy[i + 1] * Bi[i];
        }
    }
}