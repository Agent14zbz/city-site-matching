package elements;

import advancedGeometry.ZShapeDescriptor;
import math.ZMath;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * an empty block
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/11/3
 * @time 10:53
 */
public class BlockEmpty {
    private Polygon shape;
    private Point centroid;
    private double area;
    private ZShapeDescriptor shapeDescriptor;

    private double targetGSI;
    private double targetFSI;

    /* ------------- constructor ------------- */

    public BlockEmpty(Polygon polygon) {
        this.shape = polygon;
        this.centroid = polygon.getCentroid();
        this.area = polygon.getArea();
        this.shapeDescriptor = new ZShapeDescriptor(polygon);

        this.targetGSI = ZMath.random(0.2, 0.7);
        this.targetFSI = ZMath.random(0.4, 6);
    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public Polygon getShape() {
        return shape;
    }

    public Point getCentroid() {
        return centroid;
    }

    public double getArea() {
        return area;
    }

    public ZShapeDescriptor getShapeDescriptor() {
        return shapeDescriptor;
    }

    public double getTargetGSI() {
        return targetGSI;
    }

    public double getTargetFSI() {
        return targetFSI;
    }

    /* ------------- draw ------------- */
}
