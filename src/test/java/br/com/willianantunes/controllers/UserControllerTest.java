package br.com.willianantunes.controllers;

import br.com.willianantunes.services.UserService;
import br.com.willianantunes.services.dtos.UserCreateDTO;
import br.com.willianantunes.services.dtos.UserDTO;
import br.com.willianantunes.services.dtos.UserUpdateDTO;
import br.com.willianantunes.support.ScenarioBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.util.UriTemplate;

import java.util.Optional;

import static br.com.willianantunes.controllers.components.UrlBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ScenarioBuilder scenarioBuilder;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return a list of users as JSON")
    public void a() throws Exception {

        when(userService.getAllUsers()).thenReturn(scenarioBuilder.getMockedUserDTOs());

        mockMvc.perform(get(REQUEST_PATH_API + REQUEST_PATH_USER_GET_ALL))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should return an user when his is found by ID")
    public void b() throws Exception {

        UserDTO anyUser = scenarioBuilder.getMockedUserDTOs().stream().findAny().orElseThrow();
        anyUser.setId("fake-id");
        when(userService.getUserById(eq(anyUser.getId()))).thenReturn(Optional.of(anyUser));

        mockMvc.perform(get(new UriTemplate(REQUEST_PATH_API + REQUEST_PATH_USER_GET_OR_DELETE).expand(anyUser.getId())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.email", equalTo(anyUser.getEmail())));

        verify(userService).getUserById(eq(anyUser.getId()));
    }

    @Test
    @DisplayName("Should return 404 when no user is found")
    public void c() throws Exception {

        String fakeId = "fake-id";

        when(userService.getUserById(eq(fakeId))).thenReturn(Optional.empty());

        mockMvc.perform(get(new UriTemplate(REQUEST_PATH_API + REQUEST_PATH_USER_GET_OR_DELETE).expand(fakeId)))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(userService).getUserById(eq(fakeId));
    }

    @Test
    @DisplayName("Should return a user with 201 when a new one is created")
    public void d() throws Exception {

        UserDTO anyUser = scenarioBuilder.getMockedUserDTOs().stream().findAny().orElseThrow();

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
            .email(anyUser.getEmail())
            .name(anyUser.getName())
            .password("my-custom-honest-password")
            .build();

        when(userService.createUser(eq(userCreateDTO))).thenReturn(anyUser);

        mockMvc.perform(post(REQUEST_PATH_API + REQUEST_PATH_USER_POST_OR_PUT)
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(userCreateDTO)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", equalTo(new UriTemplate(REQUEST_PATH_API + REQUEST_PATH_USER_GET_OR_DELETE).expand(anyUser.getId()).toString())))
            .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        verify(userService).createUser(eq(userCreateDTO));
    }

    @Test
    @DisplayName("Should return 400 when the provided user is not valid to be created")
    public void e() throws Exception {

        UserDTO anyUser = scenarioBuilder.getMockedUserDTOs().stream().findAny().orElseThrow();

        UserCreateDTO userCreateDTO = UserCreateDTO.builder()
            .name("Salt Man")
            .email("honest-salt-shaker@salt.com")
            .build();

        mockMvc.perform(post(REQUEST_PATH_API + REQUEST_PATH_USER_POST_OR_PUT)
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(anyUser)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.violations[0].field", equalTo("password")));
    }

    @Test
    @DisplayName("Should return a user with 200 when an updated is executed in his properties")
    public void f() throws Exception {

        UserDTO anyUser = scenarioBuilder.getMockedUserDTOs().stream().findAny().orElseThrow();

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
            .id("fake-id")
            .name("Fake name")
            .email(anyUser.getEmail())
            .password("my-salted-password")
            .build();

        when(userService.updateUser(eq(userUpdateDTO))).thenReturn(anyUser);

        mockMvc.perform(put(REQUEST_PATH_API + REQUEST_PATH_USER_POST_OR_PUT)
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(userUpdateDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.email", equalTo(anyUser.getEmail())));

        verify(userService).updateUser(eq(userUpdateDTO));
    }

    @Test
    @DisplayName("Should return 400 when the provided user is not valid to be updated when ID is not available")
    public void g() throws Exception {

        UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
            .name("Salt Man")
            .email("salt-shaker@salt.com")
            .password("my-salted-password")
            .build();

        MvcResult result = mockMvc.perform(put(REQUEST_PATH_API + REQUEST_PATH_USER_POST_OR_PUT)
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(userUpdateDTO)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
    }

    @Test
    @DisplayName("Should return 200 when the user is deleted")
    public void h() throws Exception {

        String fakeId = "fake-id";

        mockMvc.perform(delete(new UriTemplate(REQUEST_PATH_API + REQUEST_PATH_USER_GET_OR_DELETE).expand(fakeId)))
            .andDo(print())
            .andExpect(status().isOk());

        verify(userService).deleteUserById(eq(fakeId));
    }
}