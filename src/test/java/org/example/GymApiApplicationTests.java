package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.transaction.Transactional;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GymApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	private String username;
	private String password;

	@BeforeEach
	void setup() throws Exception {
		MvcResult result = mockMvc.perform(post("/trainees")
						.param("firstName", "FNM")
						.param("lastName", "LNM"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("FNM.LNM"))
				.andExpect(jsonPath("$.password").exists())
				.andReturn();

		String response = result.getResponse().getContentAsString();
		this.username = response.split("\"username\":\"")[1].split("\"")[0];
		this.password = response.split("\"password\":\"")[1].split("\"")[0];
	}

	private String authHeader() {
		return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}

	@Test
	void getTrainee_afterCreation() throws Exception {
		mockMvc.perform(get("/trainees/{username}", username)
						.header("Authorization", authHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.Trainee.firstName").value("FNM"))
				.andExpect(jsonPath("$.Trainee.lastName").value("LNM"));
	}

	@Test
	void login_withWrongPassword_returns401() throws Exception {
		String loginJson = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, "wrong123");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid username or password"));
	}

	@Test
	void login_withCorrectCredentials_returns200() throws Exception {
		String loginJson = String.format("""
        {
            "username": "%s",
            "password": "%s"
        }
        """, username, password);

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Login successful"));
	}

	@Test
	void get_firstName_withCorrectPassword_returns200() throws Exception {
		mockMvc.perform(get("/trainees/{username}", username)
						.header("Authorization", authHeader()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.Trainee.firstName").value("FNM"));
	}

	@Test
	void delete_trainee_returns204() throws Exception {
		mockMvc.perform(delete("/trainees/{username}", username)
						.header("Authorization", authHeader()))
				.andExpect(status().isNoContent());
	}
}
