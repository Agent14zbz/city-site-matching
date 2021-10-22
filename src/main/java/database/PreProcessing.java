package database;

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
        // extract raw blocks and raw cities from database
        int[] blockRawIDs = new int[]{
                1, 2, 3, 4, 5
        };
        List<BlockRaw> blockRawList = manager.collectBlocks(blockRawIDs);
        CityRaw cityRaw = manager.collectCity();

        // create new Block
        this.blockList = new ArrayList<>();
        for (BlockRaw blockRaw : blockRawList) {
            Block block = new Block(blockRaw, cityRaw);
            long id = blockRaw.getID();
            List<BuildingRaw> buildingRaws = manager.collectBuildingInBlock((int) id);
            List<Building> buildings = new ArrayList<>();
            for (BuildingRaw buildingRaw : buildingRaws) {
                Building building = new Building(buildingRaw, blockRaw, cityRaw);
                buildings.add(building);
            }

            block.setBuildings(buildings);
            blockList.add(block);
        }
    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */

    public List<Block> getBlockList() {
        return blockList;
    }

    /* ------------- draw ------------- */
}
