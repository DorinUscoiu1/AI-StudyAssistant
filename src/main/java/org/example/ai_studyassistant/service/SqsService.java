package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue-name}")
    private String queueName;

    public void sendQuizEvent(String username, String topic, int questionCount) {
        try {
            String queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder()
                            .queueName(queueName)
                            .build()
            ).queueUrl();

            String message = String.format(
                    "{\"username\":\"%s\",\"topic\":\"%s\",\"questionCount\":%d,\"timestamp\":\"%s\"}",
                    username, topic, questionCount, java.time.Instant.now()
            );

            sqsClient.sendMessage(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(message)
                            .build()
            );

            log.info("SQS event sent: user={}, topic={}", username, topic);

        } catch (Exception e) {
            log.warn("SQS send failed (non-critical): {}", e.getMessage());
        }
    }
}