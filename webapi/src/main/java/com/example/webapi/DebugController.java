package com.example.webapi;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
  @Autowired
  OAuth2AuthorizedClientService authorizedClientService;

  @GetMapping(value = "/me", produces = "application/json")
  @ResponseBody
  public String me() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String accessToken = authorizedClientService.loadAuthorizedClient("pingidentity", authentication.getName())
        .getAccessToken()
        .getTokenValue();

    return String.join("\n", Arrays.asList(accessToken.split("\\."))
        .stream()
        .map(section -> deserialize(section))
        .collect(Collectors.toList()));
  }

  private String deserialize(String node) {
    try {

      String payload = new String(Base64.getDecoder().decode(node));
      ObjectMapper mapper = new ObjectMapper();
      JsonNode payloadNode = mapper.readTree(payload);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payloadNode);
    } catch (Exception ex) {
      return node;
    }
  }
}
