package com.sixgroup.refit.observability.item35.creator.configuration;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class StrategyConfig {

    private final List<ItemTypeStrategy> itemTypeStrategies;

    @Bean
    public Map<ItemType, ItemTypeStrategy> getItemByType() {
        Map<ItemType, ItemTypeStrategy> itemByType = new EnumMap<>(ItemType.class);
        itemTypeStrategies.forEach(itemTypeStrategy -> itemByType.put(itemTypeStrategy.getItemType(), itemTypeStrategy));
        return itemByType;
    }
}
