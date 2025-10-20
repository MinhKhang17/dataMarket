package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.PricingRule;
import com.example.datasetapi.model.UserManager.ConsumerSubscription;
import com.example.datasetapi.model.UserManager.User;
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
}
