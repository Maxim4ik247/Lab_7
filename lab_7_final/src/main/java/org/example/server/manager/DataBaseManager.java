package org.example.server.manager;


import org.example.data.*;
import org.example.server.handlers.HashHandler;

import java.sql.*;
import java.util.LinkedList;

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

    public  void connectToDataBase() {
        try {
            connection = DriverManager.getConnection(URL, username, password); // connection не должен сущ нет многопоточки
            if (checkRequiredTablesExist()) {
                System.out.println("Подключено успешно");
            }
            else {
                System.out.println("Ошибка при проверке таблиц: ");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при подключении к базам данных");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    public  boolean checkRequiredTablesExist() {
        String[] requiredTables = {"my_users", "workers"};
        try {
            DatabaseMetaData meta = connection.getMetaData();
            for (String tableName: requiredTables) {
                try (ResultSet tables = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (!tables.next()) {
                        System.out.println("Таблица " + tableName + " не существует.");
                        return false;
                    }
                }
            }
            System.out.println("Все необходимые таблицы существуют.");
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке таблиц: " + e.getMessage());
            return false;
        }
    }

    public  boolean checkUser(String login, String password) {
        try {
            PreparedStatement getStatement = connection.prepareStatement(GET_USER_BY_USERNAME);
            getStatement.setString(1, login);
            ResultSet rs = getStatement.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(HashHandler.encryptString(password));
            }
            return false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public  boolean insertUser(String username, String password) {
        try (PreparedStatement addStatement = connection.prepareStatement(INSERT_USER)) {
            addStatement.setString(1, username);
            addStatement.setString(2, HashHandler.encryptString(password));
            addStatement.executeUpdate();
            addStatement.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Не может зарегестрировать пользователя: " + e.getMessage());
            return false;
        }
    }

    public  int getUserId(String login) {
        try {
            PreparedStatement getStatement = connection.prepareStatement(GET_USER_BY_USERNAME);
            getStatement.setString(1, login);
            ResultSet rs = getStatement.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getInt("id"));
                return rs.getInt("id");
            }
            return -1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public  boolean addWorker(String login, Worker worker) {
        try {
            PreparedStatement addStatement = connection.prepareStatement(INSERT_WORKER);
            addStatement.setInt(1, getUserId(login));
            addStatement.setString(2, worker.getName());
            addStatement.setFloat(3, worker.getCoordinates().getX());
            addStatement.setFloat(4, worker.getCoordinates().getY());
            addStatement.setObject(5, worker.getCreationDate());
            addStatement.setFloat(6, worker.getSalary());
            addStatement.setString(7, String.valueOf(worker.getPosition()));
            addStatement.setString(8, String.valueOf(worker.getStatus()));
            addStatement.setInt(9, worker.getPerson().getHeight());
            addStatement.setString(10, String.valueOf(worker.getPerson().getEyeColor()));
            addStatement.setString(11, String.valueOf(worker.getPerson().getHairColor()));
            addStatement.setString(12, String.valueOf(worker.getPerson().getNationality()));
            addStatement.setFloat(13, worker.getPerson().getLocation().getX());
            addStatement.setFloat(14, worker.getPerson().getLocation().getY());
            addStatement.setLong(15, worker.getPerson().getLocation().getZ());
            addStatement.setString(16, worker.getPerson().getLocation().getName());
            addStatement.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public  int getOwnerId(String key) {
        try {
            PreparedStatement getStatement = connection.prepareStatement(GET_OWNER_BY_KEY);
            getStatement.setInt(1, Integer.parseInt(key));
            ResultSet rs = getStatement.executeQuery();
            while (rs.next()) {
                return rs.getInt("user_id");
            }
            return -1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -2;
        }
    }

    public  boolean updateWorker(String login, Worker worker, Integer id) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(UPDATE_WORKER);
            updateStatement.setInt(1, getUserId(login));
            updateStatement.setString(2, worker.getName());
            updateStatement.setFloat(3, worker.getCoordinates().getX());
            updateStatement.setFloat(4, worker.getCoordinates().getY());
            updateStatement.setObject(5, worker.getCreationDate());
            updateStatement.setFloat(6, worker.getSalary());
            updateStatement.setString(7, String.valueOf(worker.getPosition()));
            updateStatement.setString(8, String.valueOf(worker.getStatus()));
            updateStatement.setInt(9, worker.getPerson().getHeight());
            updateStatement.setString(10, String.valueOf(worker.getPerson().getEyeColor()));
            updateStatement.setString(11, String.valueOf(worker.getPerson().getHairColor()));
            updateStatement.setString(12, String.valueOf(worker.getPerson().getNationality()));
            updateStatement.setFloat(13, worker.getPerson().getLocation().getX());
            updateStatement.setFloat(14, worker.getPerson().getLocation().getY());
            updateStatement.setLong(15, worker.getPerson().getLocation().getZ());
            updateStatement.setString(16, worker.getPerson().getLocation().getName());
            updateStatement.setInt(17, id);
            updateStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось обновить работника: " + e.getMessage());
            return false;
        }
    }

    public  LinkedList<Worker> getWorkers(String login) {
        LinkedList<Worker> workers = new LinkedList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(GET_WORKERS);
             statement.setInt(1, getUserId(login));
             ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Coordinates coordinates = new Coordinates(
                        resultSet.getFloat("coord_x"),
                        resultSet.getFloat("coord_y")
                );


                Person person = new Person(resultSet.getInt("person_height"), Color.valueOf(resultSet.getString("eye_color")), Color.valueOf(resultSet.getString("hair_color")), Country.valueOf(resultSet.getString("nationality")), new Location(resultSet.getFloat("location_x"), resultSet.getFloat("location_y"), resultSet.getLong("location_z"), resultSet.getString("location_name")));

                Worker worker = new Worker(resultSet.getInt("id"), resultSet.getString("name"), coordinates, resultSet.getTimestamp("creation_date").toLocalDateTime(), resultSet.getFloat("salary"), Position.valueOf(resultSet.getString("position")), Status.valueOf(resultSet.getString("status")), person);


                worker.setPerson(person);

                workers.add(worker);
            }

        } catch (SQLException e) {
            System.out.println("Не удалось получить работников: " + e.getMessage());
        }

        return workers;
    }

    public  boolean removeById(int workerId, String login) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_WORKER_BY_ID)) {
            statement.setInt(1, workerId);
            statement.setInt(2, getUserId(login));

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Ошибка: worker с id = " + workerId + " не обнаружен или не принадлежит пользователю " + login);
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении worker: " + e.getMessage());
            return false;
        }
    }

    public  boolean removeFirst(String login) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_FIRST_WORKER)) {
            int userId = getUserId(login);
            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Нет worker доступных для удаления для пользователем: " + login);
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении worker: " + e.getMessage());
            return false;
        }
    }

    public  boolean clear(String login){
        try (PreparedStatement statement = connection.prepareStatement(CLEAR_WORKERS)) {
            int userId = getUserId(login);
            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("Нет worker доступных для удаления для пользователем: " + login);
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении worker: " + e.getMessage());
            return false;
        }
    }
    
}
