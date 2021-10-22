package elements;


import basicGeometry.ZFactory;
import basicGeometry.ZPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import transform.ZTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/19
 * @time 15:38
 */
public class Building {
    // properties
    private long osmid;
    private String name = "";
    private String type;
    private Polygon baseShape;
    private long blockID;

    // geometry properties
    private long storey;
    private List<Polygon> faces;

    // constant
    private static final double storeyHeight = 3.6;

    /* ------------- constructor ------------- */

    public Building(BuildingRaw buildingRaw, BlockRaw blockRaw, CityRaw cityRaw) {
        this.osmid = buildingRaw.getId();
        this.name = buildingRaw.getName();
        this.type = buildingRaw.getBuilding_type();
        this.baseShape = buildingRaw.getAbsShapePts(blockRaw.getCentroidLatLon(), cityRaw.getRatio());
        this.blockID = blockRaw.getID();
    }

    /* ------------- member function ------------- */

    /**
     * generate all faces of a building
     *
     * @param
     * @return void
     */
    public void generateVolume() {
        double height = storey * storeyHeight;
        this.faces = new ArrayList<>();
        faces.add(baseShape);
        for (int i = 0; i < baseShape.getNumPoints() - 1; i++) {
            Coordinate[] coords = new Coordinate[5];
            coords[0] = baseShape.getCoordinates()[i];
            coords[1] = baseShape.getCoordinates()[(i + 1) % (baseShape.getNumPoints() - 1)];
            coords[2] = new Coordinate(coords[1].getX(), coords[1].getY(), height);
            coords[3] = new Coordinate(coords[0].getX(), coords[0].getY(), height);
            coords[4] = coords[0];
            Polygon face = ZFactory.jtsgf.createPolygon(coords);
            faces.add(face);
        }
        Coordinate[] topFaceCoords = new Coordinate[this.baseShape.getNumPoints()];
        for (int i = 0; i < baseShape.getNumPoints(); i++) {
            topFaceCoords[i] = new Coordinate(baseShape.getCoordinates()[i].getX(), baseShape.getCoordinates()[i].getY(), height);
        }
        Polygon topFace = ZFactory.jtsgf.createPolygon(topFaceCoords);
        faces.add(topFace);
    }

    public void rotateAlongVec(ZPoint vec) {
        this.faces = new ArrayList<>();

    }

    /* ------------- setter & getter ------------- */

    public void setBaseShape(Polygon baseShape) {
        this.baseShape = baseShape;
    }

    public void setStorey(long storey) {
        this.storey = storey;
    }

    public Polygon getBaseShape() {
        return baseShape;
    }

    /* ------------- draw ------------- */

}
