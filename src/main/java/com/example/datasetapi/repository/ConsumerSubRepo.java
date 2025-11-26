package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.PricingRule;
import com.example.datasetapi.model.userManager.Consumer;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.userManager.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumerSubRepo extends JpaRepository<ConsumerSubscription,Long> {

    List<ConsumerSubscription> findByConsumer(User consumer);

    List<ConsumerSubscription> findAllByConsumerAndIsUsing(User consumer, boolean b);

    Optional<ConsumerSubscription> findByConsumerAndIsUsing(User consumer, boolean b);


    boolean existsByPricingRuleAndConsumerAndIsActive(PricingRule pricingRule, User consumer, boolean b);

    boolean existsByConsumerAndIsActiveAndIsUsing(User consumer, boolean isActive, boolean isUsing);

    ConsumerSubscription findByConsumerAndIsActiveAndIsUsing(User consumer, boolean isActive, boolean isUsing);

    boolean existsByConsumerAndIsActiveAndIsUsingAndPricingRule(User consumer, boolean isActive, boolean isUsing, PricingRule pricingRule);


    Optional<ConsumerSubscription> findByConsumerAndIsActiveAndIsUsingAndPricingRule(User consumer, boolean isActive, boolean isUsing, PricingRule pricingRule);
}
