package elements;

import basicGeometry.ZFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import utils.GeoMath;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/18
 * @time 19:16
 */
public class BlockRaw {
    // properties from database
    private long ID;
    private double area;
    private double buildingArea;
    private double GSI;
    private LineString geom;

    private Point centroidLatLon;

    /* ------------- constructor ------------- */

    public BlockRaw() {

    }

    /* ------------- member function ------------- */

    /**
     * transform lat-lon coordinate to absolute coordinate (origin at the block centroid)
     *
     * @param ratio scale ratio
     * @return double[][]
     */
    public Polygon getAbsShape(double ratio) {
        // lat, lon
        GeoMath geoMath = new GeoMath(centroidLatLon.getY(), centroidLatLon.getX());
        geoMath.setRatio(ratio);

        Coordinate[] coords = geom.getCoordinates();
        double[][] absPts = new double[coords.length][];
        for (int i = 0; i < coords.length; i++) {
            double[] xy = geoMath.latLngToXY(coords[i].getY(), coords[i].getX());
            absPts[i] = xy;
        }

        Coordinate[] absCoords = new Coordinate[absPts.length];
        for (int i = 0; i < absCoords.length; i++) {
            absCoords[i] = new Coordinate(absPts[i][0], absPts[i][1]);
        }
        return ZFactory.jtsgf.createPolygon(absCoords);
    }

    /* ------------- setter & getter ------------- */

    public void setID(long ID) {
        this.ID = ID;
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

    public void setGeom(LineString geom) {
        this.geom = geom;
        this.centroidLatLon = geom.getCentroid();
    }

    public long getID() {
        return ID;
    }

    public double getArea() {
        return area;
    }

    public double getBuildingArea() {
        return buildingArea;
    }

    public double getGSI() {
        return GSI;
    }

    public LineString getGeom() {
        return geom;
    }

    public Point getCentroidLatLon() {
        return centroidLatLon;
    }

    /* ------------- draw ------------- */

    @Override
    public String toString() {
        return "Block{" +
                "ID=" + ID +
                ", shape=" + geom +
                ", area=" + area +
                ", buildingArea=" + buildingArea +
                ", GSI=" + GSI +
                '}';
    }
}
