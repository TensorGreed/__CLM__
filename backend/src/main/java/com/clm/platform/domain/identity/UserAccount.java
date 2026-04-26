package com.clm.platform.domain.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "display_name", nullable = false, length = 200)
	private String displayName;

	@Column(name = "password_hash", length = 255)
	private String passwordHash;

	@Column(name = "external_subject", length = 256)
	private String externalSubject;

	@Column(name = "external_provider", length = 128)
	private String externalProvider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AccountStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserAccount() {
	}

	public UserAccount(UUID id, String email, String displayName, String passwordHash, Instant now) {
		this.id = id;
		this.email = email;
		this.displayName = displayName;
		this.passwordHash = passwordHash;
		this.status = AccountStatus.ACTIVE;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public UUID id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public AccountStatus status() {
		return status;
	}
}
