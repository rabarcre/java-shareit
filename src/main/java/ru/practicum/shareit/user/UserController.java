package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.markers.Create;
import ru.practicum.shareit.user.markers.Update;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto add(@Validated({Create.class}) @RequestBody UserDto userDto) {
        log.info("Запрос на добавление пользователя {}", userDto);
        return userService.add(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Integer userId) {
        log.info("Запрос на получение пользователя id = {}", userId);
        return userService.findById(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Запрос на получение списка всех пользователей");
        return userService.findAll();
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Integer userId, @Validated({Update.class}) @RequestBody UserDto userDto) {
        log.info("Запрос на обновление пользователя id = {}", userId);
        return userService.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Integer userId) {
        log.info("Delete - запрос на удаление пользователя id = {}", userId);
        userService.delete(userId);
    }
}
