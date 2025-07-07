package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserDao {
    User add(User user);

    User update(Integer id, User user);

    User findUserById(Integer id);

    void delete(Integer id);

    List<User> findAll();
}
