package com.clm.platform.testsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@SqlGroup({
	@Sql(
		statements = {
			"DELETE FROM certificate_tags",
			"DELETE FROM certificate_chain_entries",
			"UPDATE certificates SET current_version_id = NULL",
			"DELETE FROM certificate_versions",
			"DELETE FROM certificates",
			"DELETE FROM api_tokens",
			"DELETE FROM service_accounts",
			"DELETE FROM user_role_assignments",
			"DELETE FROM user_accounts",
			"DELETE FROM organizations",
			"DELETE FROM tenants",
			"DELETE FROM audit_events",
			"DELETE FROM task_runs"
		},
		executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
	@Sql(
		statements = {
			"DELETE FROM certificate_tags",
			"DELETE FROM certificate_chain_entries",
			"UPDATE certificates SET current_version_id = NULL",
			"DELETE FROM certificate_versions",
			"DELETE FROM certificates",
			"DELETE FROM api_tokens",
			"DELETE FROM service_accounts",
			"DELETE FROM user_role_assignments",
			"DELETE FROM user_accounts",
			"DELETE FROM organizations",
			"DELETE FROM tenants",
			"DELETE FROM audit_events",
			"DELETE FROM task_runs"
		},
		executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public @interface CleanDatabase {
}
