package com.tuckshop.ordering.repository;

import com.tuckshop.ordering.entity.ShopSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopSettingsRepository extends JpaRepository<ShopSettings, Long> {
}
