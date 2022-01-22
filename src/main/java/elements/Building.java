package elements;

import basicGeometry.ZFactory;
import math.ZMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import transform.ZJtsTransform;
import transform.ZTransform;
import utils.GeoMath;

import java.util.*;

/**
 * building object
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
    private Map<String, String> s3db;

    private Date timestamp;

    private long blockID;
    private Block block;
    private Polygon baseShape;
    private double baseArea;

    private double height;
    private double buildingArea;

    private Polygon baseShapeTrans;
    private List<Polygon> faces;

    private static final double storeyHeight = 3.6;

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
        this.baseArea = baseShape.getArea();
    }

    /**
     * calculate 3D information
     */
    public void cal3DInfo() {
        // parse s3db to get height or building levels
        if (s3db != null) {
            for (String s : s3db.keySet()) {
                if (s.equals("building:levels")) {
                    int level = Integer.parseInt(s3db.get("building:levels"));
                    if (level > 0) {
                        this.buildingArea = level * baseArea;
                        this.height = level * storeyHeight;
                    } else {
                        this.buildingArea = baseArea;
                        this.height = storeyHeight;
                    }
                    break;
                } else if (s.equals("height")) {
                    this.height = Double.parseDouble(s3db.get("height"));
                    int level = (int) Math.round(height / storeyHeight);
                    if (level > 0) {
                        this.buildingArea = level * baseArea;
                    } else {
                        this.buildingArea = baseArea;
                    }
                    break;
                } else {
                    this.height = 0;
                    this.buildingArea = 0;
                }
            }
        }
    }

    /**
     * transform base shape by given ZJtsTransform
     *
     * @param transform input ZJtsTransform
     */
    public void transformBase(ZJtsTransform transform) {
        this.baseShapeTrans = (Polygon) transform.applyToGeometry2D(baseShape);
    }

    /**
     * generate all faces of a building
     */
    public void generateVolume(double buildingAreaAll, int building3DNum) {
        this.faces = new ArrayList<>();
        ZTransform.validateGeometry3D(baseShapeTrans);
        faces.add(baseShapeTrans);

//        // parse s3db to get height or building levels
//        if (s3db != null) {
//            for (String s : s3db.keySet()) {
//                if (s.equals("height")) {
//                    flag = true;
//                    height = Double.parseDouble(s3db.get("height"));
//                    break;
//                } else if (s.equals("building:levels")) {
//                    flag = true;
//                    int level = Integer.parseInt(s3db.get("building:levels"));
//                    height = level * storeyHeight;
//                    break;
//                }
//            }
//        }

        // if s3db contains height or building levels, generate the volume accordingly
        // if s3db doesn't, generate the volume approximately according to block fsi
        if (height == 0) {
            double blockFSI = block.getFSI();
            double buildingAreaByFSI = blockFSI * block.getArea();
            double areaOfOthers = buildingAreaByFSI - buildingAreaAll;
            int numOfOthers = block.getBuildings().size() - building3DNum;

            double buildingBaseAreaAll = block.getGSI() * block.getArea();

            double buildingArea = areaOfOthers * (baseArea / buildingBaseAreaAll);
            double randomBuildingArea = buildingArea * ZMath.random(0.9, 1.1);

            this.buildingArea = randomBuildingArea;
            int level = (int) Math.round(randomBuildingArea / baseArea);
            if (level > 0) {
                this.height = level * storeyHeight;
            } else {
                this.height = storeyHeight;
            }
        }
        for (int i = 0; i < baseShapeTrans.getNumPoints() - 1; i++) {
            Coordinate[] coords = new Coordinate[5];
            coords[0] = baseShapeTrans.getCoordinates()[i];
            coords[1] = baseShapeTrans.getCoordinates()[(i + 1) % (baseShapeTrans.getNumPoints() - 1)];
            coords[2] = new Coordinate(coords[1].getX(), coords[1].getY(), height);
            coords[3] = new Coordinate(coords[0].getX(), coords[0].getY(), height);
            coords[4] = coords[0];
            Polygon face = ZFactory.jtsgf.createPolygon(coords);
            faces.add(face);
        }
        Coordinate[] topFaceCoords = new Coordinate[this.baseShapeTrans.getNumPoints()];
        for (int i = 0; i < baseShapeTrans.getNumPoints(); i++) {
            topFaceCoords[i] = new Coordinate(baseShapeTrans.getCoordinates()[i].getX(), baseShapeTrans.getCoordinates()[i].getY(), height);
        }
        Polygon topFace = ZFactory.jtsgf.createPolygon(topFaceCoords);
        faces.add(topFace);
    }

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

    public void setS3db(Map<String, String> s3db) {
        this.s3db = s3db;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setBlockID(long blockID) {
        this.blockID = blockID;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setBaseShape(Polygon baseShape) {
        this.baseShape = baseShape;
    }

    public Polygon getBaseShape() {
        return baseShape;
    }

    public List<Polygon> getFaces() {
        return faces;
    }

    public double getBuildingArea() {
        return buildingArea;
    }

    /* ------------- draw ------------- */

}