import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.practicum.controller.AdminController;
import ru.practicum.dto.input.NewCompilationDto;

import ru.practicum.entity.Compilation;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.service.*;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {ExploreWithMeMainService.class})
@WebMvcTest(controllers = AdminController.class)
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {
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
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    public void shouldSaveCompilationStandardCase() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("1111111");
        Compilation compilation = new Compilation();
        when(compilationService.saveCompilation(any())).thenReturn(CompilationMapper.toCompilationDto(compilation));
        mockMvc.perform(post("/admin/compilations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompilationDto))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @SneakyThrows
    @Test
    public void shouldSaveCompilationNotValidCase() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        Compilation compilation = new Compilation();
        when(compilationService.saveCompilation(any())).thenReturn(CompilationMapper.toCompilationDto(compilation));
        mockMvc.perform(post("/admin/compilations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCompilationDto))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}