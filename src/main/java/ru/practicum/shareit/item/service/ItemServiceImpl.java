package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemDtoOut add(Integer userId, ItemDto itemDto) {
        UserDto user = userService.findById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner((UserMapper.toUser(user)));
        return ItemMapper.toItemDtoOut(itemRepository.save(item));
    }


    @Override
    @Transactional
    public ItemDtoOut update(Integer userId, Integer itemId, ItemDto itemDto) {
        UserDto user = userService.findById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                            return new NotFoundException("Вещи с " + itemId + " не существует");
                        }
                );
        if (!UserMapper.toUser(user).equals(item.getOwner())) {
            throw new NotFoundException("Пользователь с id = " + userId +
                                        " не является собственником вещи id = " + itemId);
        }
        Boolean isAvailable = itemDto.getAvailable();
        if (isAvailable != null) {
            item.setAvailable(isAvailable);
        }
        String description = itemDto.getDescription();
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }
        String name = itemDto.getName();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }
        return ItemMapper.toItemDtoOut(item);
    }


    @Override
    @Transactional
    public ItemDtoOut findItemById(Integer userId, Integer itemId) {
        userService.findById(userId);
        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new NotFoundException("У пользователя с id = " + userId + " не " +
                                        "существует вещи с id = " + itemId);
        }
        Item item = itemGet.get();
        ItemDtoOut itemDtoOut = ItemMapper.toItemDtoOut(itemGet.get());
        itemDtoOut.setComments(getAllItemComments(itemId));
        if (!item.getOwner().getId().equals(userId)) {
            return itemDtoOut;
        }
        List<Booking> bookings = bookingRepository.findAllByItemAndStatusOrderByStartAsc(item, Status.APPROVED);
        List<BookingDtoOut> bookingDTOList = bookings
                .stream()
                .map(BookingMapper::toBookingOut)
                .collect(toList());

        itemDtoOut.setLastBooking(getLastBooking(bookingDTOList, LocalDateTime.now()));
        itemDtoOut.setNextBooking(getNextBooking(bookingDTOList, LocalDateTime.now()));
        return itemDtoOut;
    }


    @Override
    @Transactional
    public List<ItemDtoOut> findAll(Integer userId) {
        UserDto owner = userService.findById(userId);
        List<Item> itemList = itemRepository.findAllByOwnerId(userId);
        List<Integer> idList = itemList.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        Map<Integer, List<CommentDtoOut>> comments = commentRepository.findAllByItemIdIn(idList)
                .stream()
                .map(CommentMapper::toCommentDtoOut)
                .collect(groupingBy(CommentDtoOut::getItemId, toList()));

        Map<Integer, List<BookingDtoOut>> bookings = bookingRepository.findAllByItemInAndStatusOrderByStartAsc(itemList,
                        Status.APPROVED)
                .stream()
                .map(BookingMapper::toBookingOut)
                .collect(groupingBy(BookingDtoOut::getItemId, toList()));

        return itemList
                .stream()
                .map(item -> ItemMapper.toItemDtoOut(
                        item,
                        getLastBooking(bookings.get(item.getId()), LocalDateTime.now()),
                        comments.get(item.getId()),
                        getNextBooking(bookings.get(item.getId()), LocalDateTime.now())
                ))
                .collect(toList());
    }


    @Override
    @Transactional
    public List<ItemDtoOut> search(Integer userId, String text) {
        userService.findById(userId);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> itemList = itemRepository.search(text);
        return itemList.stream()
                .map(ItemMapper::toItemDtoOut)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDtoOut createComment(Integer userId, CommentDto commentDto, Integer itemId) {
        User user = UserMapper.toUser(userService.findById(userId));
        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {

            throw new NotFoundException("У пользователя с id = " + userId + " не " +
                                        "существует вещи с id = " + itemId);
        }
        Item item = itemById.get();

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new ValidationException("У пользователя с id   " + userId + " должно быть хотя бы одно бронирование предмета с id " + itemId);
        }

        return CommentMapper.toCommentDtoOut(commentRepository.save(CommentMapper.toComment(commentDto, item, user)));
    }

    public List<CommentDtoOut> getAllItemComments(Integer itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        return comments.stream()
                .map(CommentMapper::toCommentDtoOut)
                .collect(toList());
    }

    private BookingDtoOut getLastBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> !bookingDTO.getStart().isAfter(time))
                .reduce((booking1, booking2) -> booking1.getStart().isAfter(booking2.getStart()) ? booking1 : booking2)
                .orElse(null);
    }

    private BookingDtoOut getNextBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> bookingDTO.getStart().isAfter(time))
                .findFirst()
                .orElse(null);
    }
}