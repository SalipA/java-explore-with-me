import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ExploreWithMeMainService;
import ru.practicum.controller.PrivateController;
import ru.practicum.dto.output.SubscriptionDto;
import ru.practicum.entity.Subscription;
import ru.practicum.entity.User;
import ru.practicum.mapper.SubscriptionMapper;
import ru.practicum.service.*;
import ru.practicum.state.SubscriptionState;
import ru.practicum.state.UserProfileState;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {ExploreWithMeMainService.class})
@WebMvcTest(controllers = PrivateController.class)
@ExtendWith(MockitoExtension.class)
public class PrivateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventService eventService;
    @MockBean
    private RequestService requestService;
    @MockBean
    private UserService userService;

    @SneakyThrows
    @Test
    public void shouldGetSubscriptionsStandardCase() {
        Subscription subscription = new Subscription();
        subscription.setState(SubscriptionState.CONFIRMED);
        subscription.setId(1L);
        subscription.setSubscriber(new User(1L, "test1", "test@mail.ru", UserProfileState.PUBLIC));
        subscription.setSubscribedTo(new User(2L, "test2", "test2@mail.ru", UserProfileState.PUBLIC));
        SubscriptionDto subscriptionDto = SubscriptionMapper.toSubscriptionDto(subscription);
        when(userService.getUsersSubscriptions(any(), any(), any(), any(), any())).thenReturn(List.of(subscriptionDto));
        mockMvc.perform(get("/users/{userId}/subscriptions", 1L)
                .param("direction", "TO_ME")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].from.id").value(1L));
    }
}