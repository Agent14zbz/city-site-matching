package database;


import advancedGeometry.ZShapeDescriptor;
import elements.Block;
import elements.BlockMatch;
import elements.Building;
import elements.CityRaw;
import math.ZGeoMath;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.postgresql.util.HStoreConverter;
import transform.ZTransform;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * database manager
 *
 * @author Yichen Mo, ZHANG Baizhou
 * @project city_site_matching
 * @date 2021/10/18
 * @time 18:42
 */
public class DBManager {
    private Connection conn = null;
    private Statement stmt = null;

    /* ------------- constructor ------------- */

    public DBManager() {
        this.conn = connect();
    }

    /* ------------- connect ------------- */

    /**
     * connect to database
     *
     * @return java.sql.Connection
     */
    private Connection connect() {
        conn = null;

        try {
            conn = DriverManager.getConnection(DBConst.URL, DBConst.USERNAME, DBConst.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * shut down connection
     */
    public void closeConnention() {
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /* ------------- matching ------------- */

    public List<Block> getBlockData(int num) {
        List<Block> data = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select id, st_astext(geom), area, gsi, fsi, city_name, city_ratio, building_ids, st_astext(shape), shape_descriptor, axes, axes_new, axes_obb from blocks_new " +
                    "limit " + num);
            WKTReader reader = new WKTReader();

            while (rs.next()) {
                Block block = new Block();
                block.setID(rs.getLong(1));
                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                block.setArea(rs.getDouble(3));
                block.setGSI(rs.getDouble(4));
                block.setFSI(rs.getDouble(5));
                block.setCityName(rs.getString(6));
                block.setCityRatio(rs.getDouble(7));
                Array building_ids = rs.getArray(8);
                List<Long> ids = Arrays.asList((Long[]) building_ids.getArray());
                block.setBuildingIDs(ids);
                block.setShape((Polygon) ZTransform.LineStringToPolygon((LineString) reader.read(rs.getString(9))).reverse());
                Array shape_descriptor = rs.getArray(10);
                Array axes = rs.getArray(11);
                Array axes_new = rs.getArray(12);
                Array axes_obb = rs.getArray(13);
                block.setShapeDescriptor(new ZShapeDescriptor(
                        (Double[]) shape_descriptor.getArray(),
                        (Double[]) axes.getArray(),
                        (Double[]) axes_new.getArray(),
                        (Double[]) axes_obb.getArray()
                ));

                data.add(block);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println(">>> collected " + data.size() + " block data from alphaville");
        return data;
    }

    /* ------------- matching ------------- */

    /**
     * request valid block features from database for matching
     *
     * @param areaRange valid range of block area
     * @param gsiRange  valid range of block GSI
     * @param fsiRange  valid range of block FSI
     * @return java.util.List<elements.BlockMatch>
     */
    public List<BlockMatch> collectBlocksInfo(double[] areaRange, double[] gsiRange, double[] fsiRange) {
        List<BlockMatch> result = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String query =
                    "select id, st_astext(shape), shape_descriptor, axes from blocks_new " +
                            "where area between " + areaRange[0] + " and " + areaRange[1];
//            String query =
//                    "select id, st_astext(shape), shape_descriptor, axes from blocks_new"
//                            + " where area between " + areaRange[0] + " and " + areaRange[1]
//                            + " and gsi between " + gsiRange[0] + " and " + gsiRange[1]
//                            + " and fsi between " + fsiRange[0] + " and " + fsiRange[1];

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                BlockMatch blockMatch = new BlockMatch();
                blockMatch.setId(rs.getLong(1));
                Array arr1 = rs.getArray(3);
                blockMatch.setShapeDescriptor((Double[]) arr1.getArray());
                Array arr2 = rs.getArray(4);
                blockMatch.setAxes((Double[]) arr2.getArray());

                result.add(blockMatch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(">>> collected " + result.size() + " valid block samples");
        return result;
    }

    /**
     * collect the match result from the database
     *
     * @return java.util.List<main.Block>
     */
    public Block collectBlockResult(long id) {
        try {
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("select id, st_astext(geom), area, gsi, fsi, city_name, city_ratio, building_ids, st_astext(shape), shape_descriptor, axes, axes_new, axes_obb from blocks_new where id=" + id);
            WKTReader reader = new WKTReader();
            while (rs.next()) {
                Block block = new Block();
                block.setID(rs.getLong(1));
                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                block.setArea(rs.getDouble(3));
                block.setGSI(rs.getDouble(4));
                block.setFSI(rs.getDouble(5));
                block.setCityName(rs.getString(6));
                block.setCityRatio(rs.getDouble(7));
                Array building_ids = rs.getArray(8);
                List<Long> ids = Arrays.asList((Long[]) building_ids.getArray());
                block.setBuildingIDs(ids);
                block.setShape(ZTransform.LineStringToPolygon((LineString) reader.read(rs.getString(9))));
                Array shape_descriptor = rs.getArray(10);
                Array axes = rs.getArray(11);
                Array axes_new = rs.getArray(12);
                Array axes_obb = rs.getArray(13);
                block.setShapeDescriptor(new ZShapeDescriptor(
                        (Double[]) shape_descriptor.getArray(),
                        (Double[]) axes.getArray(),
                        (Double[]) axes_new.getArray(),
                        (Double[]) axes_obb.getArray()
                ));

                return block;
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * request all valid blocks from database for matching
//     *
//     * @param areaRange valid range of block area
//     * @param gsiRange  valid range of block GSI
//     * @param fsiRange  valid range of block FSI
//     * @return java.util.List<elements.Block>
//     */
//    @Deprecated
//    public List<Block> collectBlocksConstrain(double[] areaRange, double[] gsiRange, double[] fsiRange) {
//        List<Block> result = new ArrayList<>();
//        try {
//            stmt = conn.createStatement();
//            String query =
//                    "select id, st_astext(geom), area, gsi, fsi, city_name, city_ratio, building_ids, st_astext(shape), shape_descriptor, axes, axes_new from blocks_new " +
//                            "where area between " + areaRange[0] + " and " + areaRange[1];
//
//            ResultSet rs = stmt.executeQuery(query);
//            WKTReader reader = new WKTReader();
//            while (rs.next()) {
//                Block block = new Block();
//                block.setID(rs.getLong(1));
//                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
//                block.setArea(rs.getDouble(3));
//                block.setGSI(rs.getDouble(4));
//                block.setFSI(rs.getDouble(5));
//                block.setCityName(rs.getString(6));
//                block.setCityRatio(rs.getDouble(7));
//                Array arr1 = rs.getArray(8);
//                List<Long> ids = Arrays.asList((Long[]) arr1.getArray());
//                block.setBuildingIDs(ids);
//                block.setShape(ZTransform.LineStringToPolygon((LineString) reader.read(rs.getString(9))));
//                Array arr2 = rs.getArray(10);
//                Array arr3 = rs.getArray(11);
//                Array arr4 = rs.getArray(12);
//                block.setShapeDescriptor(new ZShapeDescriptor(
//                        (Double[]) arr2.getArray(), (Double[]) arr3.getArray(), (Double[]) arr4.getArray()
//                ));
//
//                result.add(block);
//            }
//        } catch (SQLException | ParseException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(">>> collected " + result.size() + " valid block samples");
//        return result;
//    }

    /* ------------- pre-processing ------------- */

    /**
     * collected original blocks from database for pre-processing
     *
     * @return java.util.List<elements.Block>
     */
    public List<Block> collectBlocksPre() {
        List<Block> blocks = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String query = "select id, st_astext(geom), city, building_ids, area, gsi, fsi from blocks where blocks.valid_3d = " + true + ";";

            ResultSet rs = stmt.executeQuery(query);
            WKTReader reader = new WKTReader();

            while (rs.next()) {
                Block b = new Block();
                b.setID(rs.getLong(1));
                b.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                b.setCityName(rs.getString(3));
                Array arr = rs.getArray(4);
                List<Long> ids = Arrays.asList((Long[]) arr.getArray());
                b.setBuildingIDs(ids);
                b.setArea(rs.getDouble(5));
                b.setGSI(rs.getDouble(6));
                b.setFSI(rs.getDouble(7));

                blocks.add(b);
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println(">>> collected " + blocks.size() + " blocks from database");
        return blocks;
    }

    /**
     * collected city ratios for each block from database for pre-processing
     *
     * @param cityName name of the city which the block belongs to
     * @return double
     */
    public double collectCityRatioForBlockPre(String cityName) {
        double ratio = 1;
        try {
            stmt = conn.createStatement();
            String query = "select ratio from city where city.name = '" + cityName + "'";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                ratio = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratio;
    }

    /**
     * collected original buildings of each block from database for pre-processing
     *
     * @param buildingIDs IDs of each building which belongs to the block
     * @return java.util.List<elements.Building>
     */
    public List<Building> collectBuildingInBlockPre(List<Long> buildingIDs) {
        List<Building> buildings = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String query = "select id, st_astext(geom), name, building_type, s3db, timestamp from buildings where id = Any(Array[" + StringUtils.join(buildingIDs, ",") + "])";

            ResultSet rs = stmt.executeQuery(query);
            WKTReader reader = new WKTReader();
            while (rs.next()) {
                Building b = new Building();
                b.setOsmid(rs.getLong(1));
                b.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                if (rs.getString(3) != null) {
                    b.setName(rs.getString(3).trim());
                }
                b.setType(rs.getString(4).trim());
                if (rs.getString(5) != null && rs.getString(5).length() > 0) {
                    String s3db = rs.getString(5).trim();
                    Map<String, String> map = HStoreConverter.fromString(s3db);
                    b.setS3db(map);
                }
                b.setTimestamp(rs.getTimestamp(6));

                buildings.add(b);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        System.out.println(buildings);
        return buildings;
    }

    /* ------------- create / update table ------------- */

    /**
     * update processed data to a new table "block_new"
     *
     * @param blockList list of blocks
     */
    public void updateNewTable(List<Block> blockList) {
        System.out.println(">>> starting update table");
        try {
            stmt = conn.createStatement();
            StringBuilder sql = new StringBuilder(
                    "INSERT INTO public.blocks_new"
                            + "(id, geom, area, gsi, fsi, city_name, city_ratio, building_ids, shape, shape_descriptor, axes)"
                            + "VALUES"
            );

            WKTWriter writer = new WKTWriter();

            for (Block b : blockList) {
                long id = b.getID();
                String geom = writer.write(b.getGeomLatLon());
                double area = b.getArea();
                double gsi = b.getGSI();
                double fsi = b.getFSI();
                String city_name = b.getCityName();
                double city_ratio = b.getCityRatio();
                List<Long> building_ids = b.getBuildingIDs();
                String shape = writer.write(ZTransform.PolygonToLineString(b.getShape()).get(0));
                List<Double> shape_descriptor = b.getShapeDescriptor().getDescriptorAsList();
                List<Double> axes = b.getShapeDescriptor().getAxesAsList();

                sql.append("(")
                        .append(id)
                        .append(",").append("'")
                        .append(geom).append("'")
                        .append(",")
                        .append(area)
                        .append(",")
                        .append(gsi)
                        .append(",")
                        .append(fsi)
                        .append(",").append("'")
                        .append(city_name).append("'")
                        .append(",")
                        .append(city_ratio)
                        .append(",").append("'{")
                        .append(StringUtils.join(building_ids, ",")).append("}'")
                        .append(",").append("'")
                        .append(shape).append("'")
                        .append(",").append("'{")
                        .append(StringUtils.join(shape_descriptor, ",")).append("}'")
                        .append(",").append("'{")
                        .append(StringUtils.join(axes, ",")).append("}'")
                        .append(")")
                        .append(",")
                ;
            }
            sql.deleteCharAt(sql.length() - 1);
            stmt.executeUpdate(sql.toString());

            System.out.println(">>> updated table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * collect blocks to update "shape_descriptor" and "axes_new"
     *
     * @return java.util.List<elements.Block>
     */
    public List<Block> collectBlockForUpdate() {
        List<Block> result = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            String query = "select id, st_astext(shape) from blocks_new";
            WKTReader reader = new WKTReader();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Block block = new Block();
                block.setID(rs.getLong(1));
                block.setShape(ZTransform.LineStringToPolygon((LineString) reader.read(rs.getString(2))));

                result.add(block);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        System.out.println(">>> collected " + result.size() + " valid block samples to update");
        return result;
    }

    /**
     * collect blocks to update "shape" and "axes_obb"
     *
     * @return java.util.List<elements.Block>
     */
    public List<Block> collectBlockForUpdate2() {
        List<Block> result = new ArrayList<>();

        try {
            stmt = conn.createStatement();
            String query = "select id, st_astext(geom), city_ratio from blocks_new";
            WKTReader reader = new WKTReader();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Block block = new Block();
                block.setID(rs.getLong(1));
                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                block.setCityRatio(rs.getDouble(3));

                result.add(block);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        System.out.println(">>> collected " + result.size() + " valid block samples to update");
        return result;
    }

    /**
     * perform updating "shape_descriptor" and "axes_new" for each block
     *
     * @param id      id
     * @param newSD   new shape_descriptor
     * @param axesNew new axes
     */
    public void updateTableSDAxesNew(long id, List<Double> newSD, List<Double> axesNew) {
        try {
            stmt = conn.createStatement();
            String sql = "UPDATE public.blocks_new SET"
                    + " shape_descriptor = " + "'{" + StringUtils.join(newSD, ",") + "}'"
                    + " , axes_new = " + "'{" + StringUtils.join(axesNew, ",") + "}'"
                    + "WHERE id = " + id;

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * perform updating "axes" for each block
     *
     * @param id   id
     * @param axes new axes
     */
    public void updateTableAxes(long id, List<Double> axes) {
        try {
            stmt = conn.createStatement();
            String sql = "UPDATE public.blocks_new SET"
                    + " axes = " + "'{" + StringUtils.join(axes, ",") + "}'"
                    + "WHERE id = " + id;

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * perform updating "axes_obb" for each block
     *
     * @param id   id
     * @param axes new axes
     */
    public void updateTableAxesOBB(long id, List<Double> axes, Polygon shape) {
        try {
            stmt = conn.createStatement();
            WKTWriter writer = new WKTWriter();
            String shapeString = writer.write(ZTransform.PolygonToLineString(shape).get(0));
            String sql = "UPDATE public.blocks_new SET"
                    + " axes_obb = " + "'{" + StringUtils.join(axes, ",") + "}'"
                    + ", shape = '" + shapeString + "'"
                    + " WHERE id = " + id;
            System.out.println(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ------------- deprecated collector ------------- */

//    /**
//     * collect cities in the database
//     *
//     * @return elements.CityRaw
//     */
//    public CityRaw collectCity() {
//        try {
//            stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("select name, lat, lon, ratio, st_astext(boundary), timestamp from city");
//
//            while (rs.next()) {
//                CityRaw cityRaw = new CityRaw();
//
//                cityRaw.setName(rs.getString(1).trim());
//                cityRaw.setRatio(rs.getDouble(4));
//                return cityRaw;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * collect all blocks in the database
//     *
//     * @param ids ids to collect
//     * @return java.util.List<main.Block>
//     */
//    public List<Block> collectBlocks(int[] ids) {
//        List<Block> blocks = new ArrayList<>();
//        try {
//            stmt = conn.createStatement();
//            StringBuilder query = new StringBuilder(
//                    String.format(
//                            "select id, st_astext(geom), a, gsi, b, \"T_num\", \"T_dense\", \"F_num\", \"F_diversity\", \"M_vec16\" from blocks where id=%d", ids[0]
//                    )
//            );
//            for (int i = 1; i < ids.length; ++i) {
//                query.append(" or id=").append(ids[i]);
//            }
//
//            ResultSet rs = stmt.executeQuery(query.toString());
//            WKTReader reader = new WKTReader();
//            while (rs.next()) {
//                Block block = new Block();
//                block.setID(rs.getLong(1));
//                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
//                block.setArea(rs.getDouble(3));
//                block.setGSI(rs.getDouble(4));
//                blocks.add(block);
//            }
//        } catch (SQLException | ParseException e) {
//            e.printStackTrace();
//        }
//        return blocks;
//    }
//
//    /**
//     * collect all buildings in the given block
//     *
//     * @param id block ID
//     * @return java.util.List<elements.Building>
//     */
//    public List<Building> collectBuildingInBlock(Integer id) {
//        List<Building> buildings = new ArrayList<>();
//        try {
//            stmt = conn.createStatement();
//            String query = String.format("select buildings.id, st_astext(buildings.geom), name, building_type, timestamp from buildings, blocks where blocks.id=%d and st_contains(ST_MakePolygon(blocks.geom), buildings.geom)", id);
//
//            ResultSet rs = stmt.executeQuery(query);
//            WKTReader reader = new WKTReader();
//            while (rs.next()) {
//                Building b = new Building();
//                b.setOsmid(rs.getLong(1));
//                b.setGeomLatLon((LineString) reader.read(rs.getString(2)));
//                if (rs.getString(3) != null)
//                    b.setName(rs.getString(3).trim());
//                b.setType(rs.getString(4).trim());
//
//                b.setBlockID(id);
//                buildings.add(b);
//            }
//        } catch (SQLException | ParseException e) {
//            e.printStackTrace();
//        }
//        return buildings;
//    }

    /* ------------- setter & getter ------------- */

    public Connection getConn() {
        return conn;
    }

}
