package com.clm.platform.api.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.clm.platform.api.error.ApiException;

class QuerySpecTests {

	@Test
	void pageSpecUsesDefaults() {
		PageSpec pageSpec = PageSpec.from(null, null);

		assertThat(pageSpec.page()).isZero();
		assertThat(pageSpec.size()).isEqualTo(25);
	}

	@Test
	void pageSpecRejectsOversizedPages() {
		assertThatThrownBy(() -> PageSpec.from(0, 101))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("Page size");
	}

	@Test
	void sortSpecParsesFieldAndDirection() {
		List<SortSpec> sorts = SortSpec.parse(List.of("createdAt,desc"));

		assertThat(sorts).containsExactly(new SortSpec("createdAt", SortDirection.DESC));
	}

	@Test
	void sortSpecParsesSpringSplitFieldAndDirection() {
		List<SortSpec> sorts = SortSpec.parse(List.of("createdAt", "desc"));

		assertThat(sorts).containsExactly(new SortSpec("createdAt", SortDirection.DESC));
	}

	@Test
	void filterSpecParsesFieldValuePairs() {
		List<FilterSpec> filters = FilterSpec.parse(List.of("status:queued"));

		assertThat(filters).containsExactly(new FilterSpec("status", "queued"));
	}
}
