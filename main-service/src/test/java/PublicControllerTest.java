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
import ru.practicum.controller.PublicController;
import ru.practicum.dto.output.EventInitiatorDto;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;
import ru.practicum.state.UserProfileState;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {ExploreWithMeMainService.class})
@WebMvcTest(controllers = PublicController.class)
@ExtendWith(MockitoExtension.class)
public class PublicControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventService eventService;
    @MockBean
    private CategoryService categoryService;
    @MockBean
    private CompilationService compilationService;
    @MockBean
    private UserService userService;

    @SneakyThrows
    @Test
    public void shouldGetInitiatorsStandardCase() {
        EventInitiatorDto eventInitiatorDto = new EventInitiatorDto();
        eventInitiatorDto.setId(1L);
        eventInitiatorDto.setName("test");
        eventInitiatorDto.setProfile(UserProfileState.PRIVATE);
        when(userService.getInitiators(any(), any(), any(), any())).thenReturn(List.of(eventInitiatorDto));
        mockMvc.perform(get("/initiators")
                .param("sort", "MOST_POPULAR")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
