package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserServiceImpl userService;
    private final Integer USERS = 1;

    @Override
    public List<ItemDto> findAllByOwner(Integer ownerId) {
        log.info("Поиск вещей пользователя с Id: " + ownerId);
        userService.findById(ownerId);
        List<Item> items = itemDao.findAllByOwner(ownerId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(Integer userId, Integer itemId) {
        userService.findById(userId);
        List<Item> items = itemDao.findAllByOwner(userId);
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                return ItemMapper.toItemDto(item);
            }
        }
        throw new NotFoundException("Предмета " + itemId + " несуществует у пользователя " + userId);
    }

    @Override
    public ItemDto create(ItemDto itemDto, Integer ownerId) {
        log.info("Создание предмета:  " + itemDto + " владелец: " + ownerId);
        UserDto user = userService.findById(ownerId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user.getId());
        itemDao.add(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Integer itemId, ItemDto itemDto, Integer ownerId) {
        log.info("Обновление предмета Id:" + itemId + " предмет: " + itemDto + " владелец: " + ownerId);
        UserDto user = userService.findById(ownerId);

        Item existing = itemDao.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));

        if (!existing.getOwner().equals(user.getId())) {
            List<Item> sameItems = itemDao.findAllByOwner(ownerId);
            if (sameItems.size() > USERS) {
                existing = findItemByOwner(sameItems, ownerId);
            }
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        itemDao.update(existing);
        return ItemMapper.toItemDto(existing);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск текста: " + text);
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> items = itemDao.search(text);
        return items.stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private Item findItemByOwner(List<Item> items, Integer ownerId) {
        for (Item item : items) {
            if (ownerId.equals(item.getOwner())) {
                return item;
            }
        }
        throw new NotFoundException("Редактировать может только владелец");
    }
}