package runners;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import config.SecurityProperties;
import config.SecurityProperties.UniquenessMode;
import lombok.RequiredArgsConstructor;
import repositories.account.AccountRepository;

@Component
@RequiredArgsConstructor
public class UniquenessIntegrityChecker implements CommandLineRunner {

	private final SecurityProperties properties;
	private final AccountRepository accountRepository;

	@Override
	public void run(String... args) throws Exception {
		if (StringUtils.equalsIgnoreCase(UniquenessMode.USERNAME.toString(), properties.getAuth().getUniquenessMode().toString())) {
			checkUsernameIntegrity();
		}
	}
	
	private void checkUsernameIntegrity() {
		List<String> duplicateUsernames = accountRepository.findDuplicateUsernames();

		if (!duplicateUsernames.isEmpty()) {
			String violations = duplicateUsernames.stream()
					.limit(10)
					.collect(Collectors.joining(", "));

			throw new IllegalStateException(
				"Integrity constraint violated : " +
				"UniquenessMode is set to 'username', but duplicates exist. " +
				"Please fix the accounts before starting the application, or revert to 'username-tag' UniquenessMode. " +
				"First duplicates found : " + violations
			);
		}
	}
}