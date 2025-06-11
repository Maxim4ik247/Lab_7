package org.example.server.manager;


import org.example.data.*;
import org.example.server.handlers.HashHandler;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DataBaseManager {

    private  String URL = "jdbc:postgresql://pg:5432/studs";
    private  String username = "s413020";
    private  String password = "zUZ8s63lcvNq8AVk";


    private  Connection connection;
    private  final String GET_WORKERS = "SELECT * FROM workers WHERE user_id = ?";
    private  final String GET_USER_BY_USERNAME = "SELECT * FROM my_users WHERE login = ?";
    private  final String INSERT_WORKER = "INSERT INTO workers (user_id, name, coord_x, coord_y, creation_date, salary, position, status, person_height, eye_color, hair_color, nationality, location_x, location_y, location_z, location_name) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private  final String UPDATE_WORKER = "UPDATE workers SET user_id = ?, name = ?, coord_x = ?, coord_y = ?, creation_date = ?, salary = ?, position = ?, status = ?, person_height = ?, eye_color = ?, hair_color = ?, nationality = ?, location_x = ?, location_y = ?, location_z = ?, location_name = ? WHERE id = ?";
    private  final String INSERT_USER = "INSERT INTO my_users (login, password) VALUES (?, ?)";
    private  final String GET_OWNER_BY_KEY = "SELECT user_id FROM workers WHERE worker_id = ?";
    private  final String CLEAR_WORKERS = "DELETE FROM workers WHERE user_id=?";
    private  final String DELETE_WORKER_BY_ID = "DELETE FROM workers WHERE id = ? AND user_id = ?";
    private  final String DELETE_FIRST_WORKER = " DELETE FROM workers WHERE id = (SELECT id FROM workers WHERE user_id = ? ORDER BY id ASC LIMIT 1)";

    public DataBaseManager() {
    }

    public void checkTables() {
        List<String> required = Arrays.asList("workers", "my_users");
        try (Connection conn = DriverManager.getConnection(URL, username, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String table : required) {
                try (ResultSet rs = meta.getTables(null, null, table.toLowerCase(), new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        throw new RuntimeException("В базе не найдена таблица: " + table);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при проверке схемы БД: " + e.getMessage(), e);
        }
    }


    public boolean checkUser(String login, String password) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_USERNAME)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password")
                            .equals(HashHandler.encryptString(password));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка в checkUser: " + e.getMessage());
        }
        return false;
    }

    public boolean insertUser(String login, String password) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(INSERT_USER)) {
            ps.setString(1, login);
            ps.setString(2, HashHandler.encryptString(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Не может зарегистрировать пользователя: " + e.getMessage());
            return false;
        }
    }

    public int getUserId(String login) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(GET_USER_BY_USERNAME)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка в getUserId: " + e.getMessage());
        }
        return -1;
    }

    public boolean addWorker(String login, Worker worker) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(INSERT_WORKER)) {
            ps.setInt(1, getUserId(login));
            ps.setString(2, worker.getName());
            ps.setFloat(3, worker.getCoordinates().getX());
            ps.setFloat(4, worker.getCoordinates().getY());
            ps.setObject(5, worker.getCreationDate());
            ps.setFloat(6, worker.getSalary());
            ps.setString(7, String.valueOf(worker.getPosition()));
            ps.setString(8, String.valueOf(worker.getStatus()));
            ps.setInt(9, worker.getPerson().getHeight());
            ps.setString(10, String.valueOf(worker.getPerson().getEyeColor()));
            ps.setString(11, String.valueOf(worker.getPerson().getHairColor()));
            ps.setString(12, String.valueOf(worker.getPerson().getNationality()));
            ps.setFloat(13, worker.getPerson().getLocation().getX());
            ps.setFloat(14, worker.getPerson().getLocation().getY());
            ps.setLong(15, worker.getPerson().getLocation().getZ());
            ps.setString(16, worker.getPerson().getLocation().getName());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка в addWorker: " + e.getMessage());
            return false;
        }
    }

    public int getOwnerId(String key) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(GET_OWNER_BY_KEY)) {
            ps.setInt(1, Integer.parseInt(key));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка в getOwnerId: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateWorker(String login, Worker worker, Integer id) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(UPDATE_WORKER)) {
            ps.setInt(1, getUserId(login));
            ps.setString(2, worker.getName());
            ps.setFloat(3, worker.getCoordinates().getX());
            ps.setFloat(4, worker.getCoordinates().getY());
            ps.setObject(5, worker.getCreationDate());
            ps.setFloat(6, worker.getSalary());
            ps.setString(7, String.valueOf(worker.getPosition()));
            ps.setString(8, String.valueOf(worker.getStatus()));
            ps.setInt(9, worker.getPerson().getHeight());
            ps.setString(10, String.valueOf(worker.getPerson().getEyeColor()));
            ps.setString(11, String.valueOf(worker.getPerson().getHairColor()));
            ps.setString(12, String.valueOf(worker.getPerson().getNationality()));
            ps.setFloat(13, worker.getPerson().getLocation().getX());
            ps.setFloat(14, worker.getPerson().getLocation().getY());
            ps.setLong(15, worker.getPerson().getLocation().getZ());
            ps.setString(16, worker.getPerson().getLocation().getName());
            ps.setInt(17, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось обновить работника: " + e.getMessage());
            return false;
        }
    }

    public LinkedList<Worker> getWorkers(String login) {
        LinkedList<Worker> workers = new LinkedList<>();
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(GET_WORKERS)) {
            ps.setInt(1, getUserId(login));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Coordinates coords = new Coordinates(
                            rs.getFloat("coord_x"),
                            rs.getFloat("coord_y")
                    );
                    Person person = new Person(
                            rs.getInt("person_height"),
                            Color.valueOf(rs.getString("eye_color")),
                            Color.valueOf(rs.getString("hair_color")),
                            Country.valueOf(rs.getString("nationality")),
                            new Location(
                                    rs.getFloat("location_x"),
                                    rs.getFloat("location_y"),
                                    rs.getLong("location_z"),
                                    rs.getString("location_name")
                            )
                    );
                    Worker w = new Worker(
                            rs.getInt("id"),
                            rs.getString("name"),
                            coords,
                            rs.getTimestamp("creation_date").toLocalDateTime(),
                            rs.getFloat("salary"),
                            Position.valueOf(rs.getString("position")),
                            Status.valueOf(rs.getString("status")),
                            person
                    );
                    workers.add(w);
                }
            }
        } catch (SQLException e) {
            System.out.println("Не удалось получить работников: " + e.getMessage());
        }
        return workers;
    }

    public boolean removeById(int workerId, String login) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(DELETE_WORKER_BY_ID)) {
            ps.setInt(1, workerId);
            ps.setInt(2, getUserId(login));
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("Ошибка: worker с id = " + workerId +
                        " не найден или не принадлежит пользователю " + login);
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении worker: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFirst(String login) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(DELETE_FIRST_WORKER)) {
            ps.setInt(1, getUserId(login));
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("Нет worker для удаления пользователем: " + login);
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении первого worker: " + e.getMessage());
            return false;
        }
    }

    public boolean clear(String login) {
        try (Connection conn = DriverManager.getConnection(URL, username, password);
             PreparedStatement ps = conn.prepareStatement(CLEAR_WORKERS)) {
            ps.setInt(1, getUserId(login));
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("Сборка пуста для пользователя: " + login);
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при очистке workers: " + e.getMessage());
            return false;
        }
    }
}
