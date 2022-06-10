
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.commons.io.FilenameUtils;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class Main {

    private final static String nasaURL =
            "https://api.nasa.gov/planetary/apod?api_key=HEfSrbXVUtbKlgmMlWf8cvIQDR5lcPkJhCOe0ob0";

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        // получаем объект HttpEntity из ссылки NASA
        HttpEntity entity = getResponseFromHttp(nasaURL);

        // создаем объект класса ApiNasaContent из контента json
        assert entity != null;
        ApiNasaContent content = mapper.readValue(entity.getContent().readAllBytes(), ApiNasaContent.class);

        // берем название файла из ссылки в полученном объекте
        String fileName = FilenameUtils.getName(content.getUrl());

        // получаем объект HttpEntity файла
        HttpEntity file = getResponseFromHttp(content.getUrl());

        // записываем в файл
        if (file != null) {
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                file.writeTo(outputStream);
            }
        }
    }

    public static HttpEntity getResponseFromHttp(String url) {
        CloseableHttpResponse response = null;
        HttpEntity entity = null;

        //создаем HTTP клиент
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
        try {

            //создаем запрос с заголовками
            HttpGet request = new HttpGet(url);
            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

            // отправляем запрос
            response = httpClient.execute(request);

            //получаем заголовки и выводим на экран
            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
            System.out.println();

            entity = response.getEntity();

            return entity;
        } catch (ConnectionClosedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
