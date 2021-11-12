package elements;

import advancedGeometry.ZShapeDescriptor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

/**
 * description
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

    /* ------------- constructor ------------- */

    public BlockEmpty(Polygon polygon) {
        this.shape = polygon;
        this.centroid = polygon.getCentroid();
        this.area = polygon.getArea();
        this.shapeDescriptor = new ZShapeDescriptor(polygon);
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

    /* ------------- draw ------------- */
}
