package com.contextguard.config;

import com.contextguard.detection.AuthenticationTokenDetector;
import com.contextguard.detection.DetectionPipeline;
import com.contextguard.detection.EmailDetector;
import com.contextguard.detection.LuhnValidator;
import com.contextguard.detection.PaymentCardDetector;
import com.contextguard.detection.PhilippineMobileDetector;
import com.contextguard.detection.SensitiveJsonFieldDetector;
import com.contextguard.policy.PolicyEvaluator;
import com.contextguard.policy.PolicyOutcomeResolver;
import com.contextguard.redaction.ArgumentRedactor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class GatewayPolicyConfiguration {

    @Bean
    public DetectionPipeline detectionPipeline() {
        return new DetectionPipeline(
                List.of(
                        new EmailDetector(),
                        new PhilippineMobileDetector(),
                        new PaymentCardDetector(new LuhnValidator()),
                        new AuthenticationTokenDetector()
                ),
                new SensitiveJsonFieldDetector()
        );
    }

    @Bean
    public PolicyEvaluator policyEvaluator() {
        return new PolicyEvaluator(new PolicyOutcomeResolver());
    }

    @Bean
    public ArgumentRedactor argumentRedactor() {
        return new ArgumentRedactor();
    }
}
