package database;

import main.Block;
import main.Building;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.sql.*;
import java.util.ArrayList;
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
     * @param
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
     *
     * @param
     * @return void
     */
    public void closeConnention() {
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /* ------------- connect ------------- */

    /**
     * create a new table in the database
     *
     * @param
     * @return void
     */
    public void createNewTable() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(DBConst.URL, DBConst.USERNAME, DBConst.PASSWORD);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE TEST " +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " AGE            INT     NOT NULL, " +
                    " ADDRESS        CHAR(50), " +
                    " SALARY         REAL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    /* ------------- data collector ------------- */

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
                block.setShape((LineString) reader.read(rs.getString(2)));
                block.setArea(rs.getDouble(3));
                block.setGSI(rs.getDouble(4));
                block.setBuildingArea(rs.getDouble(5));
                blocks.add(block);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return blocks;
    }

    public Block collectBlockInfo(Integer id) {
        try {
            stmt = conn.createStatement();
            String query = String.format("select id, a, gsi, b, \"T_num\", \"T_dense\", \"F_num\", \"F_diversity\" from blocks where blocks.id=%d", id);

            ResultSet rs = stmt.executeQuery(query);
            WKTReader reader = new WKTReader();
            rs.next();
            Block block = new Block();
            block.setID(rs.getLong(1));
            block.setShape((LineString) reader.read(rs.getString(2)));
            block.setArea(rs.getDouble(3));
            block.setGSI(rs.getDouble(4));
            block.setBuildingArea(rs.getDouble(5));
            return block;
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Building> collectBuildingInBlock(Integer id) {
        List<Building> buildings = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String query = String.format("select buildings.id, st_astext(buildings.geom), name, building_type, timestamp from buildings, blocks where blocks.id=%d and st_contains(ST_MakePolygon(blocks.geom), buildings.geom)", id);

            ResultSet rs = stmt.executeQuery(query);
            WKTReader reader = new WKTReader();
            while (rs.next()) {
                Building building = new Building();
                building.setOsmid(rs.getLong(1));
                building.setShape((LineString) reader.read(rs.getString(2)));
                if (rs.getString(3) != null) {
                    building.setName(rs.getString(3).trim());
                }
                building.setType(rs.getString(4).trim());
                buildings.add(building);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return buildings;
    }

    /* ------------- setter & getter ------------- */

    public Connection getConn() {
        return conn;
    }

}
