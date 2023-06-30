package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.UserRepository;
import ru.practicum.dto.reversible.UserDto;
import ru.practicum.entity.User;
import ru.practicum.mapper.UserMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto registerUser(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        User saved = userRepository.save(newUser);
        log.info("User value = {} has been saved, id = {}", userDto, saved.getId());
        return UserMapper.toUserDto(saved);
    }

    public void deleteUser(Long userId) {
        checkUserExists(userId);
        userRepository.deleteById(userId);
        log.info("User with id={} has been deleted", userId);
    }

    public List<UserDto> getUsers(Long[] ids, Integer from, Integer size) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<User> users;
        if (ids == null) {
            users = userRepository.findAll(pageRequest).getContent();
            log.info("Get request for Users list by ids = null processed successfully");
        } else {
            users = userRepository.getUsersByIdIn(Arrays.asList(ids), pageRequest).getContent();
            log.info("Get request for Users list by ids = {} processed successfully", Arrays.toString(ids));
        }
        return UserMapper.toUserDtoList(users);

    }

    public void checkUserExists(Long userId) {
        boolean exist = userRepository.existsById(userId);
        if (!exist) {
            String message = "User with id=" + userId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    public User getUserIfExists(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            String message = "User with id=" + userId + " was not found";
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    public List<User> getUsersById(Long[] ids) {
        if (ids == null) {
            log.info("Get request for Users list by ids = null processed successfully");
            return List.of();
        } else {
            log.info("Get request for Users list by ids = {} processed successfully", Arrays.toString(ids));
            return userRepository.getUsersByIdIn(Arrays.asList(ids));
        }
    }

}
