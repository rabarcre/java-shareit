package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    @Autowired
    private ItemService itemService;

    @PostMapping
    public ItemDtoOut add(@RequestHeader(USER_HEADER) Integer userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST Запрос на добавление пользователем с id = {} предмета {}", userId, itemDto.toString());
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoOut update(@RequestHeader(USER_HEADER) Integer userId,
                             @RequestBody ItemDto itemDto,
                             @PathVariable Integer itemId) {
        log.info("PATCH Запрос на обновление предмета с id = {} пользователем с id = {} ", itemId, userId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDtoOut findById(@RequestHeader(USER_HEADER) Integer userId,
                               @PathVariable("itemId")
                               Integer itemId) {
        log.info("GET Запрос на получение предмета с id = {} пользователем с id = {} ", itemId, userId);
        return itemService.findItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoOut> findAll(@RequestHeader(USER_HEADER) Integer userId,
                                    @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                    @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size
    ) {
        log.info("GET Запрос на получение предметов пользователя с id = {}", userId);
        return itemService.findAll(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDtoOut> searchItems(@RequestHeader(USER_HEADER) Integer userId,
                                        @RequestParam(name = "text") String text,
                                        @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("GET Запрос на поиск предметов c текстом = {}", text);
        return itemService.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoOut createComment(@RequestHeader(USER_HEADER) Integer userId,
                                       @Validated @RequestBody CommentDto commentDto,
                                       @PathVariable Integer itemId) {
        log.info("POST Запрос на создание комментария id = {}", itemId);
        return itemService.createComment(userId, commentDto, itemId);
    }
}