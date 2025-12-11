package com.dto;
import jakarta.validation.constraints.NotBlank;
public class CreateAccountRequest {
    @NotBlank(message = "name must not be blank") private String name;
    public CreateAccountRequest() {} public CreateAccountRequest(String name) { this.name = name; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
}
