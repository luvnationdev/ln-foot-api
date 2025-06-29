package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.services.LeagueService;

@SpringBootTest
@AutoConfigureMockMvc
class LeagueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeagueService leagueService;

    @Autowired
    private ObjectMapper objectMapper;

    private LeagueDto createMockLeagueDto(String id) {
        return LeagueDto.builder()
                .id(id)
                .name("Mock League")
                .country("Mockland")
                .season("2023")
                .logoUrl("http://example.com/logo.png")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser
    void listLeagues_isOk_returnsPage() throws Exception { // Updated test
        LeagueDto mockLeague = createMockLeagueDto("L1");
        Page<LeagueDto> leaguePage = new PageImpl<>(Collections.singletonList(mockLeague), PageRequest.of(0, 1), 1);

        when(leagueService.listLeagues(eq("Mockland"), eq(null), any(Pageable.class))).thenReturn(leaguePage);

        mockMvc.perform(get("/api/v1/leagues")
                .param("country", "Mockland")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("L1")))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithAnonymousUser
    void findLeagueById_isOk_whenFound() throws Exception {
        String leagueId = "L123";
        LeagueDto mockLeague = createMockLeagueDto(leagueId);
        when(leagueService.findLeagueById(leagueId)).thenReturn(Optional.of(mockLeague));

        mockMvc.perform(get("/api/v1/leagues/{id}", leagueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(leagueId)));
    }

    @Test
    @WithAnonymousUser
    void findLeagueById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(leagueService.findLeagueById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/leagues/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- Admin Endpoint Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void createLeague_isCreated_withAdminRole() throws Exception {
        CreateLeagueDto createDto = CreateLeagueDto.builder().name("New League").apiFootballId("NL1").build();
        LeagueDto returnedDto = createMockLeagueDto("NL1");
        returnedDto.setName("New League");
        when(leagueService.createLeague(any(CreateLeagueDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/leagues").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New League")));
    }

    @Test
    void createLeague_isUnauthorized_withoutAuth() throws Exception {
        CreateLeagueDto createDto = CreateLeagueDto.builder().build();
        mockMvc.perform(post("/api/v1/leagues").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createLeague_isForbidden_withUserRole() throws Exception {
        CreateLeagueDto createDto = CreateLeagueDto.builder().build();
        mockMvc.perform(post("/api/v1/leagues").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLeague_isOk_withAdminRole() throws Exception {
        String leagueId = "LToUpdate";
        UpdateLeagueDto updateDto = UpdateLeagueDto.builder().name("Updated Name").build();
        LeagueDto returnedDto = createMockLeagueDto(leagueId);
        returnedDto.setName("Updated Name");

        when(leagueService.updateLeague(eq(leagueId), any(UpdateLeagueDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/leagues/{id}", leagueId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteLeague_isNoContent_withAdminRole() throws Exception {
        String leagueId = "LToDelete";
        doNothing().when(leagueService).deleteLeague(leagueId);

        mockMvc.perform(delete("/api/v1/leagues/{id}", leagueId).with(csrf()))
                .andExpect(status().isNoContent());
        verify(leagueService, times(1)).deleteLeague(leagueId);
    }
}
