package main;

import database.DBManager;
import elements.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/21
 * @time 17:04
 */
public class PreProcessing {
    private List<Block> blockList;

    // utils

    /* ------------- constructor ------------- */

    public PreProcessing(DBManager manager) {
        // extract blocks, cities and buildings in block from database
        int[] blockRawIDs = new int[]{
                1, 2, 3, 4, 5
        };
        this.blockList = manager.collectBlocks(blockRawIDs);
        CityRaw cityRaw = manager.collectCity();
        for (Block block : blockList) {
            block.setCityName(cityRaw.getName());

            long id = block.getID();
            List<Building> buildingList = manager.collectBuildingInBlock((int) id);
            for (Building building : buildingList) {
                building.generateAbsShape(block.getCentroidLatLon(), cityRaw.getRatio());
                building.generateVolume();
            }
            block.setBuildings(buildingList);

            block.generateAbsShape(cityRaw.getRatio());
            block.initProperties();
        }

        // create new table to store new block properties
//        manager.createTable();
    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public List<Block> getBlockList() {
        return blockList;
    }

    /* ------------- draw ------------- */
}
