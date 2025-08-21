package hackathon.bigone.sunsak.foodbox.ocr.service;

import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService { //OCR 호출
    @Value("${naver.ocr.invoke-url}")
    private String apiUrl;

    @Value("${naver.ocr.secret-key}")
    private String secretKey;

    public List<OcrExtractedItem> extractItemNamesFromImage(MultipartFile file) throws Exception {
        String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();

        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setReadTimeout(30000);
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("X-OCR-SECRET", secretKey);

        // JSON message 구성
        JSONObject message = new JSONObject();
        message.put("version", "V2"); // 필요시 png로 변경
        message.put("requestId", UUID.randomUUID().toString());
        message.put("timestamp", System.currentTimeMillis());

        JSONObject imageObj = new JSONObject();
        imageObj.put("format", "jpg");
        imageObj.put("name", file.getOriginalFilename());
        JSONArray images = new JSONArray();
        images.put(imageObj);
        message.put("images", images);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        writeMultipartForm(byteStream, message.toString(), file, boundary);

        // 요청 전송
        conn.connect();
        try (OutputStream os = conn.getOutputStream()) {
            byteStream.writeTo(os);
            os.flush();
        }

        // 응답 처리
        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
        String rawResponse = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        conn.disconnect();

        return parseItemsFromResponse(rawResponse);
    }

    private void writeMultipartForm(OutputStream out, String jsonMessage, MultipartFile file, String boundary) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

        // message part
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
        writer.append(jsonMessage).append("\r\n");
        writer.flush();

        // file part
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getOriginalFilename()).append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        file.getInputStream().transferTo(out);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));

        // 끝나는 boundary
        writer.append("--").append(boundary).append("--\r\n");
        writer.flush();
    }

    private List<OcrExtractedItem> parseItemsFromResponse(String response) {
        List<OcrExtractedItem> resultList = new ArrayList<>();

        JSONObject responseJson = new JSONObject(response); // 응답 전체 파싱
        JSONArray images = responseJson.getJSONArray("images");
        JSONObject receipt = images.getJSONObject(0).getJSONObject("receipt");
        JSONArray subResults = receipt.getJSONObject("result").getJSONArray("subResults");

        for (int i = 0; i < subResults.length(); i++) {
            JSONArray items = subResults.getJSONObject(i).getJSONArray("items");

            for (int j = 0; j < items.length(); j++) {
                JSONObject item = items.getJSONObject(j);

                //이름 추출
                JSONObject nameObj = item.getJSONObject("name");
                String name = nameObj.has("formatted")
                        ? nameObj.getJSONObject("formatted").getString("value")
                        : nameObj.getString("text");

                //수량
                int quantity = 1;
                if (item.has("count")) {
                    JSONObject countObj = item.getJSONObject("count");
                    if (countObj.has("formatted")) {
                        String rawCount = countObj.getJSONObject("formatted").optString("value", "1");
                        try {
                            quantity = Integer.parseInt(rawCount);
                        } catch (NumberFormatException e) {
                            quantity = 1;
                        }
                    } else if (countObj.has("text")) {
                        String rawCount = countObj.optString("text", "1");
                        try {
                            quantity = Integer.parseInt(rawCount);
                        } catch (NumberFormatException e) {
                            quantity = 1;
                        }
                    }
                }

                log.debug("[OCR] sub[{}]/item[{}] name='{}', qty={}", i, j, name, quantity);
                resultList.add(new OcrExtractedItem(name, quantity));
            }
        }

        log.debug("[OCR] total items parsed: {}", resultList.size());
        return resultList;
    }
}
