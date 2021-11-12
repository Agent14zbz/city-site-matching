package database;


import elements.*;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import transform.ZTransform;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private PreparedStatement pstmt = null;

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

    /* ------------- data collector ------------- */

    /**
     * collect cities in the database
     *
     * @return elements.CityRaw
     */
    public CityRaw collectCity() {
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select name, lat, lon, ratio, st_astext(boundary), timestamp from city");

            while (rs.next()) {
                CityRaw cityRaw = new CityRaw();

                cityRaw.setName(rs.getString(1).trim());
                cityRaw.setRatio(rs.getDouble(4));
                return cityRaw;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * collect all blocks in the database
     *
     * @param ids ids to collect
     * @return java.util.List<main.Block>
     */
    public List<Block> collectBlocks(int[] ids) {
        List<Block> blocks = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            StringBuilder query = new StringBuilder(
                    String.format(
                            "select id, st_astext(geom), a, gsi, b, \"T_num\", \"T_dense\", \"F_num\", \"F_diversity\", \"M_vec16\" from blocks where id=%d", ids[0]
                    )
            );
            for (int i = 1; i < ids.length; ++i) {
                query.append(" or id=").append(ids[i]);
            }

            ResultSet rs = stmt.executeQuery(query.toString());
            WKTReader reader = new WKTReader();
            while (rs.next()) {
                Block block = new Block();
                block.setID(rs.getLong(1));
                block.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                block.setArea(rs.getDouble(3));
                block.setGSI(rs.getDouble(4));
                blocks.add(block);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return blocks;
    }

    /**
     * collect all buildings in the given block
     *
     * @param id block ID
     * @return java.util.List<elements.Building>
     */
    public List<Building> collectBuildingInBlock(Integer id) {
        List<Building> buildings = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String query = String.format("select buildings.id, st_astext(buildings.geom), name, building_type, timestamp from buildings, blocks where blocks.id=%d and st_contains(ST_MakePolygon(blocks.geom), buildings.geom)", id);

            ResultSet rs = stmt.executeQuery(query);
            WKTReader reader = new WKTReader();
            while (rs.next()) {
                Building b = new Building();
                b.setOsmid(rs.getLong(1));
                b.setGeomLatLon((LineString) reader.read(rs.getString(2)));
                if (rs.getString(3) != null)
                    b.setName(rs.getString(3).trim());
                b.setType(rs.getString(4).trim());

                b.setBlockID(id);

                buildings.add(b);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return buildings;
    }

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
                if (rs.getString(3) != null)
                    b.setName(rs.getString(3).trim());
                b.setType(rs.getString(4).trim());


                buildings.add(b);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return buildings;
    }

    /* ------------- create new table ------------- */

    /**
     * update processed data to a new table
     *
     * @param blockList list of blocks
     */
    public void updateTable(List<Block> blockList) {
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

    /* ------------- setter & getter ------------- */

    public Connection getConn() {
        return conn;
    }

}
