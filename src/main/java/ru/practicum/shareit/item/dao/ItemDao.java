package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {
    Item add(Item item);

    Item update(Item item);

    Optional<Item> findItemById(Integer itemId);

    List<Item> findAllByOwner(Integer ownerId);

    List<Item> search(String text);

    List<Item> findItemsByIdAcrossOwners(Integer itemId);
}
