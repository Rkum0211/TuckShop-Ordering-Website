package com.tuckshop.ordering.service;

import com.tuckshop.ordering.entity.ShopSettings;
import com.tuckshop.ordering.repository.ShopSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final ShopSettingsRepository shopSettingsRepository;

    public SettingsService(ShopSettingsRepository shopSettingsRepository) {
        this.shopSettingsRepository = shopSettingsRepository;
    }

    @Transactional(readOnly = true)
    public ShopSettings getSettings() {
        return shopSettingsRepository.findById(1L).orElseGet(() -> {
            ShopSettings defaultSettings = new ShopSettings();
            return shopSettingsRepository.save(defaultSettings);
        });
    }

    @Transactional
    public ShopSettings updateSettings(ShopSettings newSettings) {
        ShopSettings settings = getSettings();
        if (newSettings.getShopName() != null) settings.setShopName(newSettings.getShopName());
        if (newSettings.getPickupLocation() != null) settings.setPickupLocation(newSettings.getPickupLocation());
        if (newSettings.getIsOpen() != null) settings.setIsOpen(newSettings.getIsOpen());
        settings.setOpeningTime(newSettings.getOpeningTime());
        settings.setClosingTime(newSettings.getClosingTime());
        settings.setDailyOrderLimit(newSettings.getDailyOrderLimit());
        settings.setEstimatedPreparationMinutes(newSettings.getEstimatedPreparationMinutes());
        settings.setMinOrderAmount(newSettings.getMinOrderAmount());
        settings.setMaxOrderAmount(newSettings.getMaxOrderAmount());
        return shopSettingsRepository.save(settings);
    }
}
