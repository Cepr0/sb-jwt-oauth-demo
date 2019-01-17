package io.github.cepr0.demo;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

	@SuppressWarnings("unchecked")
	@GetMapping("/demo")
	public Map demo(OAuth2Authentication auth) {

		var details = (OAuth2AuthenticationDetails) auth.getDetails();
		var decodedDetails = (Map<String, Object>) details.getDecodedDetails();

		return Map.of(
				"name", decodedDetails.get("user_name"),
				"email", decodedDetails.get("user_email"),
				"roles", decodedDetails.get("authorities")
		);
	}
}
