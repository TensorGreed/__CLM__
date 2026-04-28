ALTER TABLE certificate_versions
    ADD COLUMN public_key_size_bits INTEGER;

ALTER TABLE certificate_versions
    ADD CONSTRAINT ck_certificate_versions_public_key_size_bits
    CHECK (public_key_size_bits IS NULL OR public_key_size_bits > 0);
