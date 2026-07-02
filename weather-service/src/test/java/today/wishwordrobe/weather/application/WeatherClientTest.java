package today.wishwordrobe.weather.application;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import today.wishwordrobe.weather.configuration.AirKoreaConfig;
import today.wishwordrobe.weather.configuration.WeatherConfig;
import today.wishwordrobe.weather.domain.Geographic;
import today.wishwordrobe.weather.dto.VillageForecastResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * WeatherClient#getVillageForecast() 단위 테스트.
 * WireMock으로 기상청 단기예보 외부 API를 흉내내어, lat/lon 기반 격자 좌표로 실제 WebClient 호출과
 * 응답(JSON) → VillageForecastResponse 역직렬화가 올바르게 동작하는지 검증.
 */
class WeatherClientTest {

    private WireMockServer wireMockServer;
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        WeatherConfig weatherConfig = new WeatherConfig();
        weatherConfig.setApiKey("test-service-key");
        weatherConfig.setBaseUrl("http://localhost:" + wireMockServer.port());
        weatherConfig.setVillageFcstUrl("/getVilageFcst");

        weatherClient = new WeatherClient();
        ReflectionTestUtils.setField(weatherClient, "webClient", WebClient.create());
        ReflectionTestUtils.setField(weatherClient, "config", weatherConfig);
        ReflectionTestUtils.setField(weatherClient, "airKoreaConfig", new AirKoreaConfig());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("격자 좌표로 기상청 API 호출 후 VillageForecastResponse로 파싱한다")
    void fetchesVillageForecastFromExternalApi() {
        wireMockServer.stubFor(get(urlPathEqualTo("/getVilageFcst"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "response": {
                                    "header": { "resultCode": "00", "resultMsg": "NORMAL_SERVICE" },
                                    "body": {
                                      "dataType": "JSON",
                                      "items": {
                                        "item": [
                                          { "baseDate": "20260702", "baseTime": "0500", "category": "TMX",
                                            "fcstDate": "20260702", "fcstTime": "1500", "fcstValue": "28", "nx": 60, "ny": 127 },
                                          { "baseDate": "20260702", "baseTime": "0500", "category": "TMN",
                                            "fcstDate": "20260702", "fcstTime": "0600", "fcstValue": "18", "nx": 60, "ny": 127 }
                                        ]
                                      },
                                      "pageNo": 1,
                                      "numOfRows": 1000,
                                      "totalCount": 2
                                    }
                                  }
                                }
                                """)));

        Geographic location = Geographic.builder()
                .gridX(60)
                .gridY(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .country("대한민국")
                .build();

        StepVerifier.create(weatherClient.getVillageForecast(location))
                .assertNext(response -> {
                    assertThat(response.getResponse().getHeader().getResultCode()).isEqualTo("00");
                    var items = response.getResponse().getBody().getItems().getItem();
                    assertThat(items).hasSize(2);
                    assertThat(items.get(0).getCategory()).isEqualTo("TMX");
                    assertThat(items.get(0).getFcstValue()).isEqualTo("28");
                })
                .verifyComplete();

        wireMockServer.verify(WireMock.getRequestedFor(urlPathEqualTo("/getVilageFcst"))
                .withQueryParam("nx", WireMock.equalTo("60"))
                .withQueryParam("ny", WireMock.equalTo("127"))
                .withQueryParam("serviceKey", WireMock.equalTo("test-service-key")));
    }

    @Test
    @DisplayName("외부 API가 429를 반환하면 재시도 후에도 실패하면 에러로 종료한다")
    void propagatesErrorAfterRetriesExhausted() {
        wireMockServer.stubFor(get(urlPathEqualTo("/getVilageFcst"))
                .willReturn(aResponse().withStatus(429)));

        Geographic location = Geographic.builder()
                .gridX(60)
                .gridY(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .country("대한민국")
                .build();

        StepVerifier.create(weatherClient.getVillageForecast(location))
                .expectError()
                .verify(java.time.Duration.ofSeconds(30));
    }
}
