package elements;

import advancedGeometry.ZShapeDescriptor;
import basicGeometry.ZPoint;
import math.ZGeoMath;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/21
 * @time 18:03
 */
public class Block {
    // properties
    private long ID;
    private String cityName;
    private Polygon shape;
    private List<Building> buildings;

    // features from database
    private double area;
    private double GSI;

    // features calculated
    private Point centroid;
    private ZShapeDescriptor shapeDescriptor;
    private ZPoint dirVec;

    /* ------------- constructor ------------- */

    public Block(BlockRaw blockRaw, CityRaw cityRaw) {
        this.ID = blockRaw.getID();
        this.cityName = cityRaw.getName();
        this.shape = blockRaw.getAbsShape(cityRaw.getRatio());

        this.area = blockRaw.getArea();
        this.GSI = blockRaw.getGSI();

    }

    /* ------------- member function ------------- */

    public void initProperties() {
        this.centroid = shape.getCentroid();
        this.shapeDescriptor = new ZShapeDescriptor(shape);
        this.dirVec = ZGeoMath.obbDir(shape);
    }

    /* ------------- setter & getter ------------- */

    public void setShape(Polygon shape) {
        this.shape = shape;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public Polygon getShape() {
        return shape;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    /* ------------- draw ------------- */
}
