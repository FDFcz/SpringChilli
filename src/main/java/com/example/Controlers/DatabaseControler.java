package com.example.Controlers;

import com.example.Structures.*;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class DatabaseControler {

    private static DataSource dataSource;
    public Boolean initDB()
    {
        dataSource = createSource();
        try(Connection connection = getConection())
        {
            System.out.println("sucess");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:mariadb://localhost:3306/chilly", "ChilliUser", "Chilli321");
    }

    public int registryNewUser(String name, String password)
    {
        try(Connection connection = getConection())
        {
            PreparedStatement ps = connection.prepareStatement("insert into customer (username,password)  VALUES (?,?)");
            ps.setString(1, name);
            ps.setString(2, password);
            int result = ps.executeUpdate();
            return getUserID(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String[][] getAllTAble(String nameOfTable)
    {
        try(Connection connection = getConection())
        {
            String avoidPreparedStatement = "SELECT * FROM "+nameOfTable+";";
            PreparedStatement ps = connection.prepareStatement(avoidPreparedStatement);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();
            List<String> stringStream = new ArrayList<>();
            while(rs.next())
               for (int i=1;i<=columnCount;i++) {
                       switch (metadata.getColumnTypeName(i)) {
                           case "CHARACTER VARYING","TIMESTAMP","VARCHAR":
                               stringStream.add(rs.getString(i));
                               break;
                           case "INTEGER","BIGINT":
                               stringStream.add(String.valueOf(rs.getInt(i)));
                               break;
                           case "BOOLEAN","BIT":
                               stringStream.add(String.valueOf(rs.getBoolean(i)));
                               break;
                           case "FLOAT","DOUBLE PRECISION":
                               stringStream.add(String.valueOf(rs.getFloat(i)));
                               break;
                           default:
                               System.out.println(metadata.getColumnTypeName(i));
                               break;
                   }
               }
            String[][] returnDictionary = new String[stringStream.size()/columnCount+1][columnCount];
            for (int i = 0; i < columnCount; i++) {
                returnDictionary[0][i]= metadata.getColumnName(i+1);
            }
            int n =0;
            for (int i = 0; i < returnDictionary.length-1; i++) {
                for (int j = 0; j < returnDictionary[0].length; j++) {
                    returnDictionary[i+1][j] = stringStream.get(n++);
                }
            }
            return returnDictionary;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private static DataSource createSource()
    {
        HikariDataSource ds = new HikariDataSource();
        //ds.setJdbcUrl("jdbc:mariadb://localhost:3306/chilly");
        return ds;
    }
    public String getUserPassword(int userID){return getUserPassword(getUser(userID).getUserName());}
    public String getUserPassword(String username)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customer WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString("password");
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Date getTokenExpirationDate(int id)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT tokenExpiration FROM customer WHERE customer_id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getDate("tokenExpiration");
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getUserToken(int userID){
        Customer user = getUser(userID);
        if(user == null) return null;
        return getUserToken(user.getUserName());
    }
    public String getUserToken(String username)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT token FROM customer WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString("token");
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void setCustomerToken(int userID, String token, Date expiration)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE customer SET token = ?,tokenExpiration=? WHERE customer_id = ?");
            ps.setString(1,token);
            ps.setDate(2, expiration);
            ps.setInt(3, userID);
            int result = ps.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int getUserID(String username)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customer WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt("customer_id");
            return -1;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Customer getUser(int userID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customer WHERE customer_id = ?");
            ps.setString(1, String.valueOf(userID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return new Customer(userID,rs.getString("username"));
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Customer getUserWithTeracotas(int userID)
    {
        try(Connection connection = getConection()) {
            Customer retUser;
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM customer WHERE customer_id = ?");
            ps.setString(1, String.valueOf(userID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) retUser = new Customer(userID,rs.getString("username"));
            else return null;
            ps = connection.prepareStatement("SELECT * FROM terracotta WHERE owner = ?");
            ps.setInt(1,userID);
            rs = ps.executeQuery();
            while(rs.next()) retUser.addTeracota(new Teracota(rs.getInt("terracotta_id"),rs.getString("name"), Teracota.PlantTypes.values()[rs.getInt("plant")], rs.getDate("planted_at")));
            return retUser;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void changeUser(int userID, String userName, String password)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE customer SET username = ?, password = ? WHERE customer_id = ?");
            ps.setString(1,userName);
            ps.setString(2, password);
            ps.setInt(3, userID );
            int result = ps.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Teracota addTeracota(int userID, String teracotaName,int plantID)
    {
        Teracota insertedTeracota = null;
        try(Connection connection = getConection())
        {
            PreparedStatement ps = connection.prepareStatement("insert into terracotta (name,owner,plant,planted_at,PLC)  VALUES (?,?,?,?,1)",Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,teracotaName);
            ps.setInt(2, userID);
            ps.setInt(3, plantID);
            Date curnetDate =Date.valueOf(LocalDate.now());
            ps.setDate(4, curnetDate);
            int result = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) {
                insertedTeracota = new Teracota(rs.getInt(1), teracotaName, Teracota.PlantTypes.values()[plantID], curnetDate);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return insertedTeracota;
    }
    public Teracota getTeracota(int teraID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM terracotta WHERE terracotta_id = ?");
            ps.setString(1, String.valueOf(teraID));
            ResultSet rs = ps.executeQuery();
            Teracota newTeracota = null;
            if(rs.next()) {
                newTeracota = new Teracota(teraID, rs.getString("name"), Teracota.PlantTypes.values()[rs.getInt("plant")], rs.getDate("planted_at"));
            }
            return newTeracota;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Teracota[] getAllTeracotas()
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM terracotta");
            ResultSet rs = ps.executeQuery();
            List<Teracota> teracotas = new ArrayList<>();
            while (rs.next()) {
                teracotas.add(new Teracota(rs.getInt(1), rs.getString("name"), Teracota.PlantTypes.values()[rs.getInt("plant")], rs.getDate("planted_at")));
            }
            Teracota[] retTeracotas = new Teracota[teracotas.size()];
            for(int i = 0; i < teracotas.size(); i++) retTeracotas[i] = teracotas.get(i);
            return retTeracotas;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int getTeracotaOwner(int teraID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM terracotta WHERE terracotta_id = ?");
            ps.setString(1, String.valueOf(teraID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt("owner");
            return -1;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean deleteTeracota(int teraID)
    {
        try(Connection connection = getConection()) {

            PreparedStatement ps = connection.prepareStatement("DELETE FROM terracotta WHERE terracotta_id = ? ;");
            ps.setInt(1, teraID);
            if (ps.executeUpdate() > 0) return true;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public List<Cron> getCronsToTeracota(int teraID)
    {
        try(Connection connection = getConection()) {
            List<Cron> cronList = new ArrayList<>();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM cron WHERE tracota = ?");
            ps.setInt(1,teraID);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int cronID = rs.getInt("cron_id");
                int scheduleID = rs.getInt("Schedl");
                int start= rs.getInt("startTime");
                int endTime= rs.getInt("endTime");

                ps = connection.prepareStatement("SELECT * FROM schedule WHERE schedule_id = ?");
                ps.setInt(1,scheduleID);
                ResultSet rs2 = ps.executeQuery();
                ResultSetMetaData mt = rs.getMetaData();

                rs2.next();
                float temperature = rs2.getFloat("temp");
                boolean light=rs2.getBoolean("light");
                int humidity =rs2.getInt("humidity");

                Schedule sch= new Schedule(scheduleID,temperature,light,humidity);
                cronList.add(new Cron(cronID,sch,start,endTime));
            }
            return cronList;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Cron getActiveCronfromTeracota(int teraID,int hour)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM cron LEFT JOIN terracotta on tracota=terracotta_id WHERE terracotta_id = ? AND startTime<=? AND endTime>=? ");
            ps.setInt(1, teraID);
            ps.setInt(2, hour);
            ps.setInt(3, hour);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
            {
                int cronID = rs.getInt("cron_id");
                int scheduleID = rs.getInt("Schedl");
                Schedule schedule = getSchedule(scheduleID);
                int start= rs.getInt("startTime");
                int endTime= rs.getInt("endTime");
                return new Cron(cronID,schedule,start,endTime);
            }
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void addNewCron(int teraID)
    {
        try(Connection connection = getConection())
        {
            PreparedStatement ps = connection.prepareStatement("insert into schedule (temp,light,humidity) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
            ps.setFloat(1,30);
            ps.setFloat(2, 1);
            ps.setInt(3, 1);
            int ef= ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int identity =rs.getInt("insert_id");

            ps = connection.prepareStatement("insert into cron (tracota,Schedl,startTime,endTime) VALUES (?,?,?,?);");
            ps.setInt(1, teraID);
            ps.setInt(2, identity);
            ps.setInt(3, 0);
            ps.setInt(4,23);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Cron[] getActiveCrons(int actualHour)
    {
        try(Connection connection = getConection()) {
            List<Cron> cronList = new ArrayList<Cron>();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM cron WHERE startTime<=? AND endTime>? ");
            ps.setInt(1,actualHour);
            ps.setInt(2,actualHour);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int schedlID = rs.getInt(3);
              cronList.add(new Cron(rs.getInt(1), rs.getInt(2), getSchedule(schedlID), rs.getInt(4), rs.getInt(5)));
            }
            if(cronList.size() == 0) return null;
            else {
             Cron[] crons = new Cron[cronList.size()];
             for (int i = 0; i < cronList.size(); i++) crons[i] = cronList.get(i);
             return crons;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void updateCron(Cron cron)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE cron SET startTime = ?, endTime= ? WHERE cron_id = ?");
            ps.setInt(1,cron.getStartTime());
            ps.setInt(2, cron.getEndTime());
            ps.setInt(3, cron.getId());
            int result = ps.executeUpdate();

            ps = connection.prepareStatement("UPDATE schedule SET temp = ?,humidity=? , light =? WHERE schedule_id = ?");
            ps.setFloat(1,cron.getSchedule().getTemperature());
            ps.setInt(2,cron.getSchedule().getHumidity());
            ps.setBoolean(3, cron.getSchedule().getLight());
            ps.setInt(4, cron.getSchedule().getId());
            result = ps.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteCron(int cronID)
    {
        try(Connection connection = getConection())
        {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM cron WHERE cron_id = ?;");
            ps.setInt(1, cronID);
            ps.execute();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public Schedule getSchedule(int scheduleID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM schedule WHERE schedule_id = ?");
            ps.setString(1, String.valueOf(scheduleID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return new Schedule(scheduleID,rs.getFloat(2),rs.getBoolean(3),rs.getInt(4));
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Plant getPlant(int plantID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM planttype WHERE plantType_id = ?");
            ps.setString(1, String.valueOf(plantID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return new Plant(Teracota.PlantTypes.values()[plantID],Integer.valueOf(rs.getInt("growtimeindays")));
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public PLC getPLC(int teracotaID)
    {
        try(Connection connection = getConection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM plc LEFT JOIN terracotta on PLC_id=PLC WHERE terracotta_id = ?");
            ps.setString(1, String.valueOf(teracotaID));
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return new PLC(rs.getString(3),rs.getInt(2));
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
