package elements;

import basicGeometry.ZFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import utils.GeoMath;

import java.util.Date;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/19
 * @time 15:38
 */
public class Building {
    private long osmid;
    private LineString geomLatLon;
    private String name = "";
    private String type;
    private String s3db;
    private Date timestamp;

    private long blockID;
    private Polygon baseShape;

//    private List<Polygon> faces;
//    private long storey;
//    private static final double storeyHeight = 3.6;

    /* ------------- constructor ------------- */

    public Building() {
//        this.storey = ZMath.randomInt(2, 7);
    }

    /* ------------- member function ------------- */

    /**
     * transform lat-lon coordinate to absolute coordinate (origin at the block centroid)
     *
     * @param cityRatio scale ratio
     */
    public void generateAbsShape(Point blockCentroidLatLon, double cityRatio) {
        // lat, lon
        GeoMath geoMath = new GeoMath(blockCentroidLatLon.getY(), blockCentroidLatLon.getX());
        geoMath.setRatio(cityRatio);

        Coordinate[] coords = geomLatLon.getCoordinates();
        double[][] absPts = new double[coords.length][];
        for (int i = 0; i < coords.length; i++) {
            double[] xy = geoMath.latLngToXY(coords[i].getY(), coords[i].getX());
            absPts[i] = xy;
        }

        Coordinate[] absCoords = new Coordinate[absPts.length];
        for (int i = 0; i < absCoords.length; i++) {
            absCoords[i] = new Coordinate(absPts[i][0], absPts[i][1], 0);
        }
        this.baseShape = ZFactory.jtsgf.createPolygon(absCoords);
    }

//    /**
//     * generate all faces of a building
//     */
//    public void generateVolume() {
//        double height = storey * storeyHeight;
//        this.faces = new ArrayList<>();
//        faces.add(baseShape);
//        for (int i = 0; i < baseShape.getNumPoints() - 1; i++) {
//            Coordinate[] coords = new Coordinate[5];
//            coords[0] = baseShape.getCoordinates()[i];
//            coords[1] = baseShape.getCoordinates()[(i + 1) % (baseShape.getNumPoints() - 1)];
//            coords[2] = new Coordinate(coords[1].getX(), coords[1].getY(), height);
//            coords[3] = new Coordinate(coords[0].getX(), coords[0].getY(), height);
//            coords[4] = coords[0];
//            Polygon face = ZFactory.jtsgf.createPolygon(coords);
//            faces.add(face);
//        }
//        Coordinate[] topFaceCoords = new Coordinate[this.baseShape.getNumPoints()];
//        for (int i = 0; i < baseShape.getNumPoints(); i++) {
//            topFaceCoords[i] = new Coordinate(baseShape.getCoordinates()[i].getX(), baseShape.getCoordinates()[i].getY(), height);
//        }
//        Polygon topFace = ZFactory.jtsgf.createPolygon(topFaceCoords);
//        faces.add(topFace);
//    }

//    /**
//     * transform by giving matrix
//     *
//     * @param transform transform matrix
//     * @return java.util.List<org.locationtech.jts.geom.Polygon>
//     */
//    public List<Polygon> transform(ZJtsTransform transform) {
//        List<Polygon> newFaces = new ArrayList<>();
//        for (Polygon f : faces) {
//            newFaces.add((Polygon) transform.applyToGeometry3D(f));
//        }
//        return newFaces;
//    }

    /* ------------- setter & getter ------------- */

    public void setOsmid(long osmid) {
        this.osmid = osmid;
    }

    public void setGeomLatLon(LineString geomLatLon) {
        this.geomLatLon = geomLatLon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setS3db(String s3db) {
        this.s3db = s3db;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setBlockID(long blockID) {
        this.blockID = blockID;
    }

    public void setBaseShape(Polygon baseShape) {
        this.baseShape = baseShape;
    }

//    public void setStorey(long storey) {
//        this.storey = storey;
//    }

    public Polygon getBaseShape() {
        return baseShape;
    }


//    public List<Polygon> getFaces() {
//        return faces;
//    }

    /* ------------- draw ------------- */

}