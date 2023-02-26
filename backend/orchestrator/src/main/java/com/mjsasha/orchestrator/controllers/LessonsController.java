package com.mjsasha.orchestrator.controllers;

import com.mjsasha.commonmodels.statistic.StatisticEvent;
import com.mjsasha.commonmodels.statistic.StatisticEventModel;
import com.mjsasha.orchestrator.configs.ServicesProperties;
import com.mjsasha.orchestrator.kafka.StatisticProducer;
import com.mjsasha.orchestrator.models.Lesson;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonsController {

    private static final String CONTROLLER_URI = "/lessons";
    private final WebClient webClient;
    private final StatisticProducer statisticProducer;

    public LessonsController(ServicesProperties servicesProperties, StatisticProducer statisticProducer) {
        webClient = WebClient.builder().baseUrl(servicesProperties.getFilesAndLessonsOrigin()).build();
        this.statisticProducer = statisticProducer;
    }

    @Operation(summary = "Used to create a lesson")
    @PostMapping
    public Integer create(@RequestBody Lesson lesson) {
        var lessonId = webClient
                .post().uri(CONTROLLER_URI)
                .body(Mono.just(lesson), Lesson.class)
                .retrieve().bodyToMono(Integer.class).block();

        if (lessonId != null)
            statisticProducer.sendEvent(new StatisticEventModel(StatisticEvent.LESSON_CREATED, lessonId));
        return lessonId;
    }

    @Operation(summary = "Used to read all lessons")
    @GetMapping
    public List<Lesson> read() {
        return webClient
                .get().uri(CONTROLLER_URI)
                .retrieve().bodyToFlux(Lesson.class).toStream().toList();
    }

    @Operation(summary = "Used to read one lesson by id")
    @GetMapping("/{id}")
    public Lesson read(@PathVariable Integer id) {
        return webClient
                .get().uri(CONTROLLER_URI + "/{id}", id)
                .retrieve().bodyToMono(Lesson.class).block();
    }

    @Operation(summary = "Used to delete a lesson")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        var response = ResponseEntity.ok(
                webClient
                        .delete().uri(CONTROLLER_URI + "/{id}", id)
                        .retrieve().bodyToMono(String.class).block());

        if (response.getStatusCode().is2xxSuccessful()) {
            statisticProducer.sendEvent(new StatisticEventModel(StatisticEvent.LESSON_DELETED, id, response.getBody()));
        }
        return response;
    }
}