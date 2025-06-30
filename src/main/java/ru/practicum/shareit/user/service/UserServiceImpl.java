package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UniqueEmailException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Override
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        checkEmail(user);
        return UserMapper.toUserDto(userDao.add(user));
    }

    @Override
    public UserDto update(Integer id, UserDto userDto) {
        checkUser(id);
        User user = new User();
        UserDto savedUser = UserMapper.toUserDto(userDao.findUserById(id));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        } else {
            user.setName(savedUser.getName());
        }

        if (userDto.getEmail() != null) {
            checkEmail(UserMapper.toUser(userDto));
            user.setEmail(userDto.getEmail());
        } else {
            user.setEmail(savedUser.getEmail());
        }
        user.setId(id);
        userDao.update(id, user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto findById(Integer id) {
        checkUser(id);
        return UserMapper.toUserDto(userDao.findUserById(id));
    }

    @Override
    public void delete(Integer id) {
        checkUser(id);
        userDao.delete(id);
    }

    @Override
    public List<UserDto> findAll() {
        return userDao.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void checkEmail(User user) {
        boolean isUnique = userDao.findAll().stream()
                .anyMatch(thisUser -> thisUser.getEmail().equals(user.getEmail()));
        if (isUnique) {
            throw new UniqueEmailException("Электронная почта: " + user.getEmail() + " уже зарегестрированна");
        }
    }

    private void checkUser(Integer id) {
        if (userDao.findAll().stream()
                .noneMatch(user -> user.getId().equals(id))) {
            throw new NotFoundException("Пользователя с Id: " + id + " не существует");
        }
    }
}
