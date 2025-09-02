package client;

import exception.NullBodyException;
import exception.RequestException;
import exception.StatsServerUnavailable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static util.StatsServerPaths.PATH_HIT;
import static util.StatsServerPaths.PATH_STAT;

@Slf4j
@Component
public class StatsClientRest implements StatsClient {
    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public StatsClientRest(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        restClient = RestClient.builder()
                .baseUrl(instance.getHost() + ":" + instance.getPort())
                .build();
    }

    private ServiceInstance getInstance() {
        String statsServerId = "stats-server";

        try {
            return discoveryClient
                    .getInstances(statsServerId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServerId
            );
        }
    }

    @Override
    public void createHit(EndpointHitDto hitDto) throws RequestException {
        log.info("Отправка hit app = {}, uri = {}, ip = {}, created = {}",
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                hitDto.getCreated());

        log.trace("Отправка запроса создания hit");
        ResponseEntity<Void> response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH_HIT)
                        .build())
                .body(hitDto)
                .contentType(APPLICATION_JSON)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        (req, resp) -> {
                            log.trace("Произошла ошибка при отправке Hit");
                            throw new RequestException(req.getMethod().name(),
                                    req.getURI().toString(),
                                    resp.getStatusCode().value(),
                                    convertBody(resp.getBody()));
                        })
                .toBodilessEntity();
        log.trace("Ответ при создании hit получен без ошибок");

        log.info("Hit был успешно отправлен на сервер, response code: {}", response.getStatusCode());
    }

    @Override
    public List<ViewStatsDto> getStat(StatParam statParam) throws RequestException {
        log.info("Получение статистики с параметрами: start = {}, end = {}, uris = {}, unique = {}",
                statParam.getStart(),
                statParam.getEnd(),
                statParam.getUris(),
                statParam.getUnique());

        log.trace("Отправка запроса на получение статистики");
        List<ViewStatsDto> response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(PATH_STAT)
                            .queryParam("start", statParam.getStart())
                            .queryParam("end", statParam.getEnd());

                    if (statParam.getUris() != null) {
                        uriBuilder.queryParam("uris", statParam.getUris());
                    }

                    if (statParam.getUnique() != null) {
                        uriBuilder.queryParam("unique", statParam.getUnique());
                    }

                    return uriBuilder.build();
                })
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(code -> code.is4xxClientError() || code.is5xxServerError(),
                        (req, resp) -> {
                            log.trace("Произошла ошибка при получении статистики");
                            throw new RequestException(req.getMethod().name(),
                                    req.getURI().toString(),
                                    resp.getStatusCode().value(),
                                    convertBody(resp.getBody()));
                        })
                .body(new ParameterizedTypeReference<>() {
                });
        log.trace("Ответ при запросе статистике получен без ошибок");

        log.debug("response == null ? {}", response == null);
        if (response == null) {
            throw new NullBodyException();
        }

        log.info("Статистика была успешно получена, count = {}", response.size());
        return response;
    }

    private String convertBody(InputStream inputStream) {
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String msg = "Не удалось конвертировать тело по причине: " + e.getMessage()
                    + " trace: " + Arrays.toString(e.getStackTrace());
            log.warn(msg);
            return msg;
        }
    }
}
