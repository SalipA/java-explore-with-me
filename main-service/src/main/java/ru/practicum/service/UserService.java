package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.output.EventInitiatorDto;
import ru.practicum.dto.output.SubscriptionDto;
import ru.practicum.entity.Subscription;
import ru.practicum.exception.IllegalActionException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.SubscriptionMapper;
import ru.practicum.repository.SubscriptionRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.dto.reversible.UserDto;
import ru.practicum.entity.User;
import ru.practicum.mapper.UserMapper;
import ru.practicum.state.SubscriptionState;
import ru.practicum.state.UserProfileState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public UserService(UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public UserDto registerUser(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        newUser.setProfile(UserProfileState.PUBLIC);
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

    public UserDto changeUserProfile(Long userId, UserProfileState profile) {
        User user = getUserIfExists(userId);
        if (user.getProfile().equals(profile)) {
            String message =
                "Unavailable action: can not change user profile. Reason: User id=" + userId + " profile = " + profile +
                    " is already set";
            log.error(message);
            throw new IllegalActionException(message);
        } else {
            if (profile.equals(UserProfileState.PUBLIC)) {
                subscriptionRepository.confirmedSubscriptions(SubscriptionState.CONFIRMED, SubscriptionState.PENDING,
                    user);
            }
            user.setProfile(profile);
            User saved = userRepository.save(user);
            log.info("User id = {} profile has been updated", userId);
            return UserMapper.toUserDto(saved);
        }
    }

    public List<EventInitiatorDto> getInitiators(String sort, UserProfileState profile, Integer from, Integer size) {
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<EventInitiatorDto> initiatorsSorted;
        switch (sort) {
            case "MOST_INITIATIVE":
                initiatorsSorted = userRepository.findMostInitiative(profile, pageRequest).getContent();
                break;
            case "MOST_POPULAR":
                initiatorsSorted = userRepository.findMostPopular(profile, pageRequest).getContent();
                break;
            default:
                String message = "Unavailable action: unable to get events initiators. Reason: sort = " + sort + " " +
                    "is not supported";
                throw new IllegalActionException(message);
        }
        log.info("Get request for Initiators: sort = {}, profile = {}, processed successfully", sort, profile);
        return initiatorsSorted;
    }


    public SubscriptionDto addSubscription(Long subscriberId, Long subscribesToId) {
        checkSubscriberAndInitiatorAreNotSame(subscriberId, subscribesToId);
        User subscribesTo = getUserIfExists(subscribesToId);
        User subscriber = getUserIfExists(subscriberId);
        Subscription newSubscription = new Subscription();
        newSubscription.setSubscribedTo(subscribesTo);
        newSubscription.setSubscriber(subscriber);
        switch (subscribesTo.getProfile()) {
            case PUBLIC:
                newSubscription.setState(SubscriptionState.CONFIRMED);
                break;
            case PRIVATE:
                newSubscription.setState(SubscriptionState.PENDING);
                break;
        }
        Subscription saved = subscriptionRepository.save(newSubscription);
        log.info("Subscription from User id = {} to User id = {} has been saved, id = {}", subscriberId,
            subscribesToId, saved.getId());
        return SubscriptionMapper.toSubscriptionDto(saved);
    }

    public void deleteSubscription(Long subscriberId, Long subscribedToId) {
        Subscription subscriptionFromDb = getSubscriptionIfExists(subscriberId, subscribedToId);
        subscriptionRepository.deleteById(subscriptionFromDb.getId());
        log.info("Subscription from User id = {} to User id = {} has been deleted by subscriber", subscriberId, subscribedToId);
    }

    public List<SubscriptionDto> getUsersSubscriptions(Long userId, String direction, SubscriptionState state,
                                                       Integer from,
                                                       Integer size) {
        User user = getUserIfExists(userId);
        Pageable pageRequest = PageRequestSpecifier.getPageRequestWithoutSort(from, size);
        List<Subscription> subscriptions;
        switch (direction) {
            case "TO_ME":
                subscriptions = subscriptionRepository.findByInitiatorAndState(user, state,
                    pageRequest).getContent();
                log.info("Get request by initiator for subscriptions of those, who are subscribed to initiator id = {}, state" +
                    " = {} processed successfully", userId, state);
                break;
            case "FROM_ME":
                subscriptions = subscriptionRepository.findBySubscriberAndState(user, state,
                    pageRequest).getContent();
                log.info("Get request for subscriptions by subscriber id = {}, state = {} , processed successfully",
                    userId, state);
                break;
            default:
                String message =
                    "Unavailable action: unable to get subscriptions. Reason: direction of request = " + direction +
                        "is not supported, must be only 'TO_ME' or 'FROM_ME'";
                log.error(message);
                throw new IllegalActionException(message);
        }

        return SubscriptionMapper.toSubscriptionDtoList(subscriptions);
    }

    public SubscriptionDto changeSubscriptionStatus(Long initiatorId, Long subscriberId, SubscriptionState state) {
        User initiator = getUserIfExists(initiatorId);
        boolean isPrivate = initiator.getProfile().equals(UserProfileState.PRIVATE);
        if (!isPrivate) {
            String message =
                "Unavailable action: unable to change subscription status. Reason: User id = " + initiatorId + " " +
                    "profile is PUBLIC. Subscription status management is not required.";
            log.error(message);
            throw new IllegalActionException(message);
        } else {
            Subscription subscription = getSubscriptionIfExists(subscriberId, initiatorId);
            boolean isPending = subscription.getState().equals(SubscriptionState.PENDING);
            if (isPending) {
                switch (state) {
                    case CONFIRMED:
                    case REJECTED:
                        subscription.setState(state);
                        break;
                    default:
                        String message = "Unavailable action: unable to change subscription status. Reason: Subscriptions " +
                            "status is" + subscription.getState() + ". Can not be changed to = " + state;
                        log.error(message);
                        throw new IllegalActionException(message);
                }
                Subscription saved = subscriptionRepository.save(subscription);
                log.info("Subscriptions status from User id = {} to User id = {} has been change to = {} by initiator",
                    subscriberId, initiatorId, state);
                return SubscriptionMapper.toSubscriptionDto(saved);
            } else {
                String message = "Unavailable action: unable to change subscription status. Reason: Subscriptions status " +
                    "has been already changed to = " + subscription.getState();
                log.error(message);
                throw new IllegalActionException(message);
            }
        }
    }

    public Subscription getSubscriptionIfExists(Long subscriberId, Long initiatorId) {
        User subscriber = getUserIfExists(subscriberId);
        User initiator = getUserIfExists(initiatorId);
        Optional<Subscription> subscription = subscriptionRepository.findBySubscriberAndSubscribedTo(subscriber,
            initiator);
        if (subscription.isPresent()) {
            return subscription.get();
        } else {
            String message = "User with id = " + subscriberId + " is not subscribed to User id = " + initiator;
            log.error(message);
            throw new NotFoundException(message);
        }
    }

    public void checkSubscriberAndInitiatorAreNotSame(Long subscriberId, Long initiatorId) {
        if (Objects.equals(subscriberId, initiatorId)) {
            String message =
                "Unavailable action: unable to subscribe. Reason: subscriberId and initiatorId are the same";
            log.error(message);
            throw new IllegalActionException(message);
        }
    }

}
