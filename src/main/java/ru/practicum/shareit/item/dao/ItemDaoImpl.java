package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ItemDaoImpl implements ItemDao {
    private final Map<Integer, List<Item>> items = new HashMap<>();
    private int currentId = 0;

    @Override
    public Item add(Item item) {
        List<Item> listItems = new ArrayList<>();
        if (items.containsKey(item.getOwner())) {
            listItems = items.get(item.getOwner());
        }
        listItems.add(item);
        items.put(item.getOwner(), listItems);
        return item;
    }

    @Override
    public Item update(Item item) {
        for (Map.Entry<Integer, List<Item>> entry : items.entrySet()) {
            List<Item> itemList = entry.getValue();
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getId().equals(item.getId())) {
                    itemList.set(i, item);
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public Optional<Item> findItemById(Integer itemId) {
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(i -> i.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public List<Item> findItemsByIdAcrossOwners(Integer itemId) {
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(i -> i.getId().equals(itemId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findAllByOwner(Integer ownerId) {
        return new ArrayList<>(items.get(ownerId));
    }

    @Override
    public List<Item> search(String text) {
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(Item::getAvailable)
                .filter(i -> i.getName().toLowerCase().contains(lowerText)
                             || i.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }
}