package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto update(Integer id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                            return new NotFoundException("Пользователя с " + id + " не существует");
                        }
                );
        String name = userDto.getName();
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        String email = userDto.getEmail();
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto findById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с " + id + " не существует")
                );
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}