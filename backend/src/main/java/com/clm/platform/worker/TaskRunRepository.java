package com.clm.platform.worker;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRunRepository extends JpaRepository<TaskRun, UUID> {

	Optional<TaskRun> findByIdempotencyKey(String idempotencyKey);
}
