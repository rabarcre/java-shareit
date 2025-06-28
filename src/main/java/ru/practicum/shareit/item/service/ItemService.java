package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAllByOwner(Integer ownerId);

    ItemDto findById(Integer userId, Integer itemId);

    ItemDto create(ItemDto itemDto, Integer ownerId);

    ItemDto update(Integer itemId, ItemDto itemDto, Integer ownerId);

    List<ItemDto> search(String text);
}