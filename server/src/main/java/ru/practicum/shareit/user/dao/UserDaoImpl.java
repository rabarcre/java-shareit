package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UniqueEmailException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UserDaoImpl implements UserDao {
    private final Map<Integer, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Integer currentId = 1;

    @Override
    public User add(User user) {
        checkEmail(user.getEmail());
        user.setId(currentId++);
        emails.add(user.getEmail());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(Integer id, User user) {
        checkUser(id);
        String oldEmail = users.get(id).getEmail();
        if (!user.getEmail().equals(oldEmail)) {
            emails.remove(oldEmail);
            emails.add(user.getEmail());
        }
        users.put(id, user);
        return users.get(id);
    }

    @Override
    public User findUserById(Integer id) {
        checkUser(id);
        return users.get(id);
    }

    @Override
    public void delete(Integer id) {
        checkUser(id);
        emails.remove(users.get(id).getEmail());
        users.remove(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    private void checkEmail(String email) {
        if (emails.contains(email)) {
            throw new UniqueEmailException("Электронная почта: " + email + " уже зарегестрированна");
        }
    }

    private void checkUser(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с Id: " + userId + " не существует");
        }
    }
}
