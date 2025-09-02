package ru.practicum.main.service.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server")
@Validated
public interface StatsFeign {

    @PostMapping("/hit")
    ResponseEntity<String> hitStat(@Valid @RequestBody EndpointHitDto hitDto);

    @GetMapping("/stats")
    ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                                @RequestParam(name = "uris", required = false) List<String> uris,
                                                @RequestParam(name = "unique", defaultValue = "false") Boolean unique);
}
