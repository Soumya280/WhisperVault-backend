package com.whispervault.DTO.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Signup {

    private String email;
    private String username;
    private String alias = username;
    private String password;
}
