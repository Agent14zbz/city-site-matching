package database;

import elements.*;
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
                block.setBuildingArea(rs.getDouble(5));
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

    /* ------------- create new table ------------- */

    public void createTable() {
        try {
            stmt = conn.createStatement();
            String sql = "CREATE TABLE student " +
                    "(id INTEGER not NULL, " +
                    " first VARCHAR(255), " +
                    " last VARCHAR(255), " +
                    " age INTEGER, " +
                    " PRIMARY KEY ( id ))";
            stmt.executeUpdate(sql);

            System.out.println("created table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* ------------- setter & getter ------------- */

    public Connection getConn() {
        return conn;
    }

}
