package elements;

import advancedGeometry.ZShapeDescriptor;
import basicGeometry.ZFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import utils.GeoMath;

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
    private long ID;
    private LineString geomLatLon;
    private double area;
    private double GSI;
    private double FSI;
    private String cityName;
    private double cityRatio;
    private List<Long> buildingIDs;
    private Polygon shape;
    private ZShapeDescriptor shapeDescriptor;

    private List<Building> buildings;
    private Point centroidLatLon;
    private Point centroid;

    /* ------------- constructor ------------- */

    public Block() {

    }

    /* ------------- member function: pre-processing ------------- */

    /**
     * transform lat-lon coordinate to absolute coordinate (origin at the block centroid)
     */
    public void generateAbsShape() {
        // lat, lon
        GeoMath geoMath = new GeoMath(centroidLatLon.getY(), centroidLatLon.getX());
        geoMath.setRatio(cityRatio);

        Coordinate[] coords = geomLatLon.getCoordinates();
        double[][] absPts = new double[coords.length][];
        for (int i = 0; i < coords.length; i++) {
            double[] xy = geoMath.latLngToXY(coords[i].getY(), coords[i].getX());
            absPts[i] = xy;
        }

        Coordinate[] absCoords = new Coordinate[absPts.length];
        for (int i = 0; i < absCoords.length; i++) {
            absCoords[i] = new Coordinate(absPts[i][0], absPts[i][1]);
        }
        this.shape = ZFactory.jtsgf.createPolygon(absCoords);
    }

    /**
     * calculate geometry properties from shape
     */
    public void initProperties() {
        this.centroid = shape.getCentroid();
        this.shapeDescriptor = new ZShapeDescriptor(shape);
    }

    /* ------------- member function: block matching ------------- */

    /**
     * calculate geometry properties from shape
     */
    public void initProperties2() {
        this.centroid = shape.getCentroid();
    }

    /* ------------- setter & getter ------------- */

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setGeomLatLon(LineString geomLatLon) {
        this.geomLatLon = geomLatLon;
        this.centroidLatLon = geomLatLon.getCentroid();
    }

    public void setArea(double area) {
        this.area = area;
    }

    public void setGSI(double GSI) {
        this.GSI = GSI;
    }

    public void setFSI(double FSI) {
        this.FSI = FSI;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setCityRatio(double cityRatio) {
        this.cityRatio = cityRatio;
    }

    public void setBuildingIDs(List<Long> buildingIDs) {
        this.buildingIDs = buildingIDs;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public void setShape(Polygon shape) {
        this.shape = shape;
    }

    public void setShapeDescriptor(ZShapeDescriptor shapeDescriptor) {
        this.shapeDescriptor = shapeDescriptor;
    }

    public long getID() {
        return ID;
    }

    public LineString getGeomLatLon() {
        return geomLatLon;
    }

    public double getArea() {
        return area;
    }

    public double getGSI() {
        return GSI;
    }

    public double getFSI() {
        return FSI;
    }

    public String getCityName() {
        return cityName;
    }

    public double getCityRatio() {
        return cityRatio;
    }

    public List<Long> getBuildingIDs() {
        return buildingIDs;
    }

    public Polygon getShape() {
        return shape;
    }

    public ZShapeDescriptor getShapeDescriptor() {
        return shapeDescriptor;
    }

    public Point getCentroidLatLon() {
        return centroidLatLon;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public Point getCentroid() {
        return centroid;
    }

    /* ------------- draw ------------- */
}
