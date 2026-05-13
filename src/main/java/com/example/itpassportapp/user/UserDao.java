package com.example.itpassportapp.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    //ログイン用
    public User findByEmail(String email) {
        String sql = "SELECT id, email, password_hash, name,role FROM users WHERE email = ?";

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return null;

            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setName(rs.getString("name"));
            u.setRole(rs.getString("role"));
            return u;
        }, email);
    }

    //登録用
    public void insert(String email, String passwordHash,String name){
        String sql = "INSERT INTO users(email, password_hash, name) VALUES(?,?,?)";
        jdbcTemplate.update(sql, email, passwordHash, name);
    }
    //管理者登録用
    public void insertAdmin(String email,
                            String passwordHash,
                            String name){
        String sql= """
                INSERT INTO users(
                email,password_hash,name,role)
                VALUES(?,?,?,'ADMIN')
                """;
        jdbcTemplate.update(sql,email,passwordHash,name);

    }

    //名前更新
    public int updateNameByEmail(String email, String name){
        String sql ="UPDATE users SET name = ? WHERE email =?";
        return jdbcTemplate.update(sql, name, email);
    }

    //パスワード更新
    public int updatePasswordByEmail(String email, String passwordHash){
        String sql ="UPDATE users SET password_hash =? WHERE email= ?";
        return jdbcTemplate.update(sql, passwordHash, email);
    }

    //アカウント削除
    public int deleteByEmail(String email){
        String sql="DELETE FROM users WHERE email=?";
        return jdbcTemplate.update(sql, email);
    }
    //メールアドレス更新
    public int updateEmail(String currentEmail, String newEmail){
        String sql = "UPDATE users SET email = ? WHERE email=?";
        return jdbcTemplate.update(sql, newEmail, currentEmail);

    }

}
