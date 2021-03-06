package br.com.willianantunes.services.mappers;

import br.com.willianantunes.domain.User;
import br.com.willianantunes.services.dtos.UserCreateDTO;
import br.com.willianantunes.services.dtos.UserDTO;
import br.com.willianantunes.services.dtos.UserUpdateDTO;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMapper {

    public UserDTO userToUserDTO(User user) {

        return Optional.ofNullable(user).map(u ->
            UserDTO.builder()
                .id(Optional.ofNullable(user.getId()).map(id -> id.toString()).orElse(null))
                .name(user.getName())
                .email(user.getEmail())
                .build()).orElse(null);
    }

    public List<UserDTO> usersToUserDTOs(List<User> users) {

        return users.stream()
            .filter(Objects::nonNull)
            .map(this::userToUserDTO)
            .collect(Collectors.toList());
    }

    public User userDTOToUser(UserDTO userDTO) {

        return Optional.ofNullable(userDTO).map(u ->
            User.builder()
                .id(Optional.ofNullable(userDTO.getId()).map(id -> new ObjectId(id)).orElse(null))
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .build()).orElse(null);
    }

    public List<User> userDTOsToUsers(List<UserDTO> userDTOs) {

        return userDTOs.stream()
            .filter(Objects::nonNull)
            .map(this::userDTOToUser)
            .collect(Collectors.toList());
    }

    public User userCreateDTOToUser(UserCreateDTO userCreateDTO) {

        return Optional.ofNullable(userCreateDTO).map(u ->
            User.builder()
                .name(userCreateDTO.getName())
                .email(userCreateDTO.getEmail())
                .password(userCreateDTO.getPassword())
                .build()).orElse(null);
    }

    public User userUpdateDTOToUser(UserUpdateDTO userUpdateDTO) {

        return Optional.ofNullable(userUpdateDTO).map(u ->
            User.builder()
                .id(new ObjectId(userUpdateDTO.getId()))
                .name(userUpdateDTO.getName())
                .email(userUpdateDTO.getEmail())
                .password(userUpdateDTO.getPassword())
                .build()).orElse(null);
    }
}