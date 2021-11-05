package main;

import basicGeometry.ZPoint;
import database.DBManager;
import elements.BlockEmpty;
import elements.Building;
import math.ZGeoMath;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/11/1
 * @time 14:47
 */
public class MatchManager {

    /* ------------- constructor ------------- */

    public MatchManager() {
        loadVectors();
    }

    /* ------------- member function ------------- */

    public void loadVectors() {

    }

    public void matchTest(BlockEmpty empty) {

    }

    public List<Building> generateBuildings(long bestBlockID, DBManager manager) {
        // extract buildings data from the best-match case
        List<Building> buildings = new ArrayList<>();

        return buildings;
    }

    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
