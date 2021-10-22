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
 * @date 2021/10/22
 * @time 16:07
 */
public class BuildingRaw {
    private long id;
    private LineString geom;
    private String name = "";
    private String building_type;

    /* ------------- constructor ------------- */

    public BuildingRaw() {

    }

    /* ------------- member function ------------- */

    /**
     * transform lat-lon coordinate to absolute coordinate (origin at the block centroid)
     *
     * @param ratio scale ratio
     * @return double[][]
     */
    public Polygon getAbsShapePts(Point blockCentroid, double ratio) {
        // lat, lon
        GeoMath geoMath = new GeoMath(blockCentroid.getY(), blockCentroid.getX());
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

    public void setId(long id) {
        this.id = id;
    }

    public void setGeom(LineString geom) {
        this.geom = geom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBuilding_type(String building_type) {
        this.building_type = building_type;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBuilding_type() {
        return building_type;
    }

    /* ------------- draw ------------- */
}
