package com.example.web;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    String response = "Token:\n";
    response += String.join("\n", Arrays.asList(accessToken.split("\\."))
        .stream()
        .map(section -> deserialize(section))
        .collect(Collectors.toList()));

    OAuth2AuthenticationToken oauth2Authentication = (OAuth2AuthenticationToken) authentication;
    response += "\n\nAttributes:\n";
    response += String.join("\n", oauth2Authentication.getPrincipal().getAttributes().entrySet().stream()
        .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.toList()));
    return response;
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
