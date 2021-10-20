package main;

import advancedGeometry.ZShapeDescriptor;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/18
 * @time 19:16
 */
public class Block {
    private long ID;
    private LineString shape;
    private double area;
    private double buildingArea;
    private double GSI;
    private ZShapeDescriptor shapeDescriptor;

    /* ------------- constructor ------------- */

    public Block() {

    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setShape(LineString shape) {
        this.shape = shape;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public void setBuildingArea(double buildingArea) {
        this.buildingArea = buildingArea;
    }

    public void setGSI(double GSI) {
        this.GSI = GSI;
    }

    /* ------------- draw ------------- */

    @Override
    public String toString() {
        return "Block{" +
                "ID=" + ID +
                ", shape=" + shape +
                ", area=" + area +
                ", buildingArea=" + buildingArea +
                ", GSI=" + GSI +
                ", shapeDescriptor=" + shapeDescriptor +
                '}';
    }
}
