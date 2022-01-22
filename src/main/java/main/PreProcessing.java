package main;

import database.DBManager;
import elements.Block;
import elements.Building;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * data pre-processing
 *
 * @author ZHANG Baizhou zhangbz
 * @project city_site_matching
 * @date 2021/10/21
 * @time 17:04
 */
public class PreProcessing {
    private List<Block> blockList;
    private List<Block> blocksToUpdate;

    /* ------------- constructor ------------- */

    double[] areas;
    double[] minmax;

    public PreProcessing(DBManager manager) {
//        // extract blocks, cities and buildings in block from database
//        List<Integer> idList = new ArrayList<>();
//        try {
//            File f = new File("./src/main/resources/validBlocks.txt");
//            BufferedReader reader = new BufferedReader(new FileReader(f));
//            String line = reader.readLine();
//            System.out.println(">>>> start collecting block samples");
//            double cnt = 0;
//
//            while (line != null) {
//                idList.add(Integer.parseInt(line));
//                line = reader.readLine();
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int[] ids = new int[idList.size()];
//        for (int i = 0; i < ids.length; i++) {
//            ids[i] = idList.get(i);
//        }
//        this.blockList = manager.collectBlocks(ids);


        /*temp*/
////        writeAreaDataTemp();
//        List<Double> areaList = readAreaDataTemp();
//        areas = new double[areaList.size()];
//        for (int i = 0; i < areas.length; i++) {
//            areas[i] = areaList.get(i);
//        }
//        System.out.println(">>>> all area data numbers: " + areas.length);
//        Arrays.sort(areas);
//        System.out.println(areas[(int) (areas.length * 0.9)]);
//        int groupNum = 8;
//        double max_ = 36000;
//        double max = Arrays.stream(areas).min().getAsDouble();
//        double min = Arrays.stream(areas).min().getAsDouble();
//
//        double divide = (max_ - min) / groupNum;
//        minmax = new double[groupNum + 1];
//        minmax[0] = min;
//        minmax[minmax.length - 1] = max;
//        for (int i = 1; i < groupNum; i++) {
//            minmax[i] = min + divide * i;
//        }
//
//        List<List<Double>> sortResult = new ArrayList<>();
//        for (int i = 0; i < groupNum; i++) {
//            sortResult.add(new ArrayList<>());
//        }
//        for (int i = 0; i < areas.length; i++) {
//            double a = areas[i];
//            int groupIndex = (int) Math.floor((a - min) / divide);
//            if (groupIndex > groupNum - 1) {
//                sortResult.get(sortResult.size() - 1).add(a);
//            } else {
//                sortResult.get(groupIndex).add(a);
//            }
//        }
//
//        for (int i = 0; i < groupNum; i++) {
//            System.out.println(">>> group " + i + " | " + String.format("%.2f", min + i * divide) + " ~ " + String.format("%.2f", (min + (i + 1) * divide)));
//            System.out.println("sample number: " + sortResult.get(i).size() + "   ratio: " + String.format("%.2f", ((double) sortResult.get(i).size() / (double) areas.length)));
//        }
        /*temp*/

//        CityRaw cityRaw = manager.collectCity();
//        for (Block block : blockList) {
//            block.setCityName(cityRaw.getName());
//
//            long id = block.getID();
//            List<Building> buildingList = manager.collectBuildingInBlock((int) id);
//            for (Building building : buildingList) {
//                building.generateAbsShape(block.getCentroidLatLon(), cityRaw.getRatio());
//                building.generateVolume();
//            }
//            block.setBuildings(buildingList);
//
//            block.generateAbsShape(cityRaw.getRatio());
//            block.initProperties();
//        }


    }

    /* ------------- member function ------------- */

    /**
     * collect all raw blocks for pre-processing
     *
     * @param dbManager database manager
     */
    public void initBlocks(DBManager dbManager) {
        // collect all blocks from database
        this.blockList = dbManager.collectBlocksPre();

        // initialize other relative properties from other database table
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            String cityName = block.getCityName();
            block.setCityRatio(dbManager.collectCityRatioForBlockPre(cityName));

            List<Long> buildingIDStrings = block.getBuildingIDs();
            List<Building> buildings = dbManager.collectBuildingInBlockPre(buildingIDStrings);
            for (Building building : buildings) {
                building.setBlockID(block.getID());
                building.generateAbsShape(block.getCentroidLatLon(), block.getCityRatio());
            }
            block.setBuildings(buildings);

            block.generateAbsShape();
            block.initShapeDescriptor();

            System.out.println(">>> " + i + " blocks processed");
        }
    }

    /**
     * create a new table to store data
     *
     * @param dbManager database manager
     */
    public void updateDatabase(DBManager dbManager) {
        dbManager.updateNewTable(blockList);
    }

    /**
     * collect all blocks for updating
     *
     * @param dbManager database manager
     */
    public void getBlocksToUpdate(DBManager dbManager) {
        this.blocksToUpdate = dbManager.collectBlockForUpdate();
    }

    /**
     * collect all blocks for updating 2
     *
     * @param dbManager database manager
     */
    public void getBlocksToUpdate2(DBManager dbManager) {
        this.blocksToUpdate = dbManager.collectBlockForUpdate2();
    }

    /**
     * update shape descriptors and new axes to database
     *
     * @param dbManager database manager
     */
    public void updateSDAxes(DBManager dbManager) {
        for (Block b : blocksToUpdate) {
            b.initShapeDescriptor();
            dbManager.updateTableSDAxesNew(
                    b.getID(),
                    b.getShapeDescriptor().getDescriptorAsList(),
                    b.getShapeDescriptor().getAxesNewAsList()
            );

            System.out.println(">>> updated shape_descriptor and axes_new for block " + b.getID());
        }
    }

    /**
     * update axes to database
     *
     * @param dbManager database manager
     */
    public void updateAxes(DBManager dbManager) {
        for (Block b : blocksToUpdate) {
            b.initShapeDescriptor();
            dbManager.updateTableAxes(
                    b.getID(),
                    b.getShapeDescriptor().getAxesAsList()
            );

            System.out.println(">>> updated axes for block " + b.getID());
        }
    }

    /**
     * update shape and axes_obb to database
     *
     * @param dbManager database manager
     */
    public void updateShapeAxesOBB(DBManager dbManager) {
        for (Block b : blocksToUpdate) {
            b.generateAbsShape();
            b.initShapeDescriptor();
            dbManager.updateTableAxesOBB(
                    b.getID(),
                    b.getShapeDescriptor().getAxesOBBAsList(),
                    b.getShape()
            );

            System.out.println(">>> updated shape and axes_obb for block " + b.getID());
        }
    }

    /* ------------- temp: area statistic ------------- */

    private void writeAreaDataTemp() {
        // temp: area
        double[] area = new double[blockList.size()];
        for (int i = 0; i < blockList.size(); i++) {
            double a = blockList.get(i).getArea();
            area[i] = a;
        }
        try {
            File file = new File("./src/main/resources/areaTemp.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < area.length - 1; i++) {
                String areaString = String.format("%.2f", area[i]);
                writer.write(areaString);
                writer.newLine();
            }
            String areaString = String.format("%.2f", area[area.length - 1]);
            writer.write(areaString);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Double> readAreaDataTemp() {
        List<Double> areas = new ArrayList<>();
        try {
            File f = new File("./src/main/resources/areaTemp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();

            while (line != null) {
                areas.add(Double.parseDouble(line));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return areas;
    }

    /* ------------- setter & getter ------------- */

}
