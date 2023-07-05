package ru.practicum.dto.reversible;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.state.UserProfileState;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserDto {

    @NotBlank(message = "must not be blank")
    @Size(min = 6, max = 254, message = "length must be min=6, max=254")
    @Email(message = "must be email format")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "must not be blank")
    @Size(min = 2, max = 250, message = "length must be min=2, max=250")
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserProfileState profile;
}
