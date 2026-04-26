package com.clm.platform.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = { "debug=false", "logging.level.root=INFO", "logging.level.org.springframework=INFO" })
@ActiveProfiles("test")
class MigrationSmokeTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void baselineMigrationCreatesPlatformTables() {
		Integer auditTableCount = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'audit_events'",
			Integer.class);
		Integer taskTableCount = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'task_runs'",
			Integer.class);
		Integer certificateTableCount = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'certificates'",
			Integer.class);
		Integer certificateVersionTableCount = jdbcTemplate.queryForObject(
			"select count(*) from information_schema.tables where table_name = 'certificate_versions'",
			Integer.class);

		assertThat(auditTableCount).isEqualTo(1);
		assertThat(taskTableCount).isEqualTo(1);
		assertThat(certificateTableCount).isEqualTo(1);
		assertThat(certificateVersionTableCount).isEqualTo(1);
	}
}
