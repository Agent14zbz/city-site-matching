package main;

import database.DBManager;
import elements.Block;
import elements.Building;
import igeo.*;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import transform.ZJtsTransform;
import transform.ZTransform;

import java.sql.SQLException;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2022/11/1
 * @time 11:01
 */
public class ModelOutput extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    private DBManager dbManager;
    private List<Block> blockData;

    private int DATA_NUM = 100;


    /* ------------- setup ------------- */

    public void setup() {
        long startTime = System.currentTimeMillis();

        this.dbManager = new DBManager();

        this.blockData = dbManager.getBlockData(DATA_NUM);
        for (int i = 0; i < blockData.size(); ++i) {
            Block b = blockData.get(i);
            System.out.println(ZTransform.PolygonToWB_Polygon(b.getShape()).getNormal());
            List<Long> buildingIDs = b.getBuildingIDs();
            List<Building> buildings = dbManager.collectBuildingInBlockPre(buildingIDs);
            b.setBuildings(buildings);
            double buildingAreaAll = 0;
            int building3DNum = 0;
            for (Building building : buildings) {
                building.setBlockID(b.getID());
                building.setBlock(b);
                building.generateAbsShape(b.getCentroidLatLon(), b.getCityRatio());
                building.cal3DInfo();
                double area = building.getBuildingArea();
                if (area > 0) {
                    buildingAreaAll += area;
                    building3DNum++;
                }
                building.transformBase(new ZJtsTransform());
                building.generateVolume(buildingAreaAll, building3DNum);
            }
        }

        save3dm();
    }

    private void save3dm() {
        for (int i = 0; i < blockData.size(); i++) {
            Block b = blockData.get(i);
            String filename = String.valueOf(b.getID());

            IG.init();
            IG.unit(IUnit.Type.Meters);
            PolygonToISurface(b.getShape()).layer("blockShape");
            System.out.println(b.getBuildings().size());
            for (Building building : b.getBuildings()) {
                for (Polygon p : building.getFaces()) {
                    PolygonToISurface(p).layer("buildingShape");
                }
            }
            IG.save("E:/AAA_Study/202210_Green&LowCarbon/20221101_test_real_block/" + filename + ".3dm");
        }
    }

    private ISurface PolygonToISurface(final Polygon poly) {
        IVec[] vecs = new IVec[poly.getNumPoints()];
        for (int i = 0; i < vecs.length; i++) {
            if (Double.isNaN(poly.getCoordinates()[i].getZ())) {
                vecs[i] = new IVec(
                        poly.getCoordinates()[i].getX(),
                        poly.getCoordinates()[i].getY(),
                        0
                );
            } else {
                vecs[i] = new IVec(
                        poly.getCoordinates()[i].getX(),
                        poly.getCoordinates()[i].getY(),
                        poly.getCoordinates()[i].getZ()
                );
            }
        }
        return new ISurface(vecs);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
    }


}
